package com.ghostech.blehound

import android.content.Context
import android.util.Base64
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

internal object WigleUploader {

    private const val TAG        = "WigleUploader"
    private const val UPLOAD_URL = "https://api.wigle.net/api/v2/file/upload"
    private const val PREFS_NAME = "blehound_prefs"

    const val PREF_API_NAME  = "wigle_api_name"
    const val PREF_API_TOKEN = "wigle_api_token"

    fun hasCredentials(context: Context): Boolean {
        val p = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return p.getString(PREF_API_NAME, "").orEmpty().isNotBlank() &&
               p.getString(PREF_API_TOKEN, "").orEmpty().isNotBlank()
    }

    fun upload(context: Context, csvContent: String): Boolean {
        val p        = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val apiName  = p.getString(PREF_API_NAME, "").orEmpty()
        val apiToken = p.getString(PREF_API_TOKEN, "").orEmpty()
        if (apiName.isBlank() || apiToken.isBlank()) return false
        return doUpload(apiName, apiToken, csvContent)
    }

    private fun doUpload(apiName: String, apiToken: String, csv: String): Boolean {
        val boundary    = "----BLEHound${System.currentTimeMillis()}"
        val credentials = Base64.encodeToString(
            "$apiName:$apiToken".toByteArray(Charsets.UTF_8), Base64.NO_WRAP
        )
        val body = buildMultipart(boundary, csv)
        val conn = URL(UPLOAD_URL).openConnection() as HttpURLConnection
        return try {
            conn.requestMethod = "POST"
            conn.doOutput      = true
            conn.connectTimeout = 15_000
            conn.readTimeout    = 30_000
            conn.setRequestProperty("Authorization",  "Basic $credentials")
            conn.setRequestProperty("Content-Type",   "multipart/form-data; boundary=$boundary")
            conn.setRequestProperty("Content-Length", body.size.toString())
            conn.outputStream.use { it.write(body) }
            val code = conn.responseCode
            Log.i(TAG, "WiGLE upload HTTP $code")
            code in 200..299
        } catch (e: Exception) {
            Log.e(TAG, "WiGLE upload failed: ${e.message}")
            false
        } finally {
            conn.disconnect()
        }
    }

    private fun buildMultipart(boundary: String, csv: String): ByteArray = buildString {
        append("--$boundary\r\n")
        append("Content-Disposition: form-data; name=\"file\"; filename=\"blehound-wardrive.csv\"\r\n")
        append("Content-Type: text/csv\r\n")
        append("\r\n")
        append(csv)
        append("\r\n--$boundary--\r\n")
    }.toByteArray(Charsets.UTF_8)
}
