package org.makeacake.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Class for reading a "resources" file
 */
class ResourcesReader(resourcesFilePath: String?) {
    private val reader: BufferedReader

    init {
        try {
            val inStream = javaClass.getClassLoader().getResourceAsStream(resourcesFilePath)
            reader = BufferedReader(InputStreamReader(inStream))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Read file as String. Returns empty string if something went wrong
     * @return File contents
     */
    fun readString(): String {
        val builder = StringBuilder()
        var line: String?
        try {
            while ((reader.readLine().also { line = it }) != null) {
                builder.append(line)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return builder.toString()
    }
}