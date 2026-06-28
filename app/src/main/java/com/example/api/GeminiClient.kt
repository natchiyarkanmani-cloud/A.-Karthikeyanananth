package com.example.api

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeminiClient {
    private const val TAG = "GeminiClient"
    var customApiKey: String? = null
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Standard text content generation
     */
    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = customApiKey?.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is missing or default placeholder!")
            return@withContext "Error: Gemini API Key is not set. Please add your GEMINI_API_KEY in the Secrets panel in AI Studio."
        }

        try {
            val requestBodyJson = JSONObject()
            
            // Contents array
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBodyJson.put("contents", contentsArray)

            // System instruction
            if (!systemInstruction.isNullOrBlank()) {
                val sysInstrObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstrObj.put("parts", sysPartsArray)
                requestBodyJson.put("systemInstruction", sysInstrObj)
            }

            val requestBody = requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val requestUrl = "$BASE_URL?key=$apiKey"

            val request = Request.Builder()
                .url(requestUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed code: ${response.code}, body: $errBody")
                    return@withContext "Error: API request failed with status ${response.code}."
                }

                val responseBody = response.body?.string() ?: return@withContext "Error: Empty response body."
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext "No response generated. Safety filters may have blocked the output."
                }

                val content = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext "Error: No content."
                val parts = content.optJSONArray("parts") ?: return@withContext "Error: No parts."
                if (parts.length() == 0) return@withContext "Error: Empty parts list."

                return@withContext parts.getJSONObject(0).optString("text", "No text response found.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during content generation", e)
            return@withContext "Network/Connection Error: ${e.localizedMessage ?: "Please verify your internet connection."}"
        }
    }

    /**
     * Multimodal content generation (Text + Image)
     */
    suspend fun generateContentWithImage(
        prompt: String,
        imageBase64: String,
        mimeType: String = "image/jpeg",
        systemInstruction: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = customApiKey?.takeIf { it.isNotBlank() } ?: BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is missing or default placeholder!")
            return@withContext "Error: Gemini API Key is not set. Please add your GEMINI_API_KEY in the Secrets panel in AI Studio."
        }

        try {
            val requestBodyJson = JSONObject()
            
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // Part 1: Text prompt
            val textPartObj = JSONObject()
            textPartObj.put("text", prompt)
            partsArray.put(textPartObj)

            // Part 2: Image inlineData
            val imagePartObj = JSONObject()
            val inlineDataObj = JSONObject()
            inlineDataObj.put("mimeType", mimeType)
            inlineDataObj.put("data", imageBase64)
            imagePartObj.put("inlineData", inlineDataObj)
            partsArray.put(imagePartObj)

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestBodyJson.put("contents", contentsArray)

            if (!systemInstruction.isNullOrBlank()) {
                val sysInstrObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstrObj.put("parts", sysPartsArray)
                requestBodyJson.put("systemInstruction", sysInstrObj)
            }

            val requestBody = requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val requestUrl = "$BASE_URL?key=$apiKey"

            val request = Request.Builder()
                .url(requestUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API image-call failed code: ${response.code}, body: $errBody")
                    return@withContext "Error: API request failed with status ${response.code}."
                }

                val responseBody = response.body?.string() ?: return@withContext "Error: Empty response body."
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext "No response generated. Safety filters may have blocked the output."
                }

                val content = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext "Error: No content."
                val parts = content.optJSONArray("parts") ?: return@withContext "Error: No parts."
                if (parts.length() == 0) return@withContext "Error: Empty parts list."

                return@withContext parts.getJSONObject(0).optString("text", "No text response found.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during image content generation", e)
            return@withContext "Network/Connection Error: ${e.localizedMessage ?: "Please verify your internet connection."}"
        }
    }
}
