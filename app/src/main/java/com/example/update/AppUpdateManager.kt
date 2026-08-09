package com.example.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AppUpdateManager(
    private val repoOwner: String = "nahid6714",
    private val repoName: String = "tools"
) {

    companion object {
        const val UPDATE_JSON_URL = "https://raw.githubusercontent.com/nahid6714/tools/main/update.json"
        const val UPDATE_JSON_FALLBACK_URL = "https://raw.githubusercontent.com/nahid6714/tools/master/update.json"

        private const val PREFS_NAME = "app_update_checker_prefs"
        private const val KEY_LAST_CHECK_TIME = "last_check_time"
        private const val KEY_CACHED_JSON = "cached_json"

        // Minimum time interval between automated background checks (5 minutes)
        private const val CACHE_EXPIRATION_MS = 5 * 60 * 1000L
        // Minimum time interval between manual checks to avoid spamming network (3 seconds)
        private const val MIN_CHECK_INTERVAL_MS = 3 * 1000L

        private val checkMutex = Mutex()
    }

    /**
     * Checks for updates by fetching the static update.json file.
     * Completely bypasses GitHub REST API to avoid rate limits and HTTP 403 errors.
     * Silent fallback on network/parsing failure - will never throw or crash the UI.
     */
    suspend fun checkForUpdate(context: Context, forceCheck: Boolean = true): UpdateInfo = withContext(Dispatchers.IO) {
        val currentVersionName = BuildConfig.VERSION_NAME
        val currentVersionCode = BuildConfig.VERSION_CODE

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheckTime = prefs.getLong(KEY_LAST_CHECK_TIME, 0L)
        val now = System.currentTimeMillis()

        // Throttling: If checked very recently (within 3 seconds) or if cached & not forced
        if (!forceCheck && (now - lastCheckTime < CACHE_EXPIRATION_MS)) {
            val cachedJsonStr = prefs.getString(KEY_CACHED_JSON, null)
            if (!cachedJsonStr.isNullOrBlank()) {
                val cachedInfo = parseUpdateJson(cachedJsonStr, currentVersionName, currentVersionCode)
                if (cachedInfo != null) return@withContext cachedInfo
            }
        } else if (now - lastCheckTime < MIN_CHECK_INTERVAL_MS) {
            val cachedJsonStr = prefs.getString(KEY_CACHED_JSON, null)
            if (!cachedJsonStr.isNullOrBlank()) {
                val cachedInfo = parseUpdateJson(cachedJsonStr, currentVersionName, currentVersionCode)
                if (cachedInfo != null) return@withContext cachedInfo
            }
        }

        // Lock to avoid duplicate concurrent network requests
        checkMutex.withLock {
            try {
                var jsonStr: String? = fetchUrl(UPDATE_JSON_URL)
                if (jsonStr == null) {
                    jsonStr = fetchUrl(UPDATE_JSON_FALLBACK_URL)
                }

                if (jsonStr == null) {
                    return@withContext UpdateInfo(
                        hasUpdate = false,
                        currentVersionName = currentVersionName,
                        errorMessage = "আপডেট সার্ভারে এই মুহূর্তে সংযোগ করা যাচ্ছে না।"
                    )
                }

                val updateInfo = parseUpdateJson(jsonStr, currentVersionName, currentVersionCode)
                if (updateInfo != null) {
                    // Save to local cache
                    prefs.edit()
                        .putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis())
                        .putString(KEY_CACHED_JSON, jsonStr)
                        .apply()
                    return@withContext updateInfo
                } else {
                    return@withContext UpdateInfo(
                        hasUpdate = false,
                        currentVersionName = currentVersionName,
                        errorMessage = "আপডেট তথ্য প্রসেস করার সময় সমস্যা হয়েছে।"
                    )
                }
            } catch (e: java.net.UnknownHostException) {
                return@withContext UpdateInfo(
                    hasUpdate = false,
                    currentVersionName = currentVersionName,
                    errorMessage = "ইন্টারনেট সংযোগ নেই। পরে আবার চেষ্টা করুন।"
                )
            } catch (e: java.net.SocketTimeoutException) {
                return@withContext UpdateInfo(
                    hasUpdate = false,
                    currentVersionName = currentVersionName,
                    errorMessage = "আপডেট সার্ভারে এই মুহূর্তে সংযোগ করা যাচ্ছে না।"
                )
            } catch (e: Exception) {
                return@withContext UpdateInfo(
                    hasUpdate = false,
                    currentVersionName = currentVersionName,
                    errorMessage = "আপডেট চেক করার সময় সমস্যা হয়েছে: ${e.localizedMessage ?: "অজানা ত্রুটি"}"
                )
            }
        }
    }

    private fun fetchUrl(urlString: String): String? {
        return try {
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                useCaches = false
                defaultUseCaches = false
                setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                setRequestProperty("Pragma", "no-cache")
                setRequestProperty("Expires", "0")
                setRequestProperty("User-Agent", "AndroidAppUpdater")
            }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseUpdateJson(
        jsonStr: String,
        currentVersionName: String,
        currentVersionCode: Int
    ): UpdateInfo? {
        return try {
            val json = JSONObject(jsonStr)
            val remoteVersionCode = json.optInt("versionCode", 0)
            val remoteVersionName = json.optString("versionName", "").trim()
            val downloadUrl = json.optString("downloadUrl", "").trim()
            val rawNotes = json.optString("releaseNotes", "").trim()
            val cleanNotes = sanitizeReleaseNotes(rawNotes)

            if (remoteVersionCode <= 0 && remoteVersionName.isBlank()) {
                return null
            }

            val hasUpdate = remoteVersionCode > currentVersionCode

            UpdateInfo(
                hasUpdate = hasUpdate,
                latestVersionName = if (remoteVersionName.isNotBlank()) remoteVersionName else "v$remoteVersionCode",
                latestVersionCode = remoteVersionCode,
                releaseNotes = cleanNotes,
                downloadUrl = downloadUrl,
                currentVersionName = currentVersionName,
                publishedAt = "",
                apkSizeFormatted = ""
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun sanitizeReleaseNotes(rawBody: String): String {
        if (rawBody.isBlank()) {
            return "• অ্যাপের পারফরম্যান্স ও অভিজ্ঞতা উন্নত করা হয়েছে।\n• সাধারণ সমস্যা ও বাগ ফিক্স করা হয়েছে।"
        }

        val lines = rawBody.lines()
        val cleanList = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue

            val lower = trimmed.lowercase()
            if (lower.contains("commit:") ||
                lower.contains("message:") ||
                lower.contains("update appupdatemanager") ||
                lower.contains("update build.gradle") ||
                lower.contains("new release v") ||
                trimmed.matches(Regex("(?i).*([0-9a-f]{7,40}).*")) ||
                trimmed.startsWith("#")
            ) {
                continue
            }

            val cleanLine = trimmed
                .replace("**", "")
                .replace("*", "")
                .replace("`", "")
                .trim()

            if (cleanLine.isNotBlank()) {
                if (!cleanLine.startsWith("•") && !cleanLine.startsWith("-")) {
                    cleanList.add("• $cleanLine")
                } else {
                    cleanList.add(cleanLine)
                }
            }
        }

        if (cleanList.isEmpty()) {
            return "• অ্যাপের নতুন ফিচার ও বিভিন্ন উন্নতি অন্তর্ভুক্ত করা হয়েছে।\n• পারফরম্যান্স ও স্থায়িত্ব বৃদ্ধি করা হয়েছে।"
        }

        return cleanList.joinToString("\n")
    }

    /**
     * Downloads the release APK into the app's cache directory and updates progress state.
     */
    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Int) -> Unit,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            if (downloadUrl.isBlank()) {
                withContext(Dispatchers.Main) {
                    onError("ডাউনলোড লিংক পাওয়া যায়নি।")
                }
                return@withContext
            }

            var urlStr = downloadUrl
            var connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                setRequestProperty("User-Agent", "AndroidAppUpdater")
                instanceFollowRedirects = true
            }

            // Follow HTTP redirects (GitHub Releases redirect to AWS S3)
            var responseCode = connection.responseCode
            var redirectCount = 0
            while ((responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER) && redirectCount < 5) {
                val newUrl = connection.getHeaderField("Location")
                if (!newUrl.isNullOrEmpty()) {
                    urlStr = newUrl
                    connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15000
                        readTimeout = 30000
                        setRequestProperty("User-Agent", "AndroidAppUpdater")
                    }
                    responseCode = connection.responseCode
                    redirectCount++
                } else break
            }

            val fileLength = connection.contentLength
            val updateDir = File(context.cacheDir, "updates")
            if (!updateDir.exists()) updateDir.mkdirs()

            val apkFile = File(updateDir, "update.apk")
            if (apkFile.exists()) apkFile.delete()

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val data = ByteArray(8192)
                    var total: Long = 0
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count.toLong()
                        if (fileLength > 0) {
                            val progress = ((total * 100) / fileLength).toInt()
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                        output.write(data, 0, count)
                    }
                    output.flush()
                }
            }

            withContext(Dispatchers.Main) {
                onProgress(100)
                onSuccess(apkFile)
                installApk(context, apkFile)
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e.localizedMessage ?: "ডাউনলোড করতে ব্যর্থ হয়েছে। ইন্টারনেট কানেকশন চেক করুন।")
            }
        }
    }

    /**
     * Launches the Android package installer for the downloaded APK using FileProvider.
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
