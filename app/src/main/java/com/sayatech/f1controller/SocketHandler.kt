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
    private lateinit var orientationClient: Socket
    private var status = ConnectionStatus.NOT_CONNECTED
    private lateinit var printWriter: PrintWriter
    private lateinit var printWriterOrientation: PrintWriter
    suspend fun connect(): Boolean {
        return if (status == ConnectionStatus.CONNECTED) {
            true
        } else {
            try {
                client = withContext(Dispatchers.IO) {
                    Socket(IP, PORT)
                }
                orientationClient = withContext(Dispatchers.IO) {
                    Socket(IP, PORT)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun sendMessage(data: String, messageType: MessageType) {
        GlobalScope.launch(Dispatchers.IO) {
            if (messageType == MessageType.BUTTON) {
                printWriter = PrintWriter(client.getOutputStream(), true)
                printWriter.write(data)
                printWriter.flush()
            } else {
                printWriterOrientation = PrintWriter(orientationClient.getOutputStream(), true)
                printWriterOrientation.write(data)
                printWriterOrientation.flush()
            }
        }
    }

    fun getStatus() : ConnectionStatus {
        return status
    }

    fun disconnect() {
        runBlocking {
            launch(Dispatchers.IO) {
                printWriter.write(DISCONNECT_MESSAGE)
                printWriter.flush()
                printWriter.close()

                printWriterOrientation.write(DISCONNECT_MESSAGE)
                printWriterOrientation.flush()
                printWriterOrientation.close()
            }
        }
        status = ConnectionStatus.NOT_CONNECTED
        if (this::printWriter.isInitialized) {
            printWriter.close()
        } else if (this::printWriterOrientation.isInitialized) {
            printWriterOrientation.close()
        }
        client.close()
        orientationClient.close()
    }

    fun setStatus(c: ConnectionStatus) {
        status = c
    }

    fun buttonPress(id: Int, state: Boolean) {
        sendMessage("$BUTTON_KEY,$id,${if (state) 1 else 0}", MessageType.BUTTON)
    }

    fun orientationChange(d: Double) {
        sendMessage("$ORIENTATION_KEY,$d,", MessageType.ORIENTATION)
    }
}

enum class ConnectionStatus {
    CONNECTED,
    NOT_CONNECTED
}

enum class MessageType {
    BUTTON,
    ORIENTATION
}