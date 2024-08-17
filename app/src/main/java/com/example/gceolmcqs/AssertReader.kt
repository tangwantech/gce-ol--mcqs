package com.example.gceolmcqs

import android.content.Context
import java.io.IOException
import java.nio.charset.Charset

class AssertReader {
    companion object{
        fun getJsonFromAssets(context: Context, fileName: String): String? {
            lateinit var json: String
            val charset: Charset = Charsets.UTF_8

            try {
                val jsonFile = context.assets.open(fileName)
                val size = jsonFile.available()
                val buffer = ByteArray(size)

                jsonFile.read(buffer)
                jsonFile.close()
                json = String(buffer, charset)

            } catch (e: IOException) {
                return null
            }
            return json
        }
    }
}