package org.makeacake

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.json.JSONObject
import org.makeacake.utils.ResourcesReader
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Class for working with config
 */
object Config {
    /**
     * Get config (access method)
     * @return org.makeacake.Config
     */
    var config: JSONObject? = null
        private set

    private val logger: Logger = LogManager.getLogger(Config::class.java)

    @Throws(Exception::class)
    fun init() {
        logger.info("Trying to init config")

        val configFile = File("config.json")
        if (!configFile.exists()) {
            logger.error("Configuration file not found!")
            generateNewConfig()
            logger.info("Using default config")
            config = JSONObject(defaultConfig)
            return
        }

        config = JSONObject(readConfig())
    }

    /**
     * Generate new config.json file
     */
    @Throws(Exception::class)
    private fun generateNewConfig() {
        logger.debug("Generating new config file...")
        Files.writeString(Paths.get("config.json"), defaultConfig)
        logger.info("New config file generated successfully!")
    }

    @get:Throws(Exception::class)
    private val defaultConfig: String
        /**
         * Get default config from resources
         * @return org.makeacake.Config
         */
        get() = ResourcesReader("config.json").readString()

    /**
     * Get config from config.json file
     * @return org.makeacake.Config
     */
    @Throws(Exception::class)
    private fun readConfig(): String? {
        return Files.readString(Paths.get("config.json"))
    }
}