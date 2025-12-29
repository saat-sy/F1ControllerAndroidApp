package com.sayatech.f1controller

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

@OptIn(DelicateCoroutinesApi::class)
class SocketHandler(
    private val IP: String
) {

    private lateinit var client: DatagramSocket
    private var status = ConnectionStatus.NOT_CONNECTED
    private lateinit var receiverAddress: InetAddress

    suspend fun connect(): Boolean {
        return if (status == ConnectionStatus.CONNECTED) {
            true
        } else {
            try {
                client = withContext(Dispatchers.IO) {
                    DatagramSocket(PORT)
                }
                receiverAddress =
                    withContext(Dispatchers.IO) {
                        InetAddress.getByName(IP)
                    }
                status = ConnectionStatus.CONNECTED
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun sendMessage(data: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val buffer = data.toByteArray()
            val packet = DatagramPacket(
                buffer,
                buffer.size,
                receiverAddress,
                PORT
            )
            try {
                client.send(packet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getStatus() : ConnectionStatus {
        return status
    }

    fun setStatus(c: ConnectionStatus) {
        status = c
    }

    fun buttonPress(id: Int, state: Boolean) {
        sendMessage("$BUTTON_KEY,$id,${if (state) 1 else 0}")
    }

    fun orientationChange(d: Double, a: Float) {
        sendMessage("$ORIENTATION_KEY,$d,$ACCELERATION_KEY,$a,")
    }
}

enum class ConnectionStatus {
    CONNECTED,
    NOT_CONNECTED
}