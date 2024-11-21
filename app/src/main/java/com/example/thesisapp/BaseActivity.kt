package com.example.thesisapp

import ApiClient
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*
import android.net.wifi.WifiManager
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog


open class BaseActivity : AppCompatActivity() {

    lateinit var sharedPref: SharedPreferences
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private val serviceUUID: UUID = UUID.fromString("12345678-1234-1234-1234-123456789012")
    private val messageTransferUUID: UUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130005")
    private val confirmationUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130006")
    private val timeSyncUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130007")
    private val batteryLevelUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130021")
    private val firmwareVersionUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130022")
    private val howManyFilesUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130023")
    // UUID dla charakterystyki klucza prywatnego w aplikacji Android
    private val privateKeyUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130025")

    private var readLoopActive = false

    private val characteristicsUUIDs = listOf(batteryLevelUUID, firmwareVersionUUID, howManyFilesUUID)
    private var currentCharacteristicIndex = 0

    private var batteryLevel: Int? = null
    private var firmwareVersion: String? = null
    private var howManyFiles: Int? = null

    private var bufferSize = 14
    private var dataBuffer = ByteArray(bufferSize)
    private var bufferIndex = 0
    private var unixTimestamp: Long = 0L
    private var isTimestampReceived = false
    private var zeroFlag = false

    private var fileSize = -1
    private var isFileSizeRead = false
    private var currentByteCount = 0

    protected var userId: String? = "."

    private var provisioningComplete = false
    private var isConnecting = false


    // Inicjalizacja bazy danych
    lateinit var dbHelper: SensorDataDatabaseHelper

    private lateinit var webSocketHelper: WebSocketHelper

    protected lateinit var permissionManager: PermissionManager

    protected fun setUserID(userId: String?) {
        userId?.let {
            with(sharedPref.edit()) {
                putString("user_id", it)
                apply()
            }
        }
    }

    protected fun getUserID(): String? {
        // Sprawdź, czy `user_id` jest zapisane jako `Int`, a jeśli tak, przekonwertuj na `String`
        return try {
            sharedPref.getString("user_id", null) // Spróbuj pobrać jako String
        } catch (e: ClassCastException) {
            // Jeśli nie uda się rzutować, to znaczy, że `user_id` jest przechowywane jako `Int`
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)

        savePrivateKey("nuvijridkvinorj")
        getUserID()?.let {
            userId = it
            Log.d("BaseActivity", "Pobrano userId z SharedPreferences: $userId")
        } ?: run {
            Log.d("BaseActivity", "userId jest pusty lub nie ustawiony w SharedPreferences")
        }

        permissionManager = PermissionManager(this)
        if(!permissionManager.allPermissionsGranted()) permissionManager.requestAllPermissions()

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        deleteDatabase()

        dbHelper = SensorDataDatabaseHelper(this)

        // Inicjalizacja WebSocket
        webSocketHelper = WebSocketHelper("ws://192.168.1.5:8010/connect")  // Podaj odpowiedni adres WebSocket

        // Sprawdzenie ważności sesji i ewentualne rozpoczęcie połączenia WebSocket
        if (isUserLoggedIn()) {
            webSocketHelper.startConnection()
        }
    }

    // Funkcja sprawdzająca, czy użytkownik jest zalogowany
    private fun isUserLoggedIn(): Boolean {
        val sessionId = sharedPref.getString("session_id", null)
        return !sessionId.isNullOrEmpty()
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        webSocketHelper.stopConnection()
    }

    protected fun checkSessionValidity() {
        val expirationDateStr = sharedPref.getString("expiration_date", null)
        if (expirationDateStr != null) {
            try {
                val sessionDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault())
                val expirationDate = sessionDateFormat.parse(expirationDateStr)
                val currentDate = Date()

                if (expirationDate != null && currentDate.after(expirationDate)) {
                    Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                    clearLoginData()
                    with(sharedPref.edit()) {
                        remove("session_id")
                        remove("expiration_date")
                        apply()
                    }
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            } catch (e: java.text.ParseException) {
                e.printStackTrace()
                Toast.makeText(this, "Error parsing session expiration date.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePrivateKey(privateKey: String) {
        with(sharedPref.edit()) {
            putString("private_key", privateKey)
            apply()
        }
    }

    private fun getPrivateKey(): String? {
        return sharedPref.getString("private_key", null)
    }


    fun clearLoginData() {
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

    fun deleteDatabase() {
        val deleted = this.deleteDatabase(SensorDataDatabaseHelper.DATABASE_NAME)
        if (deleted) {
            Log.d("DB", "Baza danych została usunięta.")
        } else {
            Log.e("DB", "Nie udało się usunąć bazy danych.")
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()

        if (bluetoothGatt != null) {
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
                Toast.makeText(this, "Brak sparowanych urządzeń", Toast.LENGTH_SHORT).show()
            }
        }
    }



    protected fun startSequentialRead() {
        currentCharacteristicIndex = 0
        readNextCharacteristic()
    }



    // Funkcja odczytująca kolejną charakterystykę z listy
    @SuppressLint("MissingPermission")
    private fun readNextCharacteristic() {
        if (currentCharacteristicIndex < characteristicsUUIDs.size) {
            val uuid = characteristicsUUIDs[currentCharacteristicIndex]
            val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(uuid)

            if (characteristic != null && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                bluetoothGatt?.readCharacteristic(characteristic)
            } else {
                Log.w("BLE", "Brak uprawnień lub charakterystyka $uuid nieznaleziona!")
                currentCharacteristicIndex++
                readNextCharacteristic()  // Przejdź do następnej, jeśli nie można odczytać tej
            }
        } else {
            Log.d("BLE", "Odczyt wszystkich charakterystyk zakończony.")
            val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(messageTransferUUID)
            if (characteristic != null && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                bluetoothGatt?.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                bluetoothGatt?.writeDescriptor(descriptor)
                Log.d("BLE", "Notifications enabled for messageTransferUUID")
            } else {
                Log.w("BLE", "Unable to enable notifications for messageTransferUUID")
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            isConnecting = false
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLE", "Connected to GATT server.")

                if (ContextCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    gatt.requestMtu(256)
                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                } else {
                    Toast.makeText(this@BaseActivity, "Brak uprawnień Bluetooth.", Toast.LENGTH_SHORT).show()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BLE", "Disconnected from GATT server.")
                bluetoothGatt = null // Ustawienie na null, aby umożliwić ponowne połączenie
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
                // Włącz powiadomienia dla messageTransferUUID
                val characteristic = gatt.getService(serviceUUID)?.getCharacteristic(messageTransferUUID)
                if (characteristic != null) {
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    if (descriptor != null) {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                        Log.d("BLE", "Notifications enabled for messageTransferUUID")
                    } else {
                        Log.w("BLE", "Descriptor for enabling notifications not found")
                    }
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    startSequentialRead()
                }, 100)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if ((characteristic.uuid == messageTransferUUID) and provisioningComplete) {
                var receivedData = characteristic.value
                Log.d("BLE", "Data received on messageTransferUUID: ${receivedData.joinToString(" ") { String.format("%02X", it) }}")
                // Sprawdzenie, czy rozmiar pliku został już odczytany
                if (!isFileSizeRead && receivedData.size >= 4) {
                    val byteBuffer = ByteBuffer.wrap(receivedData.copyOfRange(0, 4))
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                    fileSize = byteBuffer.int
                    isFileSizeRead = true  // Ustawienie flagi na true po odczytaniu rozmiaru pliku
                    Log.d("BLE", "Rozmiar pliku: $fileSize bajtów")

                    // Usunięcie pierwszych 4 bajtów po odczytaniu rozmiaru pliku
                    receivedData = receivedData.copyOfRange(4, receivedData.size)
                }

                // Sprawdzamy, czy timestamp został już odebrany
                if (!isTimestampReceived && receivedData.size >= 4) {
                    val byteBuffer = ByteBuffer.wrap(receivedData.copyOfRange(0, 4))
                    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
                    unixTimestamp = byteBuffer.int.toLong()
                    isTimestampReceived = true
                    currentByteCount += 4
                    Log.d("BLE", "Otrzymano Unix timestamp: $unixTimestamp")
                    receivedData = receivedData.copyOfRange(4, receivedData.size)  // Usunięcie pierwszych 4 bajtów
                }

                // Sprawdzenie, czy ostatnie 16 bajtów to same zera, co oznacza koniec transmisji
                if (receivedData.size >= 16 && receivedData.copyOfRange(receivedData.size - 16, receivedData.size).all { it == 0.toByte() }) {
                    Log.d("BLE", "Koniec transmisji - odebrano 16 zer na końcu pakietu.")
                    receivedData = receivedData.copyOfRange(0, receivedData.size - 16)  // Usuń końcowe zera
                    zeroFlag = true
                }

                // Dodajemy dane do bufora
                addToBuffer(receivedData)

                // Jeśli odbiór danych został zakończony, resetujemy flagi i wywołujemy `sendMessageOK`
                if (zeroFlag) {
                    if(currentByteCount == fileSize){
                        Log.d("BLE", "wszytko odebrano :) rozmiar = $currentByteCount")
                    }
                    else{
                        Log.d("BLE", "cos nie pyklo, trzeba jeszcze raz pobrac rozmiar = $currentByteCount")
                        deleteDatabase() //tu powinien usuwac sie tylko trening, ktory zostal zle odczytany
                    }
                    currentByteCount = 0
                    bufferIndex = 0
                    isTimestampReceived = false
                    isFileSizeRead = false
                    zeroFlag = false
                    readLoopActive = false
                    Log.d("BLE", "Wszystkie dane odebrane, wywołanie sendMessageOK.")
                    Handler(Looper.getMainLooper()).postDelayed({
                        sendMessageOK()
                    }, 100)
                    return
                }
            }
        }


        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid) {
                    batteryLevelUUID -> {
                        batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                        Log.d("BLE", "Battery Level: $batteryLevel%")
                    }
                    firmwareVersionUUID -> {
                        firmwareVersion = characteristic.getStringValue(0)
                        Log.d("BLE", "Firmware Version: $firmwareVersion")
                    }
                    howManyFilesUUID -> {
                        howManyFiles = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0)
                        Log.d("BLE", "Number of Files: $howManyFiles")
                    }
                    messageTransferUUID -> {
                        Log.w("BLE", "Do wiadomosci")
                    }
                    else -> Log.w("BLE", "Nieznana charakterystyka odczytana")
                }
                currentCharacteristicIndex++  // Przejdź do kolejnej charakterystyki
                readNextCharacteristic()
            } else {
                Log.e("BLE", "Błąd odczytu charakterystyki o UUID ${characteristic.uuid}")
            }
        }

    }

    private fun addToBuffer(data: ByteArray) {
        var dataIndex = 0

        while (dataIndex < data.size) {
            val spaceLeftInBuffer = bufferSize - bufferIndex
            val bytesToCopy = Math.min(spaceLeftInBuffer, data.size - dataIndex)

            // Logowanie danych do sprawdzenia
            Log.d("BUFFER", "Kopiowanie $bytesToCopy bajtów do bufora. Indeks bufora: $bufferIndex")

            // Kopiujemy tyle danych, ile się zmieści w buforze
            System.arraycopy(data, dataIndex, dataBuffer, bufferIndex, bytesToCopy)
            bufferIndex += bytesToCopy
            dataIndex += bytesToCopy
            currentByteCount += bytesToCopy
            // Sprawdzamy, czy bufor jest pełny
            if (bufferIndex == bufferSize) {
                saveBufferToDatabase()  // Zapisujemy dane do bazy
                bufferIndex = 0         // Resetujemy indeks bufora
            }
        }
    }

    private fun saveBufferToDatabase() {
        if (bufferIndex < bufferSize) {
            Log.e("BUFFER", "Bufor nie jest pełny, nie zapisujemy danych.")
            return
        }

        val byteBuffer = ByteBuffer.wrap(dataBuffer)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

        // Odczytujemy dane w pętlach po 12 bajtów na jeden zestaw (IR, Red, accX, accY, accZ)
        while (byteBuffer.remaining() >= 12) {
            val ir = byteBuffer.int.toLong()
            val red = byteBuffer.int.toLong()
            val accX = byteBuffer.short.toDouble()
            val accY = byteBuffer.short.toDouble()
            val accZ = byteBuffer.short.toDouble()

            // Logowanie dekodowanych wartości przed zapisem
            Log.d("DATA", "Dekodowane dane - IR: $ir, Red: $red, accX: $accX, accY: $accY, accZ: $accZ")

            // Zapisz dane do bazy, używając zapisanego wcześniej timestampu
            val id = dbHelper.insertSensorData(unixTimestamp, ir, red, accX, accY, accZ)
            if (id > 0) {
                Log.d("DB", "Dane zapisane w bazie z ID: $id, Unix timestamp: $unixTimestamp")
            } else {
                Log.e("DB", "Błąd zapisu danych w bazie")
            }
        }
    }

    protected fun startReadingLoop() {
        readLoopActive = true
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (readLoopActive) {
                    readCharacteristic()
                    handler.postDelayed(this, 1)
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun readCharacteristic() {
        if (!readLoopActive) return

        val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(messageTransferUUID)
        if (characteristic != null && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED) {
            bluetoothGatt?.readCharacteristic(characteristic)
        } else {
            Log.w("BLE", "Characteristic not found or permission denied!")
        }
    }

    @SuppressLint("MissingPermission")
    protected fun sendMessageOK() {
        // Pobranie charakterystyki `confirmationUUID` z usługi `serviceUUID`
        val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(confirmationUUID)

        // Sprawdzenie, czy charakterystyka istnieje i mamy wymagane uprawnienia
        if (characteristic != null && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED) {
            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE == 0) {
                Log.e("BLE", "Characteristic $confirmationUUID is not writable.")
                return
            }
            // Przekształcenie wartości "OK" na tablicę bajtów i przypisanie jej do charakterystyki
            characteristic.value = "OK".toByteArray(Charsets.UTF_8)

            // Próba zapisu charakterystyki, sprawdzenie czy zapis się powiódł
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

    fun createJSONFile() {
        val db = dbHelper.readableDatabase
        val jsonData = JSONObject()
        val axArray = JSONArray()
        val ayArray = JSONArray()
        val azArray = JSONArray()
        val irArray = JSONArray()
        val redArray = JSONArray()

        // Pobierz ostatni `timestamp` z bazy, jako czas `recordedAt`
        val cursor = db.query("sensor_data", null, null, null, null, null, null)
        val recordedAt: Long
        if (cursor.moveToFirst()) {
            recordedAt = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
        } else {
            cursor.close()
            Log.e("JSON", "Brak danych w bazie")
            return
        }
        cursor.close()

        // Konwertuj timestamp na czytelną datę
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(recordedAt * 1000))

        // Pobranie wszystkich danych i dodanie ich do odpowiednich tablic JSON
        val dataCursor = db.query("sensor_data", null, null, null, null, null, null)
        if (dataCursor.moveToFirst()) {
            do {
                axArray.put(dataCursor.getDouble(dataCursor.getColumnIndexOrThrow("acc_x")))
                ayArray.put(dataCursor.getDouble(dataCursor.getColumnIndexOrThrow("acc_y")))
                azArray.put(dataCursor.getDouble(dataCursor.getColumnIndexOrThrow("acc_z")))
                irArray.put(dataCursor.getDouble(dataCursor.getColumnIndexOrThrow("ir")))
                redArray.put(dataCursor.getDouble(dataCursor.getColumnIndexOrThrow("red")))
            } while (dataCursor.moveToNext())
        }
        dataCursor.close()

        // Ustawienie danych w obiekcie JSON
        jsonData.put("ax", axArray)
        jsonData.put("ay", ayArray)
        jsonData.put("az", azArray)
        jsonData.put("ir", irArray)
        jsonData.put("red", redArray)
        jsonData.put("userId", userId)
        jsonData.put("recordedAt", formattedDate)

        // Zapis JSON do pliku
        val fileName = "sensor_data.json"
        val filePath = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            FileOutputStream(filePath).use { outputStream ->
                outputStream.write(jsonData.toString().toByteArray(Charsets.UTF_8))
                Log.d("JSON", "Plik JSON zapisany w: ${filePath.absolutePath}")
                runOnUiThread {
                    Toast.makeText(this, "Plik JSON zapisany", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            Log.e("JSON", "Błąd podczas zapisywania pliku JSON", e)
            runOnUiThread {
                Toast.makeText(this, "Błąd zapisu pliku JSON", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handlePermissionsResult(requestCode, grantResults)
    }


    protected fun sendSensorData() {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "sensor_data.json")

        if (file.exists()) {
            val jsonBody = RequestBody.create("application/json".toMediaTypeOrNull(), file.readText())

            val apiClient = ApiClient(this) // Tworzymy instancję klienta API
            val apiService = apiClient.getApiService8000()

            // Sprawdzenie `session-id` przed wysłaniem danych
            val sessionId = sharedPref.getString("session_id", null)
            Log.d("sendSensorData", "session-id before request: $sessionId")

            val call = apiService.sendSensorData(jsonBody)
            call.enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        println("Dane zostały pomyślnie wysłane!")
                        println("Kod odpowiedzi: ${response.code()}")
                        println("Treść odpowiedzi: ${response.body()?.string()}")
                        println("Nagłówki odpowiedzi: ${response.headers()}")
                        println("Nagłówki wysłane: ${call.request().headers}")
                    } else {
                        println("Błąd podczas wysyłania danych. Kod odpowiedzi: ${response.code()}")
                        println("Treść błędu: ${response.errorBody()?.string()}")
                        println("Nagłówki wysłane: ${call.request().headers}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("Błąd połączenia: ${t.message}")
                }
            })

        } else {
            println("Plik JSON nie został znaleziony!")
        }
    }


    @SuppressLint("MissingInflatedId")
    fun getCurrentWifiCredentials(context: Context, callback: (String, String) -> Unit) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        var ssid = wifiInfo.ssid.removeSurrounding("\"")

        // Sprawdź, czy SSID jest pobrane; jeśli nie, pozostaw puste i poproś użytkownika o wprowadzenie
        if (ssid.isEmpty() || ssid == "<unknown ssid>") {
            ssid = "" // Ustawiamy SSID na pusty, aby wymusić wprowadzenie przez użytkownika
        }

        // Wyświetl dialog z prośbą o wprowadzenie SSID i hasła
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_wifi_credentials, null)
        val ssidInput = dialogView.findViewById<EditText>(R.id.ssidInput)
        val passwordInput = dialogView.findViewById<EditText>(R.id.passwordInput)

        // Ustaw domyślną wartość SSID, jeśli jest dostępna
        ssidInput.setText(ssid)

        AlertDialog.Builder(context).apply {
            setTitle("Podaj dane do WiFi")
            setView(dialogView)
            setPositiveButton("OK") { _, _ ->
                val enteredSsid = ssidInput.text.toString().trim()
                val password = passwordInput.text.toString()

                if (enteredSsid.isNotEmpty() && password.isNotEmpty()) {
                    callback(enteredSsid, password)
                } else {
                    Toast.makeText(context, "SSID i hasło nie mogą być puste.", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Anuluj", null)
            create()
        }.show()
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

//    protected fun sendHealthData(gender: String, age: Int, weight: Float, height: Float, bmr: Float, tdee: Float) {
//        // Pobierz userId z SharedPreferences
//        val userId = getUserID() ?: run {
//            Log.e("sendHealthData", "Brak userId w SharedPreferences!")
//            return
//        }
//
//        // Tworzymy instancję ApiClient
//        val apiClient = ApiClient(this)
//        val apiService = apiClient.getApiService8000()
//
//        // Tworzymy obiekt z danymi do wysłania
//        val healthDataRequest = HealthDataRequest(
//            userId = userId,
//            gender = gender,
//            age = age,
//            weight = weight,
//            height = height,
//            bmr = bmr,
//            tdee = tdee
//        )
//
//        // Wysyłamy dane do endpointu
//        val call = apiService.sendHealthData(healthDataRequest)
//        call.enqueue(object : retrofit2.Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
//                if (response.isSuccessful) {
//                    Log.d("sendHealthData", "Dane zostały pomyślnie wysłane!")
//                    Log.d("sendHealthData", "Kod odpowiedzi: ${response.code()}")
//                    Log.d("sendHealthData", "Treść odpowiedzi: ${response.body()?.string()}")
//                } else {
//                    Log.e("sendHealthData", "Błąd podczas wysyłania danych. Kod odpowiedzi: ${response.code()}")
//                    Log.e("sendHealthData", "Treść błędu: ${response.errorBody()?.string()}")
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                Log.e("sendHealthData", "Błąd połączenia: ${t.message}")
//            }
//        })
//    }

    @SuppressLint("MissingPermission")
    private fun sendPrivateKeyToBand() {
        val privateKey = getPrivateKey() ?: run {
            Log.e("BLE", "Private key is null or not available.")
            return
        }

        // Pobierz serwis
        val service = bluetoothGatt?.getService(serviceUUID)
        if (service == null) {
            Log.e("BLE", "Service not found for UUID: $serviceUUID")
        }

        // Pobierz charakterystykę klucza prywatnego
        val privateKeyCharacteristic = service?.getCharacteristic(privateKeyUUID)
        if (privateKeyCharacteristic == null) {
            Log.e("BLE", "Characteristic for private key not found for UUID: $privateKeyUUID")
        }

        // Sprawdź uprawnienia Bluetooth
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLE", "Permission BLUETOOTH_CONNECT is not granted.")
            return
        }

        // Ustaw wartość jako tekst UTF-8
        privateKeyCharacteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        privateKeyCharacteristic?.setValue(privateKey)  // Ustaw wartość bezpośrednio jako String

        // Próbuj zapisać wartość charakterystyki
        val success = bluetoothGatt?.writeCharacteristic(privateKeyCharacteristic) == true
        if (success) {
            Log.d("BLE", "Private key sent successfully to band as UTF-8 text.")
            provisioningComplete = true
        } else {
            Log.e("BLE", "Failed to send private key to band.")
        }
    }





}



data class HealthDataRequest(
    val userId: String,
    val gender: String,
    val age: Int,
    val weight: Float,
    val height: Float,
    val bmr: Float,
    val tdee: Float
)
