package org.makeacake

import org.json.JSONObject
import org.makeacake.util.ResourcesReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Class for working with config
 */
object Config {
    /**
     * Get config (access method)
     * @return Config
     */
    var config: JSONObject? = null
        private set

    @Throws(Exception::class)
    fun init() {
        val configFile = File("config.json")
        if (!configFile.exists()) {
            Gui.showNewMessage("Configuration file not found!", Gui.MessageType.SYSTEM_ERROR)
            Gui.showNewMessage(
                "A new configuration file has been generated. Set it up and restart the program",
                Gui.MessageType.SYSTEM_INFO
            )

            generateNewConfig()
            config = JSONObject(defaultConfig)

            Gui.breakInput() // This means stopping the program
            return
        }

        config = JSONObject(readConfig())
    }

    /**
     * Generate new config.json file
     */
    @Throws(Exception::class)
    private fun generateNewConfig() {
        Files.writeString(Paths.get("config.json"), defaultConfig)
    }

    @get:Throws(Exception::class)
    private val defaultConfig: String
        /**
         * Get default config from resources
         * @return Config
         */
        get() = ResourcesReader("config.json").readString()

    /**
     * Get config from config.json file
     * @return Config
     */
    @Throws(Exception::class)
    private fun readConfig(): String {
        return Files.readString(Paths.get("config.json"))
    }
}