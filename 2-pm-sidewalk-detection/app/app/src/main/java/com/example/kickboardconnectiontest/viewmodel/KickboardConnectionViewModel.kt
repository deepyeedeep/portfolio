package com.example.kickboardconnectiontest.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kickboardconnectiontest.connection.BLERepo
import com.example.kickboardconnectiontest.connection.LocationRepo
import com.example.kickboardconnectiontest.connection.PERMISSION_BLUETOOTH_CONNECT
import com.example.kickboardconnectiontest.connection.PERMISSION_BLUETOOTH_SCAN
import com.example.kickboardconnectiontest.connection.PERMISSION_COARSE_LOCATION
import com.example.kickboardconnectiontest.connection.PERMISSION_FINE_LOCATION
import com.example.kickboardconnectiontest.connection.RIDING
import com.example.kickboardconnectiontest.connection.STOP
import com.example.kickboardconnectiontest.connection.SocketRepo
import com.example.kickboardconnectiontest.connection.WALKING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class KickboardConnectUIState(
    // ReadyScreen <-> ConnectedScreen
    val isConnectionSuccessful: Boolean = false,
    val isInitialized: Boolean = false,
    val isSequenceRunning: Boolean = false, // Button Enable
    val debugingString: String = "",
)

@OptIn(ExperimentalCoroutinesApi::class)
class KickboardConnectionViewModel(
    private val application: Application
): AndroidViewModel(application) {

    @RequiresPermission("PERMISSION_BLUETOOTH_CONNECT")
    private val gattModel = BLERepo(application, this::onValueChanged) // BLE and Socket Connection
    private val locationModel = LocationRepo(application, this::onValueChanged) // FusedLocationProvider Connection
    private val socketModel = SocketRepo() // TCP Socket Communication Object

    val debugingString = MutableStateFlow("")

    /* VIEW CONTROL AND STATES*/
    // INPUT VALUE FROM UI and UPDATE
    val nickname = mutableStateOf("")
    val deviceNumber = mutableStateOf("")
    fun setNickName(nickName: String){ nickname.value = nickName }
    fun setDeviceName(deviceName: String){ deviceNumber.value = deviceName }


    private val _uiState = MutableStateFlow(KickboardConnectUIState())
    val uiState get() = _uiState
    init {
        viewModelScope.launch {

            gattModel.isConnectSuccess.collect {currentConnectionState ->
                _uiState.update {
                    it.copy(isConnectionSuccessful =  currentConnectionState)
                }
            }
        }
        viewModelScope.launch{
            gattModel.isSequenceRunning.collect { currentSequenceState ->
                _uiState.update {
                    it.copy(isSequenceRunning = currentSequenceState)
                }
            }
        }
        viewModelScope.launch{
            socketModel.isSocketCreated.collect { currentSocketExist ->
                _uiState.update {
                    it.copy(isInitialized = currentSocketExist && locationModel.isLocationProviderInitialized.value)
                }
            }
        }
        viewModelScope.launch{
            locationModel.isLocationProviderInitialized.collect { currentProviderExist ->
                _uiState.update {
                    it.copy(isInitialized = currentProviderExist && socketModel.isSocketCreated.value)
                }
            }
        }
        viewModelScope.launch {
            debugingString.collect { expString ->
                _uiState.update { it.copy(debugingString = expString) }
            }
        }
    }

    fun initializeConnectionObjects(){
        locationModel.getLocationClient()
        if(!socketModel.isSocketCreated.value){
            socketModel.initSocket()
        }
    }

    @RequiresPermission(
        allOf = [
            PERMISSION_BLUETOOTH_SCAN,
            PERMISSION_BLUETOOTH_CONNECT,
            PERMISSION_COARSE_LOCATION,
            PERMISSION_FINE_LOCATION
        ]
    )
    fun startDeviceConnect(){
        gattModel.startBLESequence(deviceNumber.value, nickname.value)
        locationModel.startLocationUpdates()
    }

    @RequiresPermission(allOf = [PERMISSION_BLUETOOTH_SCAN, PERMISSION_BLUETOOTH_CONNECT])
    fun stopDeviceConnect(){
        if (locationModel.behaviorState == STOP && gattModel.roadState == 1){
            sendToServer()
        }
        gattModel.stopBLESequence()
        locationModel.stopLocationUpdates()
        socketModel.closeSocket()
    }

    @RequiresPermission(
        allOf = [
            PERMISSION_COARSE_LOCATION,
            PERMISSION_FINE_LOCATION
        ]
    )
    fun onValueChanged(){
        val behaviorString = when(locationModel.behaviorState){
            STOP -> "STOP"
            WALKING -> "WALK"
            RIDING -> "RIDING"
            else -> "UNKNOWN"
        }
        val roadString = when(gattModel.roadState){
            0 -> "BIKELANE"
            1 -> "ROAD"
            2 -> "SIDEWALK"
            else -> "UNKNOWN"
        }

        viewModelScope.launch(Dispatchers.IO) {
            debugingString.value =
                "${roadString}, ${gattModel.inferenceReason.value},\n" +
                        "${behaviorString}, ${locationModel.averageSpeed.value}"

        }
        if (locationModel.behaviorState == RIDING && gattModel.roadState == 2){
            sendToServer()
        }
    }

    private fun sendToServer(){
        viewModelScope.launch(Dispatchers.IO) {
            val tcpJsonObject = JSONObject()
            val behaviorString = when(locationModel.behaviorState){
                STOP -> "STOP"
                WALKING -> "WALK"
                RIDING -> "RIDING"
                else -> "UNKNOWN"
            }
            val roadString = when(gattModel.roadState){
                0 -> "BIKELANE"
                1 -> "ROAD"
                2 -> "SIDEWALK"
                else -> "UNKNOWN"
            }
            val dataformat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            dataformat.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            tcpJsonObject.put("time", dataformat.format(Date(System.currentTimeMillis())))
            tcpJsonObject.put("name", nickname.value)
            tcpJsonObject.put("device_number", deviceNumber.value)
            tcpJsonObject.put("road_state", roadString)
            tcpJsonObject.put("reason", gattModel.inferenceReason.value)
            tcpJsonObject.put("moving", behaviorString)
            tcpJsonObject.put("avg_speed", locationModel.averageSpeed.value)
            socketModel.sendToTCPServer(tcpJsonObject.toString())
        }
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel이 kill 당했을 시 모든 활동 중지해야 함
        if(
            ActivityCompat.checkSelfPermission(
                getApplication(), android.Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED){
            gattModel.stopBLESequence()
            locationModel.stopLocationUpdates()
            socketModel.closeSocket()
        }
    }
}