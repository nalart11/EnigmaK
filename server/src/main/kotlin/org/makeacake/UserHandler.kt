package org.makeacake

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

/**
 * A class that contains everything needed to work with a user,
 * including its socket, input/output paths, and message receiving loop.
 */
class UserHandler(private val socket: Socket) : Thread() {
    private val `in`: BufferedReader
    private val out: PrintWriter

    private var disconnected = false

    /**
     * Creating a new user
     * @param socket User socket
     */
    init {
        logger.debug("Initializing user I/O streams")
        `in` = BufferedReader(InputStreamReader(socket.getInputStream()))
        out = PrintWriter(socket.getOutputStream(), true)
        logger.debug("I/O streams initialized successfully")
    }

    /**
     * Cycle of receiving and processing user messages
     */
    override fun run() {
        while (!disconnected) {
            try {
                val inputMessage = `in`.readLine()
                Main.handleUserMessage(inputMessage)
                if (Main.isQuitMessage(inputMessage)) Main.disconnectUser(this)
            } catch (e: IOException) {
                logger.error("An error occurred while retrieving user message (%s)".format(e.message))
            } catch (e: ConcurrentModificationException) {
            } // Ignore this XD
        }
    }

    /**
     * Send message from server to user
     * @param message Message
     */
    fun sendOutMessage(message: String?) {
        out.println(message)
        logger.trace("Message sent to user")
    }

    fun disconnect() {
        try {
            `in`.close()
            out.close()
            socket.close()
            disconnected = true
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(UserHandler::class.java)
    }
}