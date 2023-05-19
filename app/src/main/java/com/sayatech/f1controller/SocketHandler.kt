package com.sayatech.f1controller

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.net.Socket

@OptIn(DelicateCoroutinesApi::class)
class SocketHandler {

    private lateinit var client: Socket
    private var status = ConnectionStatus.NOT_CONNECTED
    private lateinit var printWriter: PrintWriter
    suspend fun connect(): Boolean {
        return if (status == ConnectionStatus.CONNECTED) {
            true
        } else {
            try {
                client = withContext(Dispatchers.IO) {
                    Socket(IP, PORT)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun sendMessage(data: String) {
        GlobalScope.launch(Dispatchers.IO) {
            printWriter = PrintWriter(client.getOutputStream(), true)

            printWriter.write(data)
            printWriter.flush()
        }
    }

    fun getStatus() : ConnectionStatus {
        return status
    }

    fun disconnect() {
        runBlocking {
            launch(Dispatchers.IO) {
                val printWriter = PrintWriter(client.getOutputStream(), true)
                printWriter.write(DISCONNECT_MESSAGE)
                printWriter.flush()
                printWriter.close()
            }
        }
        status = ConnectionStatus.NOT_CONNECTED
        if (this::printWriter.isInitialized) {
            printWriter.close()
        }
        client.close()
    }

    fun setStatus(c: ConnectionStatus) {
        status = c
    }

    fun buttonPress(id: Int, state: Boolean) {
//        println("$id $state")
        sendMessage("$BUTTON_KEY,$id,${if (state) 1 else 0}")
    }
}

enum class ConnectionStatus {
    CONNECTED,
    NOT_CONNECTED
}