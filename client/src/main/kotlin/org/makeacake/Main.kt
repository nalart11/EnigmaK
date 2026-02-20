package org.makeacake

import org.json.JSONException
import org.json.JSONObject
import org.makeacake.util.Encryption

object Main {
    lateinit var connection: ServerConnection
    lateinit var config: JSONObject

    @JvmStatic
    fun main(args: Array<String>) {
        Gui.init()
        Gui.showWelcomeMessage()

        try {
            Config.init()
            config = Config.config!!
        } catch (e: JSONException) {
            Gui.showNewMessage("An error occurred while reading the config!", Gui.MessageType.SYSTEM_ERROR)
            Gui.breakInput()
        }

        try {
            Encryption.initKey()
        } catch (e: Encryption.EncryptionException) {
            Encryption.showNullKeyErrorAndGenerateNewOne()
        } catch (e: IllegalArgumentException) {
            Encryption.showIncorrectKeyError()
        }

        try {
            Encryption.initEncryption()
        } catch (e: Exception) {
            Encryption.showIncorrectKeyError()
        }

        /*
        Trying to connect to the server
         */
        try {
            connection = ServerConnection(
                config.getString("server-address"),
                config.getInt("server-port")
            )
        } catch (e: java.lang.Exception) {
            Gui.showNewMessage("Failed connect to the server!", Gui.MessageType.SYSTEM_ERROR)
            Gui.breakInput()
        }
        Gui.show()

        DataParser.handleOutputSession(true)
        Runtime.getRuntime().addShutdownHook(Thread(Runnable { DataParser.handleOutputSession(false) }))
    }

    fun handleServerShutdown() {
        Gui.showNewMessage("The server has shut down!", Gui.MessageType.SYSTEM_ERROR)
        Gui.breakInput()
    }
}