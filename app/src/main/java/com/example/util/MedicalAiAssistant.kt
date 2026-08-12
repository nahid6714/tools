package com.example.util

import com.example.BuildConfig
import com.example.data.MedicalRecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object MedicalAiAssistant {

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    suspend fun generateWorkSummary(
        date: String,
        records: List<MedicalRecordEntity>,
        apiKey: String = BuildConfig.GEMINI_API_KEY
    ): String = withContext(Dispatchers.IO) {
        if (records.isEmpty()) {
            return@withContext "আজকের তালিকায় কোনো মেডিকেল এন্ট্রি পাওয়া যায়নি।"
        }

        if (apiKey.isBlank()) {
            return@withContext generateLocalSummaryFallback(date, records)
        }

        try {
            val codeCounts = records.groupingBy { it.code }.eachCount()
            val totalPatients = records.map { it.patientId }.distinct().size

            val prompt = """
                You are an expert AI Medical Work Assistant for a hospital/clinic reporting app.
                Analyze the following medical work entries for Date: $date.
                
                Summary Data:
                - Total Records: ${records.size}
                - Unique Patient IDs: $totalPatients
                - Code Breakdown: $codeCounts
                - First Patient ID: ${records.firstOrNull()?.patientId ?: "N/A"}
                - Last Patient ID: ${records.lastOrNull()?.patientId ?: "N/A"}
                
                Generate a professional, encouraging, and concise Bengali Executive Work Summary (৩-৪ টি সংক্ষেপিত প্যারাগ্রাফ/পয়েন্ট)।
                Include:
                ১. কাজের সার্বিক সারসংক্ষেপ (মোট রোগী ও কাজের পরিমাণ)।
                ২. সর্বাধিক ব্যবহৃত কোড ও কাজ বিভাজন।
                ৩. পেশেন্ট আইডি সিকোয়েন্স ও বিশেষ কোনো পর্যবেক্ষণ।
                ৪. ডক্টর/ল্যাব রিপোর্টের জন্য দ্রুত রিমাইন্ডার বা টিপস।
                
                Use clean markdown style bullet points in clear Bengali.
            """.trimIndent()

            val responseText = callGeminiTextApi(prompt, apiKey)
            if (!responseText.isNullOrBlank()) {
                return@withContext responseText
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext generateLocalSummaryFallback(date, records)
    }

    suspend fun parseNotesToRecords(
        notesText: String,
        defaultDate: String,
        apiKey: String = BuildConfig.GEMINI_API_KEY
    ): List<ScannedMedicalItem> = withContext(Dispatchers.IO) {
        if (notesText.isBlank()) return@withContext emptyList()

        if (apiKey.isNotBlank()) {
            try {
                val prompt = """
                    You are an AI assistant parsing unstructured Bengali or English medical text notes into structured entries.
                    Extract each patient entry with Patient ID and Code.
                    If patient IDs are a range like "AB2608001 to AB2608005", expand them into individual sequential entries.
                    
                    Text Input:
                    "$notesText"
                    
                    Return ONLY a valid JSON array of objects with keys "patientId" and "code".
                    Example:
                    [
                      {"patientId": "AB2608001", "code": "101"},
                      {"patientId": "AB2608002", "code": "102"}
                    ]
                """.trimIndent()

                val responseText = callGeminiTextApi(prompt, apiKey)
                if (!responseText.isNullOrBlank()) {
                    val cleanJson = responseText.replace("```json", "").replace("```", "").trim()
                    val jsonArray = JSONArray(cleanJson)
                    val items = mutableListOf<ScannedMedicalItem>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val pId = obj.optString("patientId", "").trim()
                        val code = obj.optString("code", "").trim()
                        if (pId.isNotBlank() || code.isNotBlank()) {
                            items.add(ScannedMedicalItem(patientId = pId, code = code))
                        }
                    }
                    if (items.isNotEmpty()) return@withContext items
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Fallback to local regex parser
        return@withContext MedicalImageOcrScanner.parseRawText(notesText, defaultDate).items
    }

    private fun callGeminiTextApi(prompt: String, apiKey: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonRequest.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseBody = response.body?.string() ?: return null
        val responseJson = JSONObject(responseBody)
        val candidates = responseJson.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null
        val firstCand = candidates.getJSONObject(0)
        val content = firstCand.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        if (parts.length() == 0) return null
        return parts.getJSONObject(0).optString("text", "")
    }

    private fun generateLocalSummaryFallback(date: String, records: List<MedicalRecordEntity>): String {
        val total = records.size
        val uniquePatients = records.map { it.patientId }.distinct().size
        val codeCounts = records.groupingBy { it.code }.eachCount()
        val topCode = codeCounts.maxByOrNull { it.value }

        val sb = StringBuilder()
        sb.append("📊 **মেডিকেল কাজের এআই সারসংক্ষেপ ($date)**\n\n")
        sb.append("• **মোট এন্ট্রি:** $total টি মেডিকেল রেকর্ড\n")
        sb.append("• **পেশেন্ট সংখ্যা:** $uniquePatients জন ইউনিক রোগী\n")
        if (topCode != null) {
            sb.append("• **সর্বোচ্চ ব্যবহৃত কোড:** `${topCode.key}` (${topCode.value} বার)\n")
        }
        sb.append("• **কোড বিভাজন:** ")
        codeCounts.entries.forEachIndexed { idx, entry ->
            sb.append("${entry.key} (${entry.value}টি)")
            if (idx < codeCounts.size - 1) sb.append(", ")
        }
        sb.append("\n\n💡 *টিপস: সব রেকর্ড সঠিকভাবে মেমো এবং রিপোর্টে স্থান পেয়েছেন কিনা তা মিলিয়ে নিন।*")
        return sb.toString()
    }
}
