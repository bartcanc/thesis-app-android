package com.example.thesisapp
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.bluetooth.*
//import android.content.Context
//import android.content.pm.PackageManager
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.widget.Toast
//import androidx.core.content.ContextCompat
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//import java.util.*
//
//class BluetoothManager(private val context: Context, private val callback: BluetoothCallback) {
//
//    interface BluetoothCallback {
//        fun onDataReceived(data: ByteArray)
//        fun onConnectionStateChange(isConnected: Boolean)
//        fun onCharacteristicRead(uuid: UUID, value: Any)
//    }
//
//    private val bluetoothAdapter: BluetoothAdapter? = (context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager).adapter
//    private var bluetoothGatt: BluetoothGatt? = null
//
//    // UUIDs for services and characteristics
//    private val serviceUUID = UUID.fromString("12345678-1234-1234-1234-123456789113")
//    private val messageTransferUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130013")
//    private val confirmationUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130006")
//    private val timeSyncUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130007")
//    private val batteryLevelUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130021")
//    private val firmwareVersionUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130022")
//    private val howManyFilesUUID = UUID.fromString("e2e3f5a4-8c4f-11eb-8dcd-0242ac130023")
//
//    private val characteristicsUUIDs = listOf(batteryLevelUUID, firmwareVersionUUID, howManyFilesUUID)
//    private var currentCharacteristicIndex = 0
//    private var readLoopActive = false
//    private var bufferSize = 108
//    private var dataBuffer = ByteArray(bufferSize)
//    private var bufferIndex = 0
//    private var fileSize = -1
//    private var currentByteCount = 0
//    private var isFileSizeRead = false
//    private var unixTimestamp: Long = 0L
//    private var isTimestampReceived = false
//    private var zeroFlag = false
//
//    private val gattCallback = object : BluetoothGattCallback() {
//
//        @SuppressLint("MissingPermission")
//        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                Log.d("BluetoothManager", "Connected to GATT server.")
//                callback.onConnectionStateChange(true)
//                gatt.requestMtu(256)
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                Log.d("BluetoothManager", "Disconnected from GATT server.")
//                callback.onConnectionStateChange(false)
//            }
//        }
//
//        @SuppressLint("MissingPermission")
//        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                gatt.discoverServices()
//            }
//        }
//
//        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                startSequentialRead()
//            }
//        }
//
//        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
//            if (characteristic.uuid == messageTransferUUID) {
//                var receivedData = characteristic.value
//                processReceivedData(receivedData)
//            }
//        }
//
//        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                val value = when (characteristic.uuid) {
//                    batteryLevelUUID -> characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
//                    firmwareVersionUUID -> characteristic.getStringValue(0)
//                    howManyFilesUUID -> characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0)
//                    else -> null
//                }
//                callback.onCharacteristicRead(characteristic.uuid, value ?: "Unknown value")
//            }
//        }
//    }
//
//    fun connectToDevice(deviceName: String) {
//        val pairedDevices = bluetoothAdapter?.bondedDevices ?: return
//        val device = pairedDevices.firstOrNull { it.name == deviceName }
//
//        if (device != null && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
//            bluetoothGatt = device.connectGatt(context, false, gattCallback)
//        } else {
//            Toast.makeText(context, "Device not found or missing permissions", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    fun disconnect() {
//        bluetoothGatt?.disconnect()
//        bluetoothGatt = null
//    }
//
//    private fun startSequentialRead() {
//        currentCharacteristicIndex = 0
//        readNextCharacteristic()
//    }
//
//    private fun readNextCharacteristic() {
//        if (currentCharacteristicIndex < characteristicsUUIDs.size) {
//            val uuid = characteristicsUUIDs[currentCharacteristicIndex]
//            val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(uuid)
//
//            if (characteristic != null && ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
//                bluetoothGatt?.readCharacteristic(characteristic)
//            } else {
//                currentCharacteristicIndex++
//                readNextCharacteristic()
//            }
//        }
//    }
//
//    private fun processReceivedData(receivedData: ByteArray) {
//        // Process received data similar to the initial code
//        if (!isFileSizeRead && receivedData.size >= 4) {
//            val byteBuffer = ByteBuffer.wrap(receivedData.copyOfRange(0, 4)).order(ByteOrder.LITTLE_ENDIAN)
//            fileSize = byteBuffer.int
//            isFileSizeRead = true
//            Log.d("BluetoothManager", "File size: $fileSize bytes")
//            addToBuffer(receivedData.copyOfRange(4, receivedData.size))
//        } else if (!isTimestampReceived && receivedData.size >= 4) {
//            val byteBuffer = ByteBuffer.wrap(receivedData.copyOfRange(0, 4)).order(ByteOrder.LITTLE_ENDIAN)
//            unixTimestamp = byteBuffer.int.toLong()
//            isTimestampReceived = true
//            currentByteCount += 4
//            Log.d("BluetoothManager", "Unix timestamp: $unixTimestamp")
//            addToBuffer(receivedData.copyOfRange(4, receivedData.size))
//        } else {
//            addToBuffer(receivedData)
//        }
//    }
//
//    private fun addToBuffer(data: ByteArray) {
//        var dataIndex = 0
//
//        while (dataIndex < data.size) {
//            val spaceLeftInBuffer = bufferSize - bufferIndex
//            val bytesToCopy = Math.min(spaceLeftInBuffer, data.size - dataIndex)
//            System.arraycopy(data, dataIndex, dataBuffer, bufferIndex, bytesToCopy)
//            bufferIndex += bytesToCopy
//            dataIndex += bytesToCopy
//            currentByteCount += bytesToCopy
//
//            if (bufferIndex == bufferSize) {
//                callback.onDataReceived(dataBuffer)
//                bufferIndex = 0
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    fun sendMessageOK() {
//        val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(confirmationUUID)
//        characteristic?.value = "OK".toByteArray(Charsets.UTF_8)
//        bluetoothGatt?.writeCharacteristic(characteristic)
//    }
//
//    @SuppressLint("MissingPermission")
//    fun sendUnixTime() {
//        val characteristic = bluetoothGatt?.getService(serviceUUID)?.getCharacteristic(timeSyncUUID)
//        val unixTime = System.currentTimeMillis() / 1000
//        val unixTimeBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(unixTime.toInt()).array()
//        characteristic?.value = unixTimeBytes
//        bluetoothGatt?.writeCharacteristic(characteristic)
//    }
//}
