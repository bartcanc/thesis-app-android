package com.example.thesisapp

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

open class BaseActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_BLUETOOTH_PERMISSIONS = 1
    }

    lateinit var sharedPref: SharedPreferences
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private val serviceUUID: UUID = UUID.fromString("12345678-1234-1234-1234-123456789113")
    private val messageTransferUUID: UUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130013")
    private val confirmationUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130006")
    private val timeSyncUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130007")

    private var readLoopActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("ThesisAppPreferences", MODE_PRIVATE)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        checkBluetoothPermissions()
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

    private fun connectToDevice(device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            bluetoothGatt = device.connectGatt(this, false, gattCallback)
        } else {
            Toast.makeText(this, "Brak uprawnień do połączenia Bluetooth.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            val targetDeviceName = "ESP32_Smartband_mini"

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
        } else {
            checkBluetoothPermissions()
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLE", "Connected to GATT server.")

                if (ContextCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    gatt.requestMtu(256)
                } else {
                    Toast.makeText(this@BaseActivity, "Brak uprawnień Bluetooth.", Toast.LENGTH_SHORT).show()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BLE", "Disconnected from GATT server.")
            }
        }

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

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val characteristic = gatt.getService(serviceUUID)?.getCharacteristic(messageTransferUUID)
                if (characteristic != null) {

                    if (ContextCompat.checkSelfPermission(this@BaseActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        gatt.setCharacteristicNotification(characteristic, true)

                        val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                        descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        gatt.writeDescriptor(descriptor)
                    } else {
                        Toast.makeText(this@BaseActivity, "Brak uprawnień Bluetooth.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.w("BLE", "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val receivedData = characteristic.value
            Log.d("BLE", "Otrzymano dane binarne: ${receivedData.size} bajtów")
            if (receivedData.all { it == 0.toByte() }) {
                Log.d("BLE", "Otrzymano 16 zer pod rząd, zakończenie transmisji.")
                readLoopActive = false
            }
            else{
                saveBinFile(receivedData)
            }
        }


        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if ((status == BluetoothGatt.GATT_SUCCESS) and (!readLoopActive)) {
                sendMessageOK()
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
                    handler.postDelayed(this, 100)
                }
            }
        })
    }

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

    private fun sendMessageOK() {
        val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(confirmationUUID)

        if (characteristic != null && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            == PackageManager.PERMISSION_GRANTED) {
            characteristic.value = "OK".toByteArray(Charsets.UTF_8)

            if (bluetoothGatt?.writeCharacteristic(characteristic) == true) {
                Log.d("BLE", "Wiadomość 'OK' została wysłana")
            } else {
                Log.e("BLE", "Nie udało się wysłać wiadomości 'OK'")
            }
        } else {
            Log.e("BLE", "Charakterystyka nie znaleziona lub brak uprawnień!")
        }
    }

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

    private fun saveBinFile(data: ByteArray) {
        val binFileName = "received_data.bin"
        val filePath = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), binFileName)

        try {
            FileOutputStream(filePath, true).use { outputStream ->
                outputStream.write(data)

                Log.d("BLE", "Plik binarny zapisany w: ${filePath.absolutePath}")
                runOnUiThread {
                    Toast.makeText(this, "Plik binarny zapisany", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            Log.e("BLE", "Błąd podczas zapisywania pliku binarnego", e)
            runOnUiThread {
                Toast.makeText(this, "Błąd zapisu pliku binarnego", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun createJSONFile() {
       TODO("dodac baze danych i z niej stworzyc plik json")
    }

    private fun checkBluetoothPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), REQUEST_BLUETOOTH_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, "Uprawnienia przyznane", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Brak uprawnień Bluetooth", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}