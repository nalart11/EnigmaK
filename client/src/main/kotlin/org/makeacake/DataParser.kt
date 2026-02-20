package org.makeacake

import org.json.JSONObject
import org.makeacake.util.Encryption
import org.makeacake.util.NotificationSound
import org.makeacake.util.ResourcesReader

/**
 * Class for working with data and processing it
 */
object DataParser {
    /**
     * Parse the incoming message and take the necessary action to work with it
     * @param data Raw data from server
     */
    fun handleInputData(data: String) {
        val dataObj: JSONObject?
        try {
            dataObj = JSONObject(data)
        } catch (e: Exception) {
            return
        } // Null message from server


        when (dataObj.getString("type")) {
            "user-message" -> handleUserMessage(dataObj.getJSONObject("content"))
            "user-session" -> handleUserSession(dataObj.getJSONObject("content"))
            "server-shutdown" -> Main.handleServerShutdown()
        }
    }

    /**
     * Collect a user message into a data type accepted by the client
     * @param message Message
     */
    fun handleOutputMessage(message: String?) {
        val template: String
        try {
            template = MessageType.USER_MESSAGE.template
        } catch (e: Exception) {
            Gui.showNewMessage(
                "There was an error sending the message (receiving template)",
                Gui.MessageType.SYSTEM_ERROR
            )
            e.printStackTrace()
            return
        }

        val encryptedMessage: String
        try {
            encryptedMessage = Encryption.encrypt(message!!)
        } catch (e: Exception) {
            Gui.showNewMessage("There was an error sending the message (encrypt process)", Gui.MessageType.SYSTEM_ERROR)
            e.printStackTrace()
            return
        }

        Main.connection.sendToServer(
            template
                .replace("%user%", Main.config.getString("username"))
                .replace("%message%", encryptedMessage)
        )
    }

    /**
     * Process and send a message about your session (join/leave)
     * @param isJoin Is join
     */
    fun handleOutputSession(isJoin: Boolean) {
        val status = if (isJoin) "join" else "leave"

        val template: String
        try {
            template = MessageType.USER_SESSION.template
        } catch (e: Exception) {
            Gui.showNewMessage(
                "There was an error sending the session status (receiving template)",
                Gui.MessageType.SYSTEM_ERROR
            )
            e.printStackTrace()
            return
        }

        Main.connection.sendToServer(
            template
                .replace("%user%", Main.config.getString("username"))
                .replace("%status%", status)
        )
    }

    /**
     * Processing an incoming user message
     * @param data Data's "content" object
     */
    private fun handleUserMessage(data: JSONObject) {
        val sender = data.getString("user")
        val encryptedMessage = data.getString("message") // In ftr, decrypt and handle decryption errors here

        val message: String?
        try {
            message = Encryption.decrypt(encryptedMessage)
        } catch (e: Exception) {
            Gui.showNewMessage(
                "Failed to decrypt the incoming message! (wrong encryption key)",
                Gui.MessageType.SYSTEM_ERROR
            )
            return
        }

        val formattedMessage: String = "%s -> %s".format(sender, message) // In ftr, handle timestamps here

        if (sender == Main.config.getString("username")) {
            Gui.showNewMessage(formattedMessage, Gui.MessageType.SELF_USER_MESSAGE)
        } else {
            Gui.showNewMessage(formattedMessage, Gui.MessageType.USER_MESSAGE)
        }
        Gui.scrollDown()
        if (Gui.isMinimized && Main.config.optBoolean("notification-sounds", true)) NotificationSound.play()
    }

    /**
     * Handle input user-session(join/leave) message and show it
     * @param data Data's "content" object
     */
    private fun handleUserSession(data: JSONObject) {
        val user = data.getString("user")
        val status = if (data.getString("status") == "join") "joined!" else "left."

        val formattedMessage: String = "%s %s".format(user, status)

        Gui.showNewMessage(formattedMessage, Gui.MessageType.USER_SESSION)
        Gui.scrollDown()
    }

    /**
     * Types of incoming and outgoing messages
     */
    enum class MessageType(fileName: String) {
        USER_MESSAGE("user_message"),  // User text messages
        USER_SESSION("user_session"); // Messages about user join/leave

        private val fileName: String?

        init {
            this.fileName = fileName
        }

        private val resourcesPath: String
            get() = "message_templates/%s.json".format(this.fileName)

        val template: String
            get() = ResourcesReader(this.resourcesPath).readString().replace("\n", "")
    }
}