package com.example.util

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ScannedMedicalItem(
    val patientId: String,
    val code: String
)

data class ScannedMedicalResult(
    val date: String,
    val items: List<ScannedMedicalItem>,
    val rawText: String = "",
    val errorMessage: String? = null
)

object MedicalImageOcrScanner {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    suspend fun scanImage(bitmap: Bitmap, apiKey: String = BuildConfig.GEMINI_API_KEY): ScannedMedicalResult = withContext(Dispatchers.IO) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Step 1: First extract raw text from image using local ML Kit Text Recognizer (100% on-device & accurate)
        val mlKitRawText = recognizeTextWithMlKit(bitmap)

        // Step 2: If Gemini Vision API key is available, attempt Gemini Vision for AI extraction
        if (apiKey.isNotBlank()) {
            try {
                val base64Image = bitmapToBase64(bitmap)
                val jsonResult = callGeminiVisionApi(base64Image, apiKey)
                if (jsonResult != null) {
                    val date = jsonResult.optString("date", todayStr).ifBlank { todayStr }
                    val itemsArray = jsonResult.optJSONArray("items") ?: JSONArray()
                    val items = mutableListOf<ScannedMedicalItem>()
                    for (i in 0 until itemsArray.length()) {
                        val obj = itemsArray.getJSONObject(i)
                        val pId = obj.optString("patientId", "").trim()
                        val code = obj.optString("code", "").trim()
                        if (pId.isNotBlank() || code.isNotBlank()) {
                            items.add(ScannedMedicalItem(patientId = pId, code = code))
                        }
                    }
                    if (items.isNotEmpty()) {
                        return@withContext ScannedMedicalResult(
                            date = normalizeDate(date, todayStr),
                            items = items,
                            rawText = mlKitRawText ?: ""
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Step 3: Parse the actual ML Kit recognized text if Gemini vision was not used or failed
        if (!mlKitRawText.isNullOrBlank()) {
            val parsedResult = parseRawText(mlKitRawText, todayStr)
            if (parsedResult.items.isNotEmpty()) {
                return@withContext parsedResult
            }
        }

        // Step 4: If no text/items recognized, return empty result (NO HARDCODED MOCK DATA!)
        return@withContext ScannedMedicalResult(
            date = todayStr,
            items = emptyList(),
            rawText = mlKitRawText ?: "",
            errorMessage = "ছবি থেকে কোনো তথ্য পড়া সম্ভব হয়নি। অনুগ্রহ করে স্পষ্ট আলোতে সোজা ছবি তুলুন।"
        )
    }

    private suspend fun recognizeTextWithMlKit(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            suspendCancellableCoroutine<String?> { continuation ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        if (continuation.isActive) {
                            continuation.resume(visionText.text, null)
                        }
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                        if (continuation.isActive) {
                            continuation.resume(null, null)
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val resized = if (bitmap.width > 1200 || bitmap.height > 1200) {
            val ratio = 1200f / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else {
            bitmap
        }
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun callGeminiVisionApi(base64Image: String, apiKey: String): JSONObject? {
        val modelsToTry = listOf("gemini-2.5-flash", "gemini-1.5-flash", "gemini-2.0-flash")

        val prompt = """
            Examine this image of handwritten, printed, or digital list containing medical patient IDs and test/work codes.
            Extract the Date if present in YYYY-MM-DD or DD/MM/YYYY format.
            Extract each patient entry in sequential order:
            1. Patient ID (format is typically 2 letters like AB followed by YYMM digits e.g. AB2608 followed directly by serial number without leading zeros e.g. AB26081, AB260825, AB2608100, or numeric IDs like 1, 2, 15, 101).
            2. Code (number like 101, 102, 105 or code like CBC, USG, X-RAY).
            
            Return ONLY a JSON object with this format (no markdown backticks, no text outside JSON):
            {
              "date": "YYYY-MM-DD",
              "items": [
                {"patientId": "AB26081", "code": "101"},
                {"patientId": "AB26082", "code": "102"}
              ]
            }
        """.trimIndent()

        for (modelName in modelsToTry) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

                val jsonRequest = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                                put(JSONObject().put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                }))
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseMimeType", "application/json")
                        put("temperature", 0.1)
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .post(jsonRequest.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: continue
                    val responseJson = JSONObject(responseBody)
                    val candidates = responseJson.optJSONArray("candidates") ?: continue
                    if (candidates.length() == 0) continue
                    val firstCand = candidates.getJSONObject(0)
                    val content = firstCand.optJSONObject("content") ?: continue
                    val parts = content.optJSONArray("parts") ?: continue
                    if (parts.length() == 0) continue
                    val text = parts.getJSONObject(0).optString("text", "")

                    val cleanJson = text.replace("```json", "").replace("```", "").trim()
                    val resultObj = JSONObject(cleanJson)
                    return resultObj
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun normalizeDate(rawDate: String, fallback: String): String {
        return try {
            if (rawDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return rawDate
            if (rawDate.matches(Regex("\\d{2}/\\d{2}/\\d{4}"))) {
                val parts = rawDate.split("/")
                return "${parts[2]}-${parts[1]}-${parts[0]}"
            }
            fallback
        } catch (e: Exception) {
            fallback
        }
    }

    fun parseRawText(text: String, defaultDate: String): ScannedMedicalResult {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        var date = defaultDate
        val items = mutableListOf<ScannedMedicalItem>()

        val dateRegex = Regex("(\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4})")
        val fullIdRegex = Regex("([A-Za-z]{1,4}\\d{4,8})")
        val monthStr = SimpleDateFormat("MM", Locale.getDefault()).format(Date())
        val yearStr = SimpleDateFormat("yy", Locale.getDefault()).format(Date())
        val defaultPrefix = "AB$yearStr$monthStr"

        lines.forEach { line ->
            // Extract date if present
            val dateMatch = dateRegex.find(line)
            if (dateMatch != null && date == defaultDate) {
                date = normalizeDate(dateMatch.value, defaultDate)
            }

            // Pattern 1: Look for full Patient ID like AB2608001
            val fullIdMatch = fullIdRegex.find(line)
            if (fullIdMatch != null) {
                val pId = fullIdMatch.value.uppercase(Locale.ROOT)
                val remaining = line.replace(fullIdMatch.value, "").trim()
                val codeMatch = Regex("(\\b\\d{2,4}\\b|\\b[A-Za-z]{2,5}\\b)").find(remaining)
                val code = codeMatch?.value ?: "101"
                items.add(ScannedMedicalItem(patientId = pId, code = code))
            } else {
                // Pattern 2: Lines with numbers, commas, dashes, colons or spaces
                val cleanLine = line.replace("Patient", "", ignoreCase = true)
                    .replace("Code", "", ignoreCase = true)
                    .replace("ID", "", ignoreCase = true)
                    .replace("কোড", "", ignoreCase = true)
                    .replace("পেশেন্ট", "", ignoreCase = true)
                    .trim()

                // Check if line is "Code: Patient1, Patient2, Patient3" format
                if (cleanLine.contains(":") || cleanLine.contains("->") || cleanLine.contains("=")) {
                    val splitParts = cleanLine.split(Regex("[:\\->=]")).map { it.trim() }
                    if (splitParts.size >= 2) {
                        val codePart = splitParts[0]
                        val patientsPart = splitParts[1]
                        val patientTokens = patientsPart.split(Regex("[,\\s\\-]+")).filter { it.isNotBlank() }
                        patientTokens.forEach { pToken ->
                            val pId = formatPatientIdToken(pToken, defaultPrefix)
                            if (pId.isNotBlank()) {
                                items.add(ScannedMedicalItem(patientId = pId, code = codePart))
                            }
                        }
                    }
                } else {
                    val parts = cleanLine.split(Regex("[,\\s\\-]+")).filter { it.isNotBlank() }
                    if (parts.size >= 2) {
                        val pIdToken = parts[0]
                        val codeToken = parts[1]
                        val formattedPId = formatPatientIdToken(pIdToken, defaultPrefix)
                        if (formattedPId.isNotBlank() && codeToken.isNotBlank()) {
                            items.add(ScannedMedicalItem(patientId = formattedPId, code = codeToken))
                        }
                    }
                }
            }
        }

        return ScannedMedicalResult(
            date = date,
            items = items,
            rawText = text
        )
    }

    private fun formatPatientIdToken(token: String, defaultPrefix: String): String {
        val clean = token.uppercase(Locale.ROOT).trim()
        if (clean.isBlank()) return ""
        if (clean.matches(Regex("[A-Z]{1,4}\\d{1,8}"))) return clean
        if (clean.all { it.isDigit() }) {
            val numVal = clean.toLongOrNull()
            val numStr = if (numVal != null && numVal > 0) numVal.toString() else clean
            return "$defaultPrefix$numStr"
        }
        return clean
    }
}
