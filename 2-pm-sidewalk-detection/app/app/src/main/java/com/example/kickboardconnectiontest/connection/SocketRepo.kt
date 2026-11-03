package com.example.kickboardconnectiontest.connection

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

class SocketRepo
{
    // OUTPUT FOR UI STATE
    val isSocketCreated = MutableStateFlow<Boolean>(false)

    // PROPERTY FOR SOCKET COMMUNICATION
    // Socket for TCP communication
    private var socket: Socket? = null

    fun initSocket() {
        // android.os.NetworkOnMainThreadException
        CoroutineScope(Dispatchers.IO).launch{
            socket = Socket().apply {
                reuseAddress = true
                bind(InetSocketAddress(61017))
                connect(InetSocketAddress("server.example.com", 12345)) // 시연 때 쓴 TCP 터널 주소는 지움
            }
            while(socket?.isConnected == true){
                delay(10)
            }
            isSocketCreated.value = true
        }
    }

    suspend fun sendToTCPServer(inpValue: String) = withContext(Dispatchers.IO){
            try {
                socket?.getOutputStream()?.write(
                    inpValue.toByteArray()
                )
            }
            catch (ioe: IOException){
                Log.e(TAG, "sendToTCP I/O E.")
            }
    }

    fun closeSocket(){
        socket?.close()
        isSocketCreated.value = false
    }
    companion object{
        private const val TAG = "SocketConnection"
    }
}