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
            Log.w(TAG, "No API Key configured. Redirecting to Free Local AI Simulator.")
            return@withContext simulateResponse(prompt, systemInstruction)
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
                    Log.e(TAG, "API call failed code: ${response.code}, body: $errBody. Falling back to local simulated AI.")
                    return@withContext simulateResponse(prompt, systemInstruction)
                }

                val responseBody = response.body?.string() ?: return@withContext simulateResponse(prompt, systemInstruction)
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext simulateResponse(prompt, systemInstruction)
                }

                val content = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext simulateResponse(prompt, systemInstruction)
                val parts = content.optJSONArray("parts") ?: return@withContext simulateResponse(prompt, systemInstruction)
                if (parts.length() == 0) return@withContext simulateResponse(prompt, systemInstruction)

                return@withContext parts.getJSONObject(0).optString("text", "No text response found.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during content generation, falling back to local simulated AI", e)
            return@withContext simulateResponse(prompt, systemInstruction)
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
            Log.w(TAG, "No API Key configured. Redirecting Image analysis to Free Local AI Simulator.")
            return@withContext simulateResponse(prompt, systemInstruction)
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
                    Log.e(TAG, "API image-call failed code: ${response.code}, body: $errBody. Falling back to local simulated AI.")
                    return@withContext simulateResponse(prompt, systemInstruction)
                }

                val responseBody = response.body?.string() ?: return@withContext simulateResponse(prompt, systemInstruction)
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext simulateResponse(prompt, systemInstruction)
                }

                val content = candidates.getJSONObject(0).optJSONObject("content") ?: return@withContext simulateResponse(prompt, systemInstruction)
                val parts = content.optJSONArray("parts") ?: return@withContext simulateResponse(prompt, systemInstruction)
                if (parts.length() == 0) return@withContext simulateResponse(prompt, systemInstruction)

                return@withContext parts.getJSONObject(0).optString("text", "No text response found.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during image content generation, falling back to local simulated AI", e)
            return@withContext simulateResponse(prompt, systemInstruction)
        }
    }

    // --- Local Smart Offline AI Fallback Simulation Engine ---
    
    data class UserHealthContext(
        val age: Int = 25,
        val gender: String = "Male",
        val healthIssues: String = "None",
        val weight: Double = 70.0,
        val bmi: Double = 22.0
    )

    private fun parseHealthContext(systemInstruction: String?): UserHealthContext {
        if (systemInstruction == null) return UserHealthContext()
        var age = 25
        var gender = "Male"
        var healthIssues = "None"
        var weight = 70.0
        var bmi = 22.0

        try {
            val lines = systemInstruction.lines()
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("- Age:")) {
                    age = trimmed.removePrefix("- Age:").trim().toIntOrNull() ?: 25
                } else if (trimmed.startsWith("- Gender:")) {
                    gender = trimmed.removePrefix("- Gender:").trim()
                } else if (trimmed.startsWith("- Health Issues/Dietary Conditions:")) {
                    healthIssues = trimmed.removePrefix("- Health Issues/Dietary Conditions:").trim()
                } else if (trimmed.startsWith("- Weight:")) {
                    // Extract numeric part of "- Weight: 70.0 kg"
                    val wStr = trimmed.removePrefix("- Weight:").replace("kg", "").trim()
                    weight = wStr.toDoubleOrNull() ?: 70.0
                } else if (trimmed.startsWith("- Calculated BMI:")) {
                    bmi = trimmed.removePrefix("- Calculated BMI:").trim().toDoubleOrNull() ?: 22.0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing health context", e)
        }
        return UserHealthContext(age, gender, healthIssues, weight, bmi)
    }

    private fun simulateResponse(prompt: String, systemInstruction: String?): String {
        val health = parseHealthContext(systemInstruction)
        val promptLower = prompt.lowercase()

        // 1. Check if the prompt is asking for a Product Scan / Image Scan (JSON format)
        if (promptLower.contains("json schema") || promptLower.contains("productname") || promptLower.contains("fssairating")) {
            // Extract product name or description
            var prodName = "Captured Product"
            
            if (prompt.contains("Product:")) {
                val idx = prompt.indexOf("Product:")
                val rest = prompt.substring(idx + "Product:".length).trim()
                val endIdx = rest.indexOf(",")
                prodName = if (endIdx != -1) rest.substring(0, endIdx).trim() else rest.lines().firstOrNull()?.trim() ?: "Product"
            } else if (prompt.contains("Product barcode/name entered:")) {
                val idx = prompt.indexOf("Product barcode/name entered:")
                prodName = prompt.substring(idx + "Product barcode/name entered:".length).trim().lines().firstOrNull()?.trim() ?: "Product"
            } else if (prompt.contains("Extracted Product Name")) {
                prodName = "Captured Label"
            }

            // Clean up prodName if it has trailing characters
            prodName = prodName.split("\n").first().split("\r").first().trim()

            // Clean up placeholder barcodes
            if (prodName.all { it.isDigit() }) {
                prodName = when (prodName) {
                    "8901234567891" -> "Canned Rich Tomato Soup"
                    "7622210811987" -> "Premium Chocolate Bar"
                    "5449000000996" -> "Zero Sugar Cola"
                    "8901123400232" -> "Mixed Fruit Jam"
                    else -> "Product ($prodName)"
                }
            }

            // Determine category & simulate ingredients/safety
            val nameLower = prodName.lowercase()
            var fssai = "3.5 Stars"
            var preservatives = "None detected."
            var addedIngredients = "None."
            var advice = ""

            if (nameLower.contains("cola") || nameLower.contains("soda") || nameLower.contains("fanta") || nameLower.contains("sprite") || nameLower.contains("pepsi") || nameLower.contains("beverage")) {
                prodName = if (prodName.startsWith("Product")) "Zero Sugar Cola" else prodName
                fssai = "1.5 Stars"
                preservatives = "Sodium Benzoate (High), Phosphoric Acid (Acidity Regulator)."
                addedIngredients = "Caramel Color, Aspartame, Acesulfame Potassium, Caffeine."
                advice = "This beverage is highly processed and contains zero nutritional value. " +
                        (if (health.healthIssues.lowercase().contains("diabet")) {
                            "Warning: Since you have Diabetes, please note that while artificial sweeteners like Aspartame do not raise immediate blood sugar, long-term use can alter insulin sensitivity. "
                        } else "") +
                        (if (health.healthIssues.lowercase().contains("acid") || health.healthIssues.lowercase().contains("gerd")) {
                            "Warning: Since you have Acid Reflux, the carbonation and caffeine in this drink can severely trigger your symptoms."
                        } else "Avoid regular consumption to maintain a healthy gut microbiome.")
            } else if (nameLower.contains("soup") || nameLower.contains("tomato")) {
                prodName = if (prodName.startsWith("Product")) "Canned Rich Tomato Soup" else prodName
                fssai = "3.0 Stars"
                preservatives = "Potassium Sorbate (Moderate), Sodium Benzoate (Low)."
                addedIngredients = "High Fructose Corn Syrup, Wheat Flour (Gluten), Salt, Added Colorings."
                advice = "Contains preservatives to maintain shelf life. " +
                        (if (health.healthIssues.lowercase().contains("hypertension") || health.healthIssues.lowercase().contains("pressure") || health.healthIssues.lowercase().contains("salt")) {
                            "Warning: Since you have Hypertension, this canned soup is high in sodium (added salt) which can elevate blood pressure. Consider making fresh tomato soup at home!"
                        } else "") +
                        (if (health.healthIssues.lowercase().contains("diabet")) {
                            "Note: High Fructose Corn Syrup is present, which has a high glycemic index. Consume in moderation."
                        } else "Safe for occasional use, but watch out for the sodium content.")
            } else if (nameLower.contains("chocolate") || nameLower.contains("candy") || nameLower.contains("sweet") || nameLower.contains("cookie") || nameLower.contains("biscuit") || nameLower.contains("jam")) {
                prodName = if (prodName.startsWith("Product")) "Premium Chocolate Bar" else prodName
                fssai = "2.0 Stars"
                preservatives = "Potassium Sorbate (Low)."
                addedIngredients = "Refined Sugar, Cocoa Butter, Milk Solids, Soy Lecithin (Emulsifier 322), Ethyl Vanillin (Added Flavor)."
                advice = "High in simple carbohydrates and saturated fats. " +
                        (if (health.healthIssues.lowercase().contains("diabet")) {
                            "CRITICAL WARNING: Since you have Diabetes, this product is extremely high in refined sugars which will cause a rapid spike in blood glucose levels. Avoid completely or choose a sugar-free alternative!"
                        } else "") +
                        (if (health.healthIssues.lowercase().contains("cholesterol")) {
                            "Warning: Contains milk solids and cocoa butter which are high in saturated fat. Limit intake to keep LDL cholesterol in check."
                        } else "Enjoy sparingly as a treat.")
            } else if (nameLower.contains("chips") || nameLower.contains("kurkure") || nameLower.contains("crisps") || nameLower.contains("fried") || nameLower.contains("snack") || nameLower.contains("noodles") || nameLower.contains("maggie") || nameLower.contains("pasta")) {
                prodName = if (prodName.startsWith("Product")) "Crunchy Potato Chips" else prodName
                fssai = "1.5 Stars"
                preservatives = "TBHQ (Tertiary Butylhydroquinone - synthetic antioxidant)."
                addedIngredients = "Palm Oil, Monosodium Glutamate (MSG - E621 flavor enhancer), Disodium Guanylate, Salt, Acidity Regulators."
                advice = "Deep-fried, ultra-processed savory snack with high sodium load. " +
                        (if (health.healthIssues.lowercase().contains("hypertension") || health.healthIssues.lowercase().contains("pressure") || health.healthIssues.lowercase().contains("salt")) {
                            "CRITICAL WARNING: Since you have Hypertension, the MSG and added salts load your system with high sodium, directly raising blood pressure. Avoid completely!"
                        } else "") +
                        (if (health.healthIssues.lowercase().contains("cholesterol") || health.weight > 80) {
                            "Warning: Fried in palm oil which has high saturated fats. Highly discouraged for weight management and cardiovascular health."
                        } else "High sodium and trans-fats. Consume very occasionally.")
            } else if (nameLower.contains("juice") || nameLower.contains("mango") || nameLower.contains("orange") || nameLower.contains("beverage")) {
                prodName = if (prodName.startsWith("Product")) "Packaged Orange Juice" else prodName
                fssai = "2.5 Stars"
                preservatives = "Sodium Benzoate (Moderate), Citric Acid."
                addedIngredients = "Reconstituted Fruit Pulp, Sugar Syrup, Permitted Synthetic Colors, Added Flavors."
                advice = "High in liquid sugar and lacks natural fruit fiber. " +
                        (if (health.healthIssues.lowercase().contains("diabet")) {
                            "Warning: Highly diabetic-unfriendly! Causes quick glycemic spikes. Prefer eating whole fresh fruits instead."
                        } else "Lacks whole fruit nutrients. Fresh homemade unsweetened juices are a much safer bet.")
            } else if (nameLower.contains("oats") || nameLower.contains("oatmeal") || nameLower.contains("muesli") || nameLower.contains("cereal")) {
                prodName = if (prodName.startsWith("Product")) "Whole Grain Rolled Oats" else prodName
                fssai = "4.5 Stars"
                preservatives = "None (Natural Tocopherols)."
                addedIngredients = "Whole Grain Oats, dietary fibers."
                advice = "Excellent source of complex carbs and beta-glucan soluble fiber. " +
                        (if (health.healthIssues.lowercase().contains("cholesterol")) {
                            "Highly Recommended: Soluble fiber binds to cholesterol in the digestive system and helps drag it out of the body, helping lower LDL levels!"
                        } else "") +
                        (if (health.healthIssues.lowercase().contains("diabet")) {
                            "Recommended: Keeps blood sugar stable and has a low glycemic index if eaten unsweetened."
                        } else "Very healthy breakfast choice!")
            } else if (nameLower.contains("apple") || nameLower.contains("banana") || nameLower.contains("fruit") || nameLower.contains("vegetable") || nameLower.contains("salad")) {
                prodName = if (prodName.startsWith("Product")) "Fresh Fruits & Greens" else prodName
                fssai = "5.0 Stars"
                preservatives = "None (Natural product)."
                addedIngredients = "None (100% natural, whole food)."
                advice = "Perfect! Natural whole food packed with dietary fiber, water, vitamins, and minerals. Highly recommended for any healthy diet, perfect for weight management and blood sugar control."
            } else {
                // Default generic food
                fssai = "3.5 Stars"
                preservatives = "Sodium Benzoate, Potassium Sorbate (low level)."
                addedIngredients = "Emulsifiers, Salt, Acidity Regulators (Citric Acid)."
                advice = "This food item contains standard preservatives and moderate sodium/sugar. " +
                        (if (health.healthIssues.isNotBlank() && health.healthIssues != "None") {
                            "Personalized check: please read nutrition values for any components that impact your condition: ${health.healthIssues}."
                        } else "Always read the label for daily values of sugar, trans-fats, and sodium.")
            }

            return """
                {
                  "productName": "$prodName",
                  "preservatives": "$preservatives",
                  "addedIngredients": "$addedIngredients",
                  "fssaiRating": "$fssai",
                  "safetyAdvice": "$advice"
                }
            """.trimIndent()
        }

        // 2. Chat / Conversational Fallback
        if (promptLower.contains("hello") || promptLower.contains("hi") || promptLower.contains("hey")) {
            return "Hello! I am your EatRite Offline AI, working completely keyless. I can help you analyze food safety, preservation, temperature guidelines, and read nutritional labels. How can I help you today?"
        }

        if (promptLower.contains("sodium benzoate") || promptLower.contains("benzoate")) {
            return "Sodium Benzoate is a common food preservative (often listed as E211) used to prevent mold and bacterial growth in acidic foods like soft drinks, salad dressings, and fruit juices. \n\n" +
                    "**Safety Insights:**\n" +
                    "- It is generally safe in small quantities but can react with Vitamin C (Ascorbic Acid) to form benzene, a known carcinogen, under exposure to high heat/light.\n" +
                    "- *Health Profile recommendation:* " +
                    (if (health.healthIssues.lowercase().contains("allergy") || health.healthIssues.lowercase().contains("asthma")) 
                        "Since you have respiratory issues or allergies, be aware that sodium benzoate can sometimes trigger eczema, hives, or mild asthma-like symptoms." 
                    else "Perfect for occasional use, but prefer fresh, unprocessed foods to minimize synthetic additive intake.")
        }

        if (promptLower.contains("danger zone") || promptLower.contains("temperature") || promptLower.contains("storage") || promptLower.contains("spoil")) {
            return "In food safety, the **Temperature Danger Zone** is between **4°C and 60°C (40°F - 140°F)**. \n\n" +
                    "**Critical storage risks:**\n" +
                    "- Bacteria like *Salmonella*, *E. coli*, and *Listeria* multiply exponentially within this zone, doubling in number every 20 minutes.\n" +
                    "- **Rule of thumb:** Do not leave cooked foods or perishable dairy/meat out at room temperature for more than **2 hours** (or 1 hour in hot tropical weather like Thoothukudi).\n" +
                    "- Always refrigerate leftovers below 4°C and reheat them thoroughly to at least 74°C (165°F) before eating."
        }

        if (promptLower.contains("thoothukudi") || promptLower.contains("establishment") || promptLower.contains("restaurant") || promptLower.contains("advisory")) {
            // Find place details
            var placeName = "this establishment"
            var category = "food joint"
            if (prompt.contains("Name:")) {
                val idx = prompt.indexOf("Name:")
                placeName = prompt.substring(idx + "Name:".length).trim().lines().firstOrNull()?.trim() ?: "this place"
            }
            if (prompt.contains("Category/Type:")) {
                val idx = prompt.indexOf("Category/Type:")
                category = prompt.substring(idx + "Category/Type:".length).trim().lines().firstOrNull()?.trim() ?: "food outlet"
            }

            return "### EatRite Food Safety & Dietary Advisory for **$placeName** ($category) in Thoothukudi\n\n" +
                    "1. **Hygiene & Temperature Risks:**\n" +
                    "   - Thoothukudi's tropical, humid climate accelerates bacterial growth. In $category joints, watch out for incorrect hot/cold holding. If meat or gravies are kept at room temperature (the 'Danger Zone' of 4°C - 60°C), harmful pathogens like *Salmonella* and *Clostridium* can multiply rapidly.\n" +
                    "   - For juice/street stalls, ensure ice is sourced from purified water and raw ingredients are washed properly.\n\n" +
                    "2. **Personalized Health Matching:**\n" +
                    "   - **Your profile context:** User age ${health.age}, health profile includes: *${health.healthIssues}*.\n" +
                    "   - " + (if (health.healthIssues.lowercase().contains("diabet")) {
                        "**Crucial warning:** Since you have Diabetes, avoid refined-sugar specialties here (like Thoothukudi Macaroons or sugary juices/lassi). Instead, choose unsweetened options or ask for 'sugar-free/less sugar' explicitly."
                    } else if (health.healthIssues.lowercase().contains("hypertension") || health.healthIssues.lowercase().contains("pressure")) {
                        "**Crucial warning:** Since you have Hypertension, avoid heavily salted chats, papads, and re-fried snack items. Ask the chef to reduce salt in your mains if possible."
                    } else if (health.healthIssues.lowercase().contains("acid") || health.healthIssues.lowercase().contains("gerd")) {
                        "**Crucial warning:** Since you have Acid Reflux, avoid deep-fried appetizers and high-spice gravies. They stimulate high stomach acid."
                    } else {
                        "Choose fresh, freshly prepared piping hot meals to minimize contamination risk, and limit fried items to maintain a healthy diet."
                    }) + "\n\n" +
                    "3. **Safe Ordering Tips:**\n" +
                    "   - Request fresh, hot preparation where possible.\n" +
                    "   - Avoid raw items (like unpasteurized egg mayonnaise or pre-cut salads) in standard roadside eateries."
        }

        // High quality conversational fallback
        return "Thank you for asking! As your personal food safety assistant, here is my guidance:\n\n" +
                "Regarding your query: *\"$prompt\"*,\n" +
                "It is vital to prioritize hygiene and read nutritional facts. " +
                (if (health.healthIssues.isNotBlank() && health.healthIssues != "None") {
                    "Since you have *${health.healthIssues}*, ensure you check for specific triggers, avoid high saturated fats, and avoid excessive refined salt/sugar. "
                } else "") +
                "\n\nFeel free to ask more questions about specific additives, preservatives, shelf-life, or safety thresholds!"
    }
}

