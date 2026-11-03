package com.example.kickboardconnectiontest.connection

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.PHY_LE_1M
import android.bluetooth.BluetoothDevice.TRANSPORT_LE
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.LinkedList
import java.util.Queue
import java.util.UUID
import kotlin.properties.Delegates

// These fields are marked as API >= 31 in the Manifest class, so we can't use those without warning.
// So we create our own, which prevents over-suppression of the Linter
const val PERMISSION_BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN"
const val PERMISSION_BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT"

const val ROAD_STATE_BUFFER_SIZE = 12

val PENPOINT_SERVICE_UUID: UUID = UUID.fromString("e95dd91d-251d-470a-a062-fa1922dfa9a8")
val PENPOINT_CHARACTERISTIC_UUID: UUID = UUID.fromString("e95d93ee-251d-470a-a062-fa1922dfa9a8")
val DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")


@Suppress("DEPRECATION")
class BLERepo @RequiresPermission("PERMISSION_BLUETOOTH_CONNECT") constructor(
    private val context: Context,
    onValueChange: () -> Unit
) {
    // DEBUG FRIEND
    companion object {
        const val TAG = "ConnectionRepo"
    }

    // OUTPUT of ConnectionRepo as UI State
    val isConnectSuccess = MutableStateFlow(false)
    val isSequenceRunning = MutableStateFlow(false)
    val inferenceResult = MutableStateFlow(-1)
    val inferenceReason = MutableStateFlow(JSONArray())
    var roadState: Int? by Delegates.observable(null){ _, oldValue, newValue ->
        if (newValue != null && (newValue != oldValue)){
            onValueChange()
        }
    }
    val inferenceResultQueue: Queue<Int> = LinkedList<Int>()

    // Bluetooth scanner
    private val bluetooth = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        ?: throw Exception("Bluetooth is not supported by this device.")
    private val scanner: BluetoothLeScanner
        get() = bluetooth.adapter.bluetoothLeScanner

    // Connected Device & Gatt Server
    private var bleTargetDevice: BluetoothDevice? = null
    private var bleGatt: BluetoothGatt? = null

    // INPUT of ConnectionRepo for ScanFilter & user classifier
    private val targetDeviceName = mutableStateOf<String?>(null)
    private val userName = mutableStateOf<String?>(null)



    // BLE Sequence control signal callbacks
    private val scanCallback = object : ScanCallback() {
        @RequiresPermission(allOf = [PERMISSION_BLUETOOTH_SCAN, PERMISSION_BLUETOOTH_CONNECT])
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            result ?: return // null filter
            if (result.isConnectable and (result.device.name == targetDeviceName.value)) {
                // 우리가 찾는 장치를 찾은 경우
                bleTargetDevice = result.device
                startConnectGatt(bleTargetDevice)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            // NOT INTEREST
        }

        override fun onScanFailed(errorCode: Int) {
            // 애초에 Scan을 시작도 못했을 때 호출됨
            super.onScanFailed(errorCode)
            throw Exception("Scan Start Failed.")
        }
    }

    private val connectCallback = object : BluetoothGattCallback() {
        @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            Log.d(TAG, "onConnectionStateChange Enter")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices() // Go to connectCallback.onServicesDiscovered
                Log.d(TAG, "gatt.discoverService is called")
            }
        }

        @Deprecated("Deprecated in Java")
        @RequiresPermission(PERMISSION_BLUETOOTH_CONNECT)
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val deviceService = gatt?.getService(PENPOINT_SERVICE_UUID)
                val devicePenPointNotificationCharacteristic =deviceService?.getCharacteristic(PENPOINT_CHARACTERISTIC_UUID)
                val setNotificationResult = gatt?.setCharacteristicNotification(
                    devicePenPointNotificationCharacteristic,
                    true
                ) ?: false // characteristic에 대한 notification 허용
                val descriptor =
                    devicePenPointNotificationCharacteristic?.getDescriptor(DESCRIPTOR_UUID)
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt?.writeDescriptor(descriptor)
                if (setNotificationResult) { // setNotify
                    isSequenceRunning.value = false
                    isConnectSuccess.value = true
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged( // is notified
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            if (characteristic.uuid == PENPOINT_CHARACTERISTIC_UUID) {
                Log.d(TAG, "onCharacteristicChanged Enter")
                val msgJSON = JSONObject(String(value))
                inferenceReason.value = msgJSON.getJSONArray("reason")
                inferenceResult.value = msgJSON.getInt("result")

                if(inferenceResultQueue.size >= BEHAVIOR_STATE_BUFFER_SIZE){
                    inferenceResultQueue.remove()
                }

                inferenceResultQueue.add(inferenceResult.value)
                if(inferenceResultQueue.all{ it == inferenceResultQueue.peek()}){
                    roadState = inferenceResultQueue.peek()
                }
            }
        }
    }

    @RequiresPermission(allOf = [PERMISSION_BLUETOOTH_SCAN, PERMISSION_BLUETOOTH_CONNECT])
    fun startBLESequence(deviceName: String, nickName: String) {
        if (deviceName.isNotEmpty() and nickName.isNotEmpty()) {
            targetDeviceName.value = deviceName
            userName.value = nickName
            isSequenceRunning.value = true
            scanner.startScan( // Go to scanCallback.onScanResult
                List(1) {
                    ScanFilter.Builder().setDeviceName(targetDeviceName.value).build()
                },
                ScanSettings.Builder().setPhy(PHY_LE_1M)
                    .setNumOfMatches(MATCH_NUM_ONE_ADVERTISEMENT).build(),
                scanCallback
            )
        }
    }

    @RequiresPermission(allOf = [PERMISSION_BLUETOOTH_SCAN, PERMISSION_BLUETOOTH_CONNECT])
    private fun startConnectGatt(foundDevice: BluetoothDevice?) {
        // bleGatt receive
        scanner.stopScan(scanCallback)
        bleGatt = foundDevice?.connectGatt(context, false, connectCallback, TRANSPORT_LE, PHY_LE_1M)
    }

    @RequiresPermission(allOf = [PERMISSION_BLUETOOTH_SCAN, PERMISSION_BLUETOOTH_CONNECT])
    fun stopBLESequence() {
        // 그 다음에 뭐 해야 하더라...
        // 사실상 Repo의 대다수 값을 초기화하긴 해야 하는데
        // scanner stop
        scanner.stopScan(scanCallback)

        // stopNotify
        val characteristicNotify = bleGatt?.services?.find { service -> service.uuid == PENPOINT_SERVICE_UUID }
            ?.getCharacteristic(PENPOINT_CHARACTERISTIC_UUID)

        bleGatt?.setCharacteristicNotification( characteristicNotify, false )
        val descriptor = characteristicNotify?.getDescriptor(DESCRIPTOR_UUID)
        descriptor?.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        bleGatt?.writeDescriptor(descriptor)

        // Gatt connect closing
        bleGatt?.disconnect()
        bleGatt?.close()
        bleGatt = null

        // ble device closing
        bleTargetDevice = null

        // uiState element init
        isSequenceRunning.value = false
        isConnectSuccess.value = false

        // OUTPUT VALUE Init.
        roadState = null
        inferenceResultQueue.clear()
    }
}