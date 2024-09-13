package com.example.gceolmcqs

import android.content.Context
import java.io.IOException
import java.nio.charset.Charset

class AssertReader {
    companion object{
        fun getJsonFromAssets(context: Context, fileName: String): String? {
            lateinit var json: String

            try {
                json = context.assets.open(fileName).bufferedReader().use { it.readText() }

            } catch (e: IOException) {
                return null
            }
            return json
        }
    }
}