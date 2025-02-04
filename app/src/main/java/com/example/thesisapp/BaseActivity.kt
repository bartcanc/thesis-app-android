    package com.example.thesisapp
    
    import ApiClient
    import ApiService
    import android.Manifest
    import android.annotation.SuppressLint
    import android.app.Dialog
    import android.bluetooth.*
    import android.content.ContentValues
    import android.content.Context
    import android.content.Intent
    import android.content.SharedPreferences
    import android.content.pm.PackageManager
    import android.graphics.Color
    import android.os.Bundle
    import android.os.Environment
    import android.os.Handler
    import android.os.Looper
    import android.provider.Settings
    import android.util.Log
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import okhttp3.MediaType.Companion.toMediaTypeOrNull
    import okhttp3.RequestBody
    import okhttp3.ResponseBody
    import org.json.JSONArray
    import org.json.JSONObject
    import retrofit2.Call
    import java.io.File
    import java.io.FileOutputStream
    import java.io.IOException
    import java.nio.ByteBuffer
    import java.nio.ByteOrder
    import java.text.SimpleDateFormat
    import java.util.*
    import android.os.Build
    import android.os.HandlerThread
    import android.view.LayoutInflater
    import android.view.View
    import android.widget.ArrayAdapter
    import android.widget.Button
    import android.widget.EditText
    import android.widget.Spinner
    import android.widget.TextView
    import androidx.annotation.RequiresApi
    import androidx.appcompat.app.AlertDialog
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import retrofit2.Callback
    import java.util.concurrent.ConcurrentLinkedQueue
    
    
    open class BaseActivity : AppCompatActivity() {
        lateinit var apiClient: ApiClient
        lateinit var apiService: ApiService
        lateinit var sharedPref: SharedPreferences
        private var bluetoothAdapter: BluetoothAdapter? = null
        protected var bluetoothGatt: BluetoothGatt? = null
    
        private val serviceUUID: UUID = UUID.fromString("12345678-1234-1234-1234-123456789012")
        private val messageTransferUUID: UUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130005")
        private val confirmationUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130006")
        private val timeSyncUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130007")
        private val batteryLevelUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130021")
        private val firmwareVersionUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130022")
        private val howManyFilesUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130023")
        private val privateKeyUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130025")
    
        private var readLoopActive = false
    
        private val characteristicsUUIDs = listOf(batteryLevelUUID, firmwareVersionUUID, howManyFilesUUID)
        private var currentCharacteristicIndex = 0
    
        private var batteryLevel: Int? = null
        private var firmwareVersion: String? = null
        private var howManyFiles: Int? = null

        private var bufferIndex = 0
        private var unixTimestamp: Long = 0L
        private var isTimestampReceived = false
        private var zeroFlag = false
    
        private var fileSize = -1
        private var isFileSizeRead = false
        private var currentByteCount = 0
    
        protected var userId: String? = "."
        protected var sessionId: String? = "."
    
        private var provisioningComplete = false
        private var isConnecting = false
    
        protected var webSocketHelper = WebSocketHelper(this , "","")

        lateinit var permissionManager: PermissionManager

        private lateinit var dbHelper: SensorDataDatabaseHelper
        private val dataQueue = ConcurrentLinkedQueue<ByteArray>()
        private val dbWriteScope = CoroutineScope(Dispatchers.IO)

        private var totalReceivedBytes: Int = 0
        private val dataBuffer: Queue<ByteArray> = LinkedList()
    
        private val handlerThread = HandlerThread("BLEHandler").apply { start() }
        private val bleHandler = Handler(handlerThread.looper)
    
        private var startTime: Long = 0L
        private var endTime: Long = 0L

        private var incompleteData = ByteArray(0)

        protected fun setUserID(userId: String?) {
            userId?.let {
                with(sharedPref.edit()) {
                    putString("user_id", it)
                    apply()
                }
            }
        }

        private fun setSessionID(sessionId: String?) {
            sessionId?.let {
                with(sharedPref.edit()) {
                    putString("session_id", it)
                    apply()
                }
            }
        }
    
        protected fun getUserID(): String? {
            return try {
                sharedPref.getString("user_id", null)
            } catch (e: ClassCastException) {
                val userIdInt = sharedPref.getInt("user_id", -1)
                if (userIdInt != -1) {
                    val userIdString = userIdInt.toString()
                    setUserID(userIdString) // Zapisz `userId` ponownie jako `String`
                    userIdString
                } else {
                    null
                }
            }
        }

        protected fun getSessionID(): String? {
            return try {
                sharedPref.getString("session_id", null)
            } catch (e: ClassCastException) {
                val sessionIdInt = sharedPref.getInt("session_id", -1)
                if (sessionIdInt != -1) {
                    val sessionIdString = sessionIdInt.toString()
                    setSessionID(sessionIdString)
                    sessionIdString
                } else {
                    null
                }
            }
        }

        fun isRunningInTestMode(): Boolean {
            return try {
                Class.forName("androidx.test.espresso.Espresso")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            supportActionBar?.hide()

            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )

            window.statusBarColor = Color.TRANSPARENT
            sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)

            apiClient = ApiClient(this)
            apiService = apiClient.getApiService8000()

            setPrivateKey("nuvijridkvinorj")

            getUserID()?.let {
                userId = it
                Log.d("BaseActivity", "Pobrano userId z SharedPreferences: $userId")
            } ?: run {
                Log.d("BaseActivity", "userId jest pusty lub nie ustawiony w SharedPreferences")
            }

            getSessionID()?.let {
                sessionId = it
                Log.d("BaseActivity", "Pobrano sessionId z SharedPreferences: ${sessionId}")
            } ?: run {
                Log.d("BaseActivity", "SessionId jest pusty lub nie ustawiony w SharedPreferences")
            }

            permissionManager = PermissionManager(this)
            if(!permissionManager.allPermissionsGranted() && !isRunningInTestMode()) permissionManager.requestAllPermissions()
    
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = bluetoothManager.adapter

            dbHelper = SensorDataDatabaseHelper(this)
            deleteAllDatabaseData()

            if((userId != ".") and (sessionId != ".")) {
                WebSocketHelper(this, userId, sessionId).apply {
                    startConnection()
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onDestroy() {
            super.onDestroy()
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
            webSocketHelper.stopConnection()
        }

        @SuppressLint("MissingPermission")
        override fun onResume() {
            super.onResume()
            if (bluetoothGatt != null) {
                sendPrivateKeyToBand()
                Log.d("Bluetooth", "Połączenie Bluetooth już jest aktywne.")
                return
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
                val targetDeviceName = "ESP32 D3K"

                if (!pairedDevices.isNullOrEmpty()) {
                    val device = pairedDevices.firstOrNull { it.name == targetDeviceName }
                    if (device != null) {
                        connectToDevice(device)
                    } else {
                        Toast.makeText(this, "Nie znaleziono urządzenia o nazwie $targetDeviceName", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    //Toast.makeText(this, "Brak sparowanych urządzeń", Toast.LENGTH_SHORT).show()
                }
            }
        }
    
        private fun setPrivateKey(privateKey: String) {
            with(sharedPref.edit()) {
                putString("private_key", privateKey)
                apply()
            }
        }
    
        private fun getPrivateKey(): String? {
            return sharedPref.getString("private_key", null)
        }
    
        protected fun clearLoginData() {
            with(sharedPref.edit()) {
                remove("username")
                remove("password")
                remove("remember_me")
                apply()
            }
        }
    
        protected fun openBluetoothSettings() {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        }
    
        @SuppressLint("MissingPermission")
        private fun connectToDevice(device: BluetoothDevice) {
            if (isConnecting || bluetoothGatt != null) {
                Log.d("Bluetooth", "Połączenie już aktywne lub w trakcie łączenia.")
                return
            }
    
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                isConnecting = true
                bluetoothGatt = device.connectGatt(this, false, gattCallback)
            } else {
                Toast.makeText(this, "Brak uprawnień do połączenia Bluetooth.", Toast.LENGTH_SHORT).show()
            }
        }
    
        protected fun startSequentialRead() {
            currentCharacteristicIndex = 0
            readNextCharacteristic()
        }

        @SuppressLint("MissingPermission")
        private fun readNextCharacteristic() {
            if (currentCharacteristicIndex < characteristicsUUIDs.size) {
                val uuid = characteristicsUUIDs[currentCharacteristicIndex]
                val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(uuid)
    
                if (characteristic != null && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothGatt?.readCharacteristic(characteristic)
                } else {
                    Log.w("BLE", "Brak uprawnień lub charakterystyka $uuid nieznaleziona!")
                    currentCharacteristicIndex++
                    readNextCharacteristic()
                }
            }
        }

        @SuppressLint("MissingPermission")
        protected fun subscribeToCharacteristic() {
            val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(messageTransferUUID)

            if (characteristic != null) {
                bluetoothGatt?.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                if (descriptor != null) {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    val success = bluetoothGatt?.writeDescriptor(descriptor) == true
                    if (success) {
                        Log.d("BLE", "Subscription to characteristic started successfully.")
                    } else {
                        Log.e("BLE", "Failed to write descriptor for subscription.")
                    }
                } else {
                    Log.e("BLE", "Descriptor for notifications not found.")
                }
            } else {
                Log.e("BLE", "Characteristic not found for subscription.")
            }
        }

        @SuppressLint("MissingPermission")
        protected fun unsubscribeFromCharacteristic() {
            val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(messageTransferUUID)
            if (characteristic != null) {
                bluetoothGatt?.setCharacteristicNotification(characteristic, false)
                val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor?.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                bluetoothGatt?.writeDescriptor(descriptor)
                Log.d("BLE", "Subscription to characteristic stopped.")
                Toast.makeText(this, "Unsubscribed from notifications.", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("BLE", "Characteristic not found for unsubscription.")
                Toast.makeText(this, "Characteristic not found for unsubscription.", Toast.LENGTH_SHORT).show()
            }
        }
    
        private val gattCallback = object : BluetoothGattCallback() {
    
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                isConnecting = false
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "Connected to GATT server.")
                    if (ContextCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                        gatt.requestMtu(512)
                    } else {
                        Toast.makeText(this@BaseActivity, "Brak uprawnień Bluetooth.", Toast.LENGTH_SHORT).show()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLE", "Disconnected from GATT server.")
                    bluetoothGatt = null // Ustawienie na null, aby umożliwić ponowne połączenie
                }
            }
    
            override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLE", "Descriptor write successful. Subscribed to notifications.")
                } else {
                    Log.e("BLE", "Failed to write descriptor: $status")
                }
            }
    
            @SuppressLint("MissingPermission")
            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLE", "MTU size changed to $mtu")
                    if (ContextCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        gatt.discoverServices()
                    } else {
                        Toast.makeText(this@BaseActivity, "Brak uprawnień Bluetooth.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w("BLE", "Failed to change MTU size")
                }
            }
    
            @SuppressLint("MissingPermission")
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    sendPrivateKeyToBand()
                    Handler(Looper.getMainLooper()).postDelayed({
                        startSequentialRead()
                    }, 100)
                }
            }
    
            override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    when (characteristic.uuid) {
                        batteryLevelUUID -> {
                            batteryLevel =
                                characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                            Log.d("BLE", "Battery Level: $batteryLevel%")
                        }
                        firmwareVersionUUID -> {
                            firmwareVersion = characteristic.getStringValue(0)
                            Log.d("BLE", "Firmware Version: $firmwareVersion")
                        }
                        howManyFilesUUID -> {
                            howManyFiles =
                                characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0)
                            Log.d("BLE", "Number of Files: $howManyFiles")
                        }
                        else -> Log.w("BLE", "Nieznana charakterystyka odczytana")
                    }
                    currentCharacteristicIndex++  // Przejdź do kolejnej charakterystyki
                    readNextCharacteristic()
                } else {
                    Log.e("BLE", "Błąd odczytu charakterystyki o UUID ${characteristic.uuid}")
                }
            }
    
            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                if (characteristic.uuid == messageTransferUUID) {
                    var receivedData = characteristic.value
                    Log.d("BLE", "Received chunk: ${receivedData.joinToString(" ") { String.format("%02X", it) }}")
                    Log.d("BLE", "Received chunk size: ${receivedData.size} bytes")

                    if (totalReceivedBytes == 0) {
                        startTime = System.currentTimeMillis()
                        Log.d("TIME", "Odczyt danych rozpoczęty o czasie: $startTime ms")
                    }

                    if (!isFileSizeRead && receivedData.size >= 4) {
                        val byteBuffer = ByteBuffer.wrap(receivedData.copyOfRange(0, 4))
                        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                        fileSize = byteBuffer.int
                        isFileSizeRead = true
                        Log.d("BLE", "Rozmiar pliku: $fileSize bajtów")
    
                        // Usunięcie pierwszych 4 bajtów po odczytaniu rozmiaru pliku
                        receivedData = receivedData.copyOfRange(4, receivedData.size)
                    }
    
                    // Sprawdzanie, czy timestamp został już odebrany
                    if (!isTimestampReceived && receivedData.size >= 4) {
                        val byteBuffer = ByteBuffer.wrap(receivedData.copyOfRange(0, 4))
                        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                        unixTimestamp = byteBuffer.int.toLong()
                        isTimestampReceived = true
                        Log.d("BLE", "Otrzymano Unix timestamp: $unixTimestamp")
                        receivedData = receivedData.copyOfRange(4, receivedData.size) // Usunięcie pierwszych 4 bajtów
                    }
    
                    // Sprawdzanie końca transmisji (16 bajtów zerowych)
                    if (receivedData.size >= 16 && receivedData.takeLast(16).all { it == 0.toByte() }) {
                        endTime = System.currentTimeMillis()
                        val duration = endTime - startTime
                        Log.d("TIME", "Odczyt danych zakończony o czasie: $endTime ms")
                        Log.d("TIME", "Czas trwania odczytu danych: $duration ms")
                        processAllBlobsData()
                        resetFlags()
                    }

                    if (receivedData.isNotEmpty()) {
                        processDataChunk(receivedData)
                    }
                }
            }
        }
    
        private fun processDataChunk(data: ByteArray) {
            synchronized(dataBuffer) {
                dataBuffer.add(data)
            }
            totalReceivedBytes += data.size

            bleHandler.post {
                processBufferedData()
            }
        }
    
        private fun processBufferedData() {
            synchronized(dataBuffer) {
                while (dataBuffer.isNotEmpty()) {
                    val packet = dataBuffer.poll()
                    if (packet != null) {
                        saveRawDataToDatabaseAsync(unixTimestamp, packet)
                    }
                }
            }
        }
    
        private fun flushBatchData() {
            if (dataQueue.isNotEmpty()) {
                val db = dbHelper.writableDatabase
                db.beginTransaction()
                try {
                    for (packet in dataQueue) {
                        val values = ContentValues().apply {
                            put(SensorDataDatabaseHelper.COLUMN_TIMESTAMP, unixTimestamp)
                            put(SensorDataDatabaseHelper.COLUMN_RAW_DATA, packet)
                        }
                        db.insert(SensorDataDatabaseHelper.TABLE_NAME, null, values)
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                dataQueue.clear()
                Log.d("DB", "Zapisano pozostałe pakiety wsadowo")
            }
        }

        @SuppressLint("MissingPermission")
        fun sendUnixTime() {
            val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(timeSyncUUID)

            if (characteristic != null && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) {
                val unixTime = System.currentTimeMillis() / 1000

                val unixTimeBytes = ByteArray(4)
                for (i in unixTimeBytes.indices) {
                    unixTimeBytes[i] = (unixTime shr (i * 8) and 0xFF).toByte()
                }
                characteristic.value = unixTimeBytes

                if (bluetoothGatt?.writeCharacteristic(characteristic) == true) {
                    Log.d("BLE", "Czas Unix ${unixTime} został wysłany w formacie little-endian")
                } else {
                    Log.e("BLE", "Nie udało się wysłać czasu Unix")
                }
            } else {
                Log.e("BLE", "Charakterystyka nie znaleziona lub brak uprawnień!")
            }
        }

        @SuppressLint("MissingPermission")
        protected fun sendMessageOK() {
            val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(confirmationUUID)

            if (characteristic != null && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) {
                if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE == 0) {
                    Log.e("BLE", "Characteristic $confirmationUUID is not writable.")
                    return
                }
                characteristic.value = "OK".toByteArray(Charsets.UTF_8)

                if (bluetoothGatt?.writeCharacteristic(characteristic) == true) {
                    Log.d("BLE", "Wiadomość 'OK' została pomyślnie wysłana na characteristicUUID: $confirmationUUID")
                } else {
                    Log.e("BLE", "Nie udało się wysłać wiadomości 'OK' na characteristicUUID: $confirmationUUID")
                }
            } else {
                Log.e("BLE", "Charakterystyka nie znaleziona lub brak uprawnień!")
            }
        }

        @SuppressLint("MissingPermission")
        private fun sendPrivateKeyToBand() {
            val privateKey = getPrivateKey() ?: run {
                Log.e("BLE", "Private key is null or not available.")
                return
            }

            val service = bluetoothGatt?.getService(serviceUUID)
            if (service == null) {
                Log.e("BLE", "Service not found for UUID: $serviceUUID")
            }

            val privateKeyCharacteristic = service?.getCharacteristic(privateKeyUUID)
            if (privateKeyCharacteristic == null) {
                Log.e("BLE", "Characteristic for private key not found for UUID: $privateKeyUUID")
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e("BLE", "Permission BLUETOOTH_CONNECT is not granted.")
                return
            }

            privateKeyCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            privateKeyCharacteristic?.setValue(privateKey)  // Ustaw wartość bezpośrednio jako String

            val success = bluetoothGatt?.writeCharacteristic(privateKeyCharacteristic) == true
            if (success) {
                Log.d("BLE", "Private key sent successfully to band as UTF-8 text.")
                provisioningComplete = true
            } else {
                Log.e("BLE", "Failed to send private key to band.")
            }
        }

        fun getCurrentWifiCredentials(context: Context, callback: (String, String) -> Unit) {
            val dialog = Dialog(context)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setContentView(R.layout.wifi_data_dialog)

            val ssidInput = dialog.findViewById<EditText>(R.id.etWifiSSID)
            val passwordInput = dialog.findViewById<EditText>(R.id.etWifiPassword)
            val btnSend = dialog.findViewById<Button>(R.id.btnSendWifiData)
            val btnCancel = dialog.findViewById<Button>(R.id.btnCancelWifiData)

            btnSend.setOnClickListener {
                val enteredSsid = ssidInput.text.toString()
                val enteredPassword = passwordInput.text.toString()

                if (enteredSsid.isNotEmpty() && enteredPassword.isNotEmpty()) {
                    callback(enteredSsid, enteredPassword)
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "SSID i hasło nie mogą być puste.", Toast.LENGTH_SHORT).show()
                }
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        @SuppressLint("MissingPermission")
        fun sendWifiCredentials() {
            getCurrentWifiCredentials(this) { ssid, password ->  // Bez destrukturyzacji
                val ssidCharacteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130003"))
                val passwordCharacteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130004"))

                ssidCharacteristic?.let {
                    it.value = ssid.toByteArray(Charsets.UTF_8)
                    bluetoothGatt?.writeCharacteristic(it)
                    Log.d("BLE", "SSID sent: $ssid")
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    passwordCharacteristic?.let {
                        it.value = password.toByteArray(Charsets.UTF_8)
                        bluetoothGatt?.writeCharacteristic(it)
                        Log.d("BLE", "Password sent: $password")
                    }
                }, 100)
            }
        }

        protected fun showBandInfoDialog() {
            startSequentialRead()
            val dialogView = layoutInflater.inflate(R.layout.band_info_dialog, null)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val tvBatteryLevel = dialogView.findViewById<TextView>(R.id.tvBatteryLevel)
            val tvFirmwareVersion = dialogView.findViewById<TextView>(R.id.tvFirmwareVersion)
            val tvFilesToSend = dialogView.findViewById<TextView>(R.id.tvFilesToSend)
            val btnCloseBandInfo = dialogView.findViewById<Button>(R.id.btnCloseBandInfo)

            btnCloseBandInfo?.setOnClickListener {
                dialog.dismiss() // Zamknięcie dialogu
            }

            tvBatteryLevel?.text = getString(R.string.poziom_baterii, batteryLevel ?: "N/A")
            tvFirmwareVersion?.text = getString(R.string.wersja_firmware_1_0_2, firmwareVersion ?: "N/A")
            tvFilesToSend?.text = getString(R.string.liczba_plik_w_do_przes_ania, howManyFiles ?: "N/A")

            dialog.show()
        }

        private fun getAllDataFromDatabase(): List<ByteArray> {
            return dbHelper.getAllRawData()
        }

        private fun saveRawDataToDatabaseAsync(timestamp: Long, data: ByteArray) {
            dataQueue.add(data)
            dbWriteScope.launch {
                while (dataQueue.isNotEmpty()) {
                    val packet = dataQueue.poll()
                    if (packet != null) {
                        dbHelper.insertRawSensorData(timestamp, data)
                    }
                }
            }
        }

        fun convertUnixToISO8601(timestamp: Long): String {
            val date = Date(timestamp * 1000)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(date)
        }

        private fun deleteAllDatabaseData() {
            dbHelper.deleteAllData()
            Log.d("BaseActivity", "All data in the database has been deleted.")
        }

        fun processAllBlobsData() {
            val blobs = getAllDataFromDatabase()
            Log.d("test", "Liczba pobranych blobów: ${blobs.size}")

            if (blobs.isEmpty()) {
                Log.d("test", "Brak danych blob w bazie danych!")
                return
            }

            saveAllBlobsToSingleJsonUnified(blobs)
        }

        private fun saveAllBlobsToSingleJsonUnified(blobs: List<ByteArray>) {
            val axArray = JSONArray()
            val ayArray = JSONArray()
            val azArray = JSONArray()
            val irArray = JSONArray()
            val redArray = JSONArray()

            blobs.forEach { blob ->
                val combinedBlob = ByteArray(incompleteData.size + blob.size).apply {
                    System.arraycopy(incompleteData, 0, this, 0, incompleteData.size)
                    System.arraycopy(blob, 0, this, incompleteData.size, blob.size)
                }
                incompleteData = ByteArray(0)

                var offset = 0
                while (offset < combinedBlob.size) {
                    if (offset + 4 <= combinedBlob.size) {
                        val ir = ByteBuffer.wrap(combinedBlob, offset, 4).order(ByteOrder.LITTLE_ENDIAN).int
                        irArray.put(ir)
                        offset += 4
                    } else {
                        incompleteData = combinedBlob.copyOfRange(offset, combinedBlob.size)
                        break
                    }

                    if (offset + 4 <= combinedBlob.size) {
                        val red = ByteBuffer.wrap(combinedBlob, offset, 4).order(ByteOrder.LITTLE_ENDIAN).int
                        redArray.put(red)
                        offset += 4
                    } else {
                        incompleteData = combinedBlob.copyOfRange(offset, combinedBlob.size)
                        break
                    }

                    if (offset + 2 <= combinedBlob.size) {
                        val accX = ByteBuffer.wrap(combinedBlob, offset, 2).order(ByteOrder.LITTLE_ENDIAN).short
                        axArray.put(accX.toDouble())
                        offset += 2
                    } else {
                        incompleteData = combinedBlob.copyOfRange(offset, combinedBlob.size)
                        break
                    }

                    if (offset + 2 <= combinedBlob.size) {
                        val accY = ByteBuffer.wrap(combinedBlob, offset, 2).order(ByteOrder.LITTLE_ENDIAN).short
                        ayArray.put(accY.toDouble())
                        offset += 2
                    } else {
                        incompleteData = combinedBlob.copyOfRange(offset, combinedBlob.size)
                        break
                    }

                    if (offset + 2 <= combinedBlob.size) {
                        val accZ = ByteBuffer.wrap(combinedBlob, offset, 2).order(ByteOrder.LITTLE_ENDIAN).short
                        azArray.put(accZ.toDouble())
                        offset += 2
                    } else {
                        incompleteData = combinedBlob.copyOfRange(offset, combinedBlob.size)
                        break
                    }
                }
            }

            val jsonData = JSONObject().apply {
                put("ax", axArray)
                put("ay", ayArray)
                put("az", azArray)
                put("ir", irArray)
                put("red", redArray)
                put("recordedAt", convertUnixToISO8601(unixTimestamp))
                put("userId", userId)
            }

            val outputDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: return
            val outputFile = File(outputDir, "sensor_data.json")

            try {
                FileOutputStream(outputFile).use { outputStream ->
                    outputStream.write(jsonData.toString(4).toByteArray(Charsets.UTF_8))
                    Log.d("saveAllBlobsToSingleJsonUnified", "Plik JSON zapisany w: ${outputFile.absolutePath}")

                    showJsonSavedAlert()
                }
            } catch (e: IOException) {
                Log.e("saveAllBlobsToSingleJsonUnified", "Błąd podczas zapisywania pliku JSON", e)
            }
        }

        private fun showJsonSavedAlert() {
            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle("Zapis zakończony")
                    .setMessage("Dane zostały pomyślnie zapisane w pliku JSON.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }



        private fun resetFlags() {
            currentByteCount = 0
            bufferIndex = 0
            isTimestampReceived = false
            isFileSizeRead = false
            zeroFlag = false
            readLoopActive = false
            totalReceivedBytes = 0
            flushBatchData()
            sendMessageOK()
            sendUnixTime()
            dataBuffer.clear()
            unsubscribeFromCharacteristic()
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            permissionManager.handlePermissionsResult(grantResults)
        }

        protected fun showTrainingTypeDialog() {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.training_type_dialog, null)

            val spinnerTrainingType = dialogView.findViewById<Spinner>(R.id.spinnerTrainingType)
            val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

            val trainingTypeMap = mapOf(
                getString(R.string.activity_walking) to "Walking",
                getString(R.string.activity_jogging) to "Jogging",
                getString(R.string.activity_running) to "Running",
                getString(R.string.activity_sprint) to "Sprint",
                getString(R.string.activity_hiking) to "Hiking",
                getString(R.string.activity_tennis) to "Tennis",
                getString(R.string.activity_football) to "Football",
                getString(R.string.activity_basketball) to "Basketball",
                getString(R.string.activity_volleyball) to "Volleyball",
                getString(R.string.activity_light_exertion) to "Light Exertion",
                getString(R.string.activity_moderate_exertion) to "Moderate Exertion",
                getString(R.string.activity_intense_exertion) to "Intense Exertion"
            )

            val trainingTypes = trainingTypeMap.keys.toList()

            val adapter = ArrayAdapter(this, R.layout.spinner_item, trainingTypes)
            adapter.setDropDownViewResource(R.layout.spinner_item)
            spinnerTrainingType.setPopupBackgroundResource(android.R.color.transparent)
            spinnerTrainingType.adapter = adapter

            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            btnOk.setOnClickListener {
                val selectedType = spinnerTrainingType.selectedItem.toString()
                val selectedTrainingKey = trainingTypeMap[selectedType]

                if (selectedTrainingKey != null) {
                    sendSensorData(selectedTrainingKey)
                } else {
                    Toast.makeText(this, "Invalid training type selected", Toast.LENGTH_SHORT).show()
                }
                alertDialog.dismiss()
            }

            alertDialog.show()
        }



        private fun sendSensorData(workoutType: String) {
            if (workoutType.isEmpty()) {
                Log.e("sendSensorData", "Nie podano typu treningu!")
                return
            }

            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "sensor_data.json")

            if (file.exists()) {
                val jsonBody = RequestBody.create("application/json".toMediaTypeOrNull(), file.readText())

                val sessionId = getSessionID()
                Log.d("sendSensorData", "session-id before request: $sessionId")

                val call = apiService.sendSensorData(workoutType, jsonBody)

                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            Log.d("sendSensorData", "Dane zostały pomyślnie wysłane!")
                            Log.d("sendSensorData", "Kod odpowiedzi: ${response.code()}")
                            Log.d("sendSensorData", "Treść odpowiedzi: ${response.body()?.string()}")
                            Log.d("sendSensorData", "Nagłówki odpowiedzi: ${response.headers()}")
                        } else {
                            Log.e("sendSensorData", "Błąd podczas wysyłania danych. Kod odpowiedzi: ${response.code()}")
                            Log.e("sendSensorData", "Treść błędu: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("sendSensorData", "Błąd połączenia: ${t.message}")
                    }
                })
            } else {
                Log.e("sendSensorData", "Plik JSON nie został znaleziony!")
            }
        }
    }
