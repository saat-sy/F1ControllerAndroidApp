package com.sayatech.f1controller

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.net.Socket

@OptIn(DelicateCoroutinesApi::class)
class SocketHandler(
    private val ip: String
) {
    fun connect() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val client = Socket(ip, PORT)
                val printWriter = PrintWriter(client.getOutputStream(), true)
                printWriter.write("A")
                printWriter.flush();
                printWriter.close();
                // closing the connection
                client.close();
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}