package com.example.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val latestVersion: Int,
    val apkUrl: String
)

object AppUpdateChecker {

    const val DEFAULT_VERSION_JSON_URL =
        "https://raw.githubusercontent.com/aliadeeb765-eng/hay-alworood-app/main/version.json"

    private val LATEST_VERSION_REGEX = Regex(""""latestVersion"\s*:\s*(-?\d+)""")
    private val APK_URL_REGEX = Regex(""""apkUrl"\s*:\s*"([^"]+)"""")

    fun parseVersionJson(jsonText: String): AppUpdateInfo? {
        val trimmed = jsonText.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return null
        val versionMatch = LATEST_VERSION_REGEX.find(trimmed) ?: return null
        val urlMatch = APK_URL_REGEX.find(trimmed) ?: return null
        val latestVersion = versionMatch.groupValues[1].toIntOrNull() ?: return null
        val apkUrl = urlMatch.groupValues[1].trim()
        return if (latestVersion > 0 && apkUrl.startsWith("https://")) {
            AppUpdateInfo(latestVersion = latestVersion, apkUrl = apkUrl)
        } else {
            null
        }
    }

    fun isUpdateAvailable(
        remoteInfo: AppUpdateInfo,
        currentVersionCode: Int = BuildConfig.VERSION_CODE
    ): Boolean = remoteInfo.latestVersion > currentVersionCode

    suspend fun checkForUpdate(
        versionJsonUrl: String = DEFAULT_VERSION_JSON_URL,
        currentVersionCode: Int = BuildConfig.VERSION_CODE
    ): AppUpdateInfo? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(versionJsonUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("Accept", "application/json")
                useCaches = false
            }
            val code = connection.responseCode
            if (code !in 200..299) return@withContext null

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val parsed = parseVersionJson(body) ?: return@withContext null
            if (isUpdateAvailable(parsed, currentVersionCode)) parsed else null
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    fun openUpdateDownloadUrl(context: Context, apkUrl: String) {
        if (!apkUrl.startsWith("https://")) {
            Toast.makeText(context, "رابط التحديث غير صالح", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "تعذّر فتح المتصفح لتحميل التحديث", Toast.LENGTH_SHORT).show()
        }
    }
}
