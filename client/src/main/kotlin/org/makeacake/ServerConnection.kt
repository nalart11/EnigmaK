package org.makeacake

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

/**
 * Class for working with the server. Receiving and sending messages is done here.
 */
class ServerConnection(serverAddress: String?, serverPort: Int) : Thread() {
    private val server: Socket = Socket(serverAddress, serverPort)
    private val `in`: BufferedReader = BufferedReader(InputStreamReader(server.getInputStream()))
    private val out: PrintWriter = PrintWriter(server.getOutputStream(), true)

    init {
        this.start()
    }

    override fun run() {
        while (server.isConnected) {
            try {
                DataParser.handleInputData(`in`.readLine())
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    /**
     * Send information to the server
     * @param data Data
     */
    fun sendToServer(data: String?) {
        out.println(data)
    }
}