package com.example.kickboardconnectiontest.connection

import android.content.Context
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.LinkedList
import java.util.Queue
import kotlin.properties.Delegates

const val PERMISSION_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION"
const val PERMISSION_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION"

const val STOP = 0
const val WALKING = 1
const val RIDING = 2
const val BEHAVIOR_STATE_BUFFER_SIZE = 4

class LocationRepo (
    private val context: Context,
    onValueChange: () -> Unit
){
    // OUTPUT VALUE of LocationRepo
    val currentSpeed = MutableStateFlow<Float>(0.0f)
    var averageSpeed= MutableStateFlow<Float>(0.0f)
    val speedQueue: Queue<Float> = LinkedList<Float>()
    var behaviorState: Int by Delegates.observable(STOP){ _, oldValue, newValue ->
        if(newValue != oldValue){
            onValueChange()
        }
    }

    // OUTPUT STATE of LocationRepo
    val isUpdateRunning = MutableStateFlow(false)
    val isLocationProviderInitialized = MutableStateFlow(false)

    // PROPERTIES
    lateinit var fusedLocationClient: FusedLocationProviderClient

    private var locationListener = LocationListener {
        if(it.hasSpeed()){
            currentSpeed.value = it.speed
            if(speedQueue.size >= BEHAVIOR_STATE_BUFFER_SIZE){
                speedQueue.remove()
            }

            speedQueue.add(currentSpeed.value)
            averageSpeed.value = speedQueue.average().toFloat()
            if(averageSpeed.value < 0.91f) behaviorState = STOP
            else if(
                0.91f <= averageSpeed.value
                &&
                averageSpeed.value < 1.6f) behaviorState = WALKING
            else behaviorState = RIDING
        }
        else{
            currentSpeed.value = 0.0f
        }
    }

    private var locationUpdateConfigure = LocationRequest
        .Builder(Priority.PRIORITY_HIGH_ACCURACY, 500).setMaxUpdateAgeMillis(2000)
        .build()


    fun getLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        isLocationProviderInitialized.value = true
    }

    @RequiresPermission(allOf = [PERMISSION_COARSE_LOCATION, PERMISSION_FINE_LOCATION])
    fun startLocationUpdates(){
        fusedLocationClient.requestLocationUpdates(
            locationUpdateConfigure,
            locationListener,
            Looper.getMainLooper()
        )
        isUpdateRunning.value = true
    }
    fun stopLocationUpdates(){
        fusedLocationClient.removeLocationUpdates(locationListener)
        isUpdateRunning.value = false
        currentSpeed.value = 0.0f
        averageSpeed.value = 0.0f
        speedQueue.clear()
    }
}