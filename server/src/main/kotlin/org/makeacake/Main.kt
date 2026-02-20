package org.makeacake
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.json.JSONObject
import org.makeacake.utils.ResourcesReader
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.function.Consumer
import kotlin.properties.Delegates
import kotlin.system.exitProcess

object Main {
    lateinit var server: ServerSocket
    var users: ArrayList<UserHandler> = ArrayList()
    lateinit var config: JSONObject

    var port by Delegates.notNull<Int>()
    val logger: Logger = LogManager.getLogger(Main::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            Config.init()
            config = Config.config ?: return logger.error("org.makeacake.Config could not be initialized")
            port = config.optInt("server-port", 6667)
        } catch(e: Exception) {
            logger.error("An error occurred while initializing config (%s)".format(e.message))
            exitProcess(0)
        }

        try {
            initServer(port)
        } catch(e: Exception) {
            logger.error("An error occurred while initializing the server (%s)".format(e.message))
            exitProcess(0)
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info("Sending a server shutdown signal to clients...")
            users.forEach{ user ->
                user.sendOutMessage(ResourcesReader("message_templates/server_shutdown.json").readString())
            }
            logger.info("Server shutdown signal sent successfully!")
            logger.info("Shutdown server...")
        })

        while (true) {
            val user: Socket? = server.accept()
            logger.debug("New user accepted. Trying to init org.makeacake.UserHandler.")
            val userHandler = UserHandler(user!!)
            logger.debug("org.makeacake.UserHandler initialized successfully. Start processing thread...")
            userHandler.start()
            users.add(userHandler)
            logger.debug("Thread started. User added to user-list")
            logger.info("New user handled successfully!")
        }
    }

    @Throws(IOException::class)
    fun initServer(port: Int) {
        logger.info("Initializing server on port %s...".format(port))
        server = ServerSocket(port)
        logger.info("Server initialized successfully!")
    }

    fun handleUserMessage(message: String?) {
        if (message.isNullOrEmpty()) return
        logger.debug("Trying to handle user message.")
        users.forEach(Consumer { user: UserHandler? -> user!!.sendOutMessage(message) })
        logger.info("User message handled successfully.")
    }

    fun isQuitMessage(message: String?): Boolean {
        if (message == null) return false

        val msg = JSONObject(message)

        if (msg.optString("type") != "user-session") return false
        return msg.getJSONObject("content").getString("status") == "leave"
    }

    fun disconnectUser(userHandler: UserHandler) {
        logger.info("Received user-leave message, disconnecting user...")
        users.remove(userHandler)
        userHandler.disconnect()
        userHandler.interrupt();
        logger.info("User disconnected successfully.")
    }
}