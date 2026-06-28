package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.AppDatabase
import com.example.data.FoodSafetyRepository
import com.example.data.ScanHistory
import com.example.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import org.json.JSONObject

// Models for UI Chat
data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// Scanner States
sealed interface ScanResultState {
    object Idle : ScanResultState
    object Loading : ScanResultState
    data class Success(val scan: ScanHistory) : ScanResultState
    data class Error(val message: String) : ScanResultState
}

// Food places data model
data class FoodPlace(
    val id: String,
    val name: String,
    val type: String, // Restaurant, Juice Shop, Snack Shop
    val rating: Double,
    val specialNotes: String = ""
)

class FoodSafetyViewModel(application: Application) : AndroidViewModel(application) {
    
    private val TAG = "FoodSafetyViewModel"
    private val repository: FoodSafetyRepository
    
    // User profile Flow from DB
    val userProfile: StateFlow<UserProfile?>

    // Custom API Key Flow from SharedPreferences
    private val _customApiKeyFlow = MutableStateFlow("")
    val customApiKeyFlow: StateFlow<String> = _customApiKeyFlow.asStateFlow()
    
    // Scan history Flow from DB
    val scanHistory: StateFlow<List<ScanHistory>>

    // Chat Message state
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                content = "Hello! I am EatRite AI, your personal Food Safety and Dietary Assistant. Tell me about your food safety concerns, ingredients, spoilage indicators, or storage temperatures!",
                isUser = false
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // Scanner state
    private val _scanResult = MutableStateFlow<ScanResultState>(ScanResultState.Idle)
    val scanResult: StateFlow<ScanResultState> = _scanResult.asStateFlow()

    // Selected restaurant for safety advice
    private val _selectedPlace = MutableStateFlow<FoodPlace?>(null)
    val selectedPlace: StateFlow<FoodPlace?> = _selectedPlace.asStateFlow()

    private val _placeAdviceLoading = MutableStateFlow(false)
    val placeAdviceLoading: StateFlow<Boolean> = _placeAdviceLoading.asStateFlow()

    private val _placeAdvice = MutableStateFlow<String>("")
    val placeAdvice: StateFlow<String> = _placeAdvice.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FoodSafetyRepository(database.foodSafetyDao())
        
        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        scanHistory = repository.scanHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        val prefs = application.getSharedPreferences("eatrite_prefs", android.content.Context.MODE_PRIVATE)
        val loadedKey = prefs.getString("custom_api_key", "") ?: ""
        _customApiKeyFlow.value = loadedKey
        GeminiClient.customApiKey = loadedKey
    }

    // --- Profile Actions ---
    fun saveProfile(gender: String, age: Int, healthIssues: String, height: Double, weight: Double) {
        viewModelScope.launch {
            val profile = UserProfile(gender = gender, age = age, healthIssues = healthIssues, height = height, weight = weight)
            repository.saveUserProfile(profile)
            
            // Post a system message in the chat notifying about the profile update
            val currentList = _chatMessages.value.toMutableList()
            currentList.add(
                ChatMessage(
                    content = "👤 [System]: Your health profile has been updated! My recommendations will now be tailored to your health info.",
                    isUser = false
                )
            )
            _chatMessages.value = currentList
        }
    }

    fun saveCustomApiKey(key: String) {
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("eatrite_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("custom_api_key", key).apply()
            _customApiKeyFlow.value = key
            GeminiClient.customApiKey = key
            
            // Post a system message in the chat notifying about the key update
            val currentList = _chatMessages.value.toMutableList()
            currentList.add(
                ChatMessage(
                    content = "🔑 [System]: Your custom Gemini API Key has been updated in the app context!",
                    isUser = false
                )
            )
            _chatMessages.value = currentList
        }
    }

    // --- Chat Actions ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return

        // 1. Add user message
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(ChatMessage(content = text, isUser = true))
        _chatMessages.value = currentList

        _isChatLoading.value = true

        viewModelScope.launch {
            val sysInstruction = buildSystemInstruction(userProfile.value)
            
            // Build full thread context
            val promptBuilder = StringBuilder()
            promptBuilder.append("Conversation history:\n")
            // Send last 10 messages for context
            val recentMessages = currentList.takeLast(10)
            recentMessages.forEach { msg ->
                val speaker = if (msg.isUser) "User" else "Assistant"
                promptBuilder.append("$speaker: ${msg.content}\n")
            }
            promptBuilder.append("\nAssistant:")

            val response = GeminiClient.generateContent(
                prompt = promptBuilder.toString(),
                systemInstruction = sysInstruction
            )

            val updatedList = _chatMessages.value.toMutableList()
            updatedList.add(ChatMessage(content = response, isUser = false))
            _chatMessages.value = updatedList
            _isChatLoading.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                content = "Hello! Let's start fresh. How can I help you with food safety, ingredient checking, or storage queries today?",
                isUser = false
            )
        )
    }

    // --- Product Scanner Actions ---
    
    // Hardcoded barcode items so users can test immediately
    val barcodePresets = listOf(
        FoodPlacePreset("8901058002314", "Maggi 2-Min Masala Noodles", "Maida, Palm Oil, Salt, Wheat Gluten, Spices, Potassium Chloride, Flavor Enhancers (635), Preservatives"),
        FoodPlacePreset("8901234567891", "Canned Rich Tomato Soup", "Concentrated Tomato Puree, High Fructose Corn Syrup, Wheat Flour, Salt, Potassium Sorbate, Sodium Benzoate, Added Coloring"),
        FoodPlacePreset("7622210811987", "Premium Chocolate Bar", "Sugar, Cocoa Butter, Milk Solids, Cocoa Solids, Emulsifiers (442, 476), Added Flavors (Ethyl Vanillin)"),
        FoodPlacePreset("5449000000996", "Zero Sugar Cola", "Carbonated Water, Caramel Color, Phosphoric Acid, Aspartame, Ace-K, Sodium Benzoate (Preservative), Caffeine"),
        FoodPlacePreset("8901123400232", "Mixed Fruit Jam", "Fruit Pulp, Sugar, Pectin, Citric Acid, Preservative (Sodium Benzoate), Synthetic Food Colors (122)")
    )

    fun resetScanner() {
        _scanResult.value = ScanResultState.Idle
    }

    fun deleteScan(id: Long) {
        viewModelScope.launch {
            repository.deleteScanRecord(id)
        }
    }

    fun clearScanHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    /**
     * Scans by product barcode or custom name typed by the user
     */
    fun scanProduct(barcodeOrName: String) {
        if (barcodeOrName.isBlank()) return
        
        _scanResult.value = ScanResultState.Loading

        viewModelScope.launch {
            val preset = barcodePresets.find { it.barcode == barcodeOrName || it.name.lowercase().contains(barcodeOrName.lowercase()) }
            val itemDescription = preset?.let { "Product: ${it.name}, Ingredients list: ${it.ingredients}" } ?: "Product barcode/name entered: $barcodeOrName"

            val sysInstruction = buildSystemInstruction(userProfile.value)
            val prompt = """
                Analyze the following food product. Provide your answer strictly as a JSON object with the exact keys shown below.
                
                Input: $itemDescription
                
                Required JSON Schema:
                {
                  "productName": "Common Name of the Product",
                  "preservatives": "List of preservatives found (e.g. Sodium Benzoate, Potassium Sorbate) and their safety levels",
                  "addedIngredients": "List of major added ingredients or chemical additives, colorings, flavor enhancers",
                  "fssaiRating": "Give an estimated FSSAI security rating from 1 to 5 stars (e.g. '3.5 Stars') based on nutritional quality, additive load, and safety",
                  "safetyAdvice": "Personalized health advice regarding this product. State explicitly if it's safe, warned, or dangerous based on the user's age, weight, and specific health issues (e.g. if the user has Diabetes, warn about high sugar/carbs, if Hypertension warn about high Sodium, etc.). Keep it clear, friendly, and actionable."
                }
                
                Do not include any markdown wrappers (like ```json) in your final response. Return ONLY the raw valid JSON string.
            """.trimIndent()

            val response = GeminiClient.generateContent(prompt = prompt, systemInstruction = sysInstruction)
            
            try {
                // Parse the JSON response
                val cleanJson = response.trim().removeSurrounding("```json", "```").trim()
                val json = JSONObject(cleanJson)
                val prodName = json.optString("productName", preset?.name ?: barcodeOrName)
                val preservatives = json.optString("preservatives", "None identified.")
                val addedIngredients = json.optString("addedIngredients", "Standard ingredients.")
                val fssaiRating = json.optString("fssaiRating", "3.0 Stars")
                val safetyAdvice = json.optString("safetyAdvice", "No warnings generated.")

                val scanRecord = ScanHistory(
                    barcodeOrName = barcodeOrName,
                    productName = prodName,
                    preservatives = preservatives,
                    addedIngredients = addedIngredients,
                    fssaiRating = fssaiRating,
                    safetyAdvice = safetyAdvice
                )

                repository.addScanRecord(scanRecord)
                _scanResult.value = ScanResultState.Success(scanRecord)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse JSON response from Gemini", e)
                // Fallback to text presentation
                val scanRecord = ScanHistory(
                    barcodeOrName = barcodeOrName,
                    productName = preset?.name ?: barcodeOrName,
                    preservatives = "Analyzed text response: see advice.",
                    addedIngredients = preset?.ingredients ?: "Standard",
                    fssaiRating = "3.5 Stars",
                    safetyAdvice = response
                )
                repository.addScanRecord(scanRecord)
                _scanResult.value = ScanResultState.Success(scanRecord)
            }
        }
    }

    /**
     * Scan with image content (using camera to capture product label/ingredients)
     */
    fun scanProductImage(bitmap: Bitmap) {
        _scanResult.value = ScanResultState.Loading

        viewModelScope.launch {
            try {
                val base64Image = bitmap.toBase64()
                val sysInstruction = buildSystemInstruction(userProfile.value)
                
                val prompt = """
                    Analyze the captured image of this food product packaging / ingredients list. Extract the ingredients and evaluate the safety. 
                    Provide your answer strictly as a JSON object with the exact keys shown below.
                    
                    Required JSON Schema:
                    {
                      "productName": "Extracted Product Name (or 'Captured Product' if not visible)",
                      "preservatives": "List of preservatives identified from the label and their safety levels",
                      "addedIngredients": "List of chemical additives, emulsifiers, artificial colors, or flavor enhancers seen",
                      "fssaiRating": "Give an estimated FSSAI rating from 1 to 5 stars (e.g. '4.0 Stars') based on healthy vs processed metrics",
                      "safetyAdvice": "Personalized health warning or approval based on the user's health issues (like allergies, diabetes, blood pressure). Be highly specific."
                    }
                    
                    Do not include any markdown wrappers (like ```json). Return ONLY the raw valid JSON string.
                """.trimIndent()

                val response = GeminiClient.generateContentWithImage(
                    prompt = prompt,
                    imageBase64 = base64Image,
                    mimeType = "image/jpeg",
                    systemInstruction = sysInstruction
                )

                val cleanJson = response.trim().removeSurrounding("```json", "```").trim()
                val json = JSONObject(cleanJson)
                val prodName = json.optString("productName", "Captured Product")
                val preservatives = json.optString("preservatives", "No standard preservatives detected.")
                val addedIngredients = json.optString("addedIngredients", "Not clearly visible.")
                val fssaiRating = json.optString("fssaiRating", "3.0 Stars")
                val safetyAdvice = json.optString("safetyAdvice", "No specific profile-matched warnings.")

                val scanRecord = ScanHistory(
                    barcodeOrName = "Photo Scan",
                    productName = prodName,
                    preservatives = preservatives,
                    addedIngredients = addedIngredients,
                    fssaiRating = fssaiRating,
                    safetyAdvice = safetyAdvice
                )

                repository.addScanRecord(scanRecord)
                _scanResult.value = ScanResultState.Success(scanRecord)

            } catch (e: Exception) {
                Log.e(TAG, "Failed image scan analysis", e)
                _scanResult.value = ScanResultState.Error("Failed to analyze image: ${e.localizedMessage}")
            }
        }
    }

    // --- Restaurant & Places Section ---
    val localPlaces = listOf(
        // Restaurants
        FoodPlace("r1", "Arabian Lulu Restaurant", "Restaurant", 4.1, "Famous for Shawarma, Grilled chicken, and Mandi. Note: High-risk reheated meats and raw egg mayonnaise if not fresh!"),
        FoodPlace("r2", "SDR Restaurant and Cafe", "Restaurant", 4.1, "Known for modern multi-cuisine dishes, hot coffee, burgers, and dynamic menu. Note: Keep a check on high-calorie fast food and sodium!"),
        FoodPlace("r3", "New Kanis Restaurant", "Restaurant", 4.0, "Legendary local non-veg joint with rich gravies, parotta, and biryani. Note: Can be high in ghee and oil. High risk of acidity or reflux!"),
        FoodPlace("r4", "Khalids Multicuisine Restaurant", "Restaurant", 4.1, "Excellent family restaurant offering Indian, Chinese, and Tandoori items. Note: Check for added MSG or heavy oil in starters!"),
        FoodPlace("r5", "AK & Sons Restaurant", "Restaurant", 4.0, "Great location and local favorites with reliable hygiene. Note: Ask for custom less-salt options if needed!"),
        FoodPlace("r6", "PETTI", "Restaurant", 4.3, "Highly rated, quirky modern dining experience with unique snacks and food platters."),
        
        // Juice Shops
        FoodPlace("j1", "Siron Juice Park (WGC Road)", "Juice Shop", 4.1, "Popular spot for cold milkshakes and freshly squeezed juices. Note: Raw ice hygiene can be critical. Request fresh milk or pasteurized cream!"),
        FoodPlace("j2", "Johan’s Refresh Bar", "Juice Shop", 4.4, "Top-rated juice shop in Thoothukudi with dynamic menu and quality fruits. Note: Refreshing but ask for zero sugar for diabetic profiles!"),
        FoodPlace("j3", "Arul Juice Park (AC)", "Juice Shop", 4.1, "Air-conditioned juice stall with spacious seating. Note: Great hygiene, but check raw milk handling!"),
        FoodPlace("j4", "Siron Juice Park (Palai Road)", "Juice Shop", 3.9, "Consistent fresh fruit juices on Palai road. Note: Ensure they wash raw fruits thoroughly before extracting juice!"),
        
        // Snack Shops
        FoodPlace("s1", "Venkateswara Snacks Corner", "Snack Shop", 4.1, "Famous local samosas, cutlets, and vadai. Note: Deep fried snacks should be consumed fresh to avoid rancid oil issues!"),
        FoodPlace("s2", "Arya Sweets and Bakes", "Snack Shop", 4.3, "Premium traditional sweets (laddu, halwa) and baked pastries. Note: Rich in refined sugar and flour!"),
        FoodPlace("s3", "Ganesh Bakery (Famous for Macaroons)", "Snack Shop", 4.5, "World-famous Thoothukudi Macaroons (egg white, sugar, cashew nuts). Note: Exquisite local delicacy! Natural ingredients, but extremely high in sugar."),
        FoodPlace("s4", "Gnanam Bakery", "Snack Shop", 4.5, "Highly rated local bakery with fresh bread, cream cakes, and tea-time snacks."),
        FoodPlace("s5", "LALA SWEETS", "Snack Shop", 4.8, "Superb ratings for premium dairy sweets and savories. Note: Check expiration dates of milk sweets!"),
        FoodPlace("s6", "Pothigai Sweets", "Snack Shop", 4.3, "Traditional sweet stall with authentic local flavors. Note: Reliable standards, but rich desserts!"),
        FoodPlace("s7", "SELLA CHAATS", "Snack Shop", 4.9, "Highly popular chat center (pani puri, bhel puri, pav bhaji). Note: Safe water hygiene is key for Pani Puri!")
    )

    fun selectPlace(place: FoodPlace?) {
        _selectedPlace.value = place
        _placeAdvice.value = ""
        if (place == null) {
            _placeAdviceLoading.value = false
            return
        }
        _placeAdviceLoading.value = true

        viewModelScope.launch {
            val sysInstruction = buildSystemInstruction(userProfile.value)
            val prompt = """
                The user is interested in visiting or ordering from the following establishment in Thoothukudi:
                - Name: ${place.name}
                - Category/Type: ${place.type}
                - Google Rating: ${place.rating} Stars
                - Details: ${place.specialNotes}
                
                Please generate a custom, highly tailored Food Safety & Dietary Advisory for this place:
                1. HIGHLIGHT GOOGLE RATINGS: Briefly acknowledge their high-repute and people's rating.
                2. HYGIENE & STORAGE RISKS: Discuss specific food safety risks associated with this category of food in tropical environments like Thoothukudi (e.g. for restaurants, mention reheated meat/gravies; for juice shops, highlight water hygiene, fresh ice, and washing of raw fruits; for bakeries/snack shops, highlight oil freshness or expiration of milk sweets).
                3. INCORRECT TEMPERATURE STORAGE RISKS: Clearly explain the risks of leaving foods in the "Danger Zone" (4°C to 60°C) and why proper hot/cold holding is vital here.
                4. PERSONALIZED DIETARY RECOMMENDATION: Relate their menu items directly to the user's specific health profile (gender, age, health issues, height, weight). Highlight what items they can enjoy safely, what items they must absolutely avoid (e.g., Macaroons for a diabetic, spicy chats for acid reflux, deep-fried items for weight concerns), and what safe modifications to request (e.g., "ask for no added sugar in fresh juice" or "request raw mayo on the side").
                
                Keep the tone helpful, professional, and clear. Avoid engineering jargon.
            """.trimIndent()

            val response = GeminiClient.generateContent(prompt = prompt, systemInstruction = sysInstruction)
            _placeAdvice.value = response
            _placeAdviceLoading.value = false
        }
    }

    // --- Private Helper Prompt Builder ---
    private fun buildSystemInstruction(profile: UserProfile?): String {
        val profileDetails = if (profile != null) {
            "User Profile Info:\n" +
            "- Age: ${profile.age}\n" +
            "- Gender: ${profile.gender}\n" +
            "- Health Issues/Dietary Conditions: ${profile.healthIssues}\n" +
            "- Height: ${profile.height} cm\n" +
            "- Weight: ${profile.weight} kg\n" +
            "- Calculated BMI: ${String.format("%.1f", profile.weight / ((profile.height/100) * (profile.height/100)))}"
        } else {
            "No personalized user health profile has been configured yet."
        }

        return """
            You are "EatRite AI", an expert food safety, security, and personalized dietary health assistant.
            
            The user has provided their physical and health profile details below. You MUST analyze all user requests, product scans, and restaurant questions through the lens of this health profile to provide personalized food safety, preservation, and dietary recommendations:
            $profileDetails
            
            CRITICAL RESPONSIBILITIES & BEHAVIOR:
            1. FOOD SAFETY & SPOILAGE: Be an absolute expert in identifying food spoilage indicators (smell, color changes, mold, texture) and food security.
            2. PERSONALIZED DIETARY RECOMMENDATIONS: If the user has health issues (e.g., diabetes, hypertension, allergies, acid reflux, thyroid, high cholesterol), flag any ingredients or additives that are dangerous for them. Recommend safer alternatives. Use their physical details (height, weight, BMI) to give context-aware dietary advice.
            3. TEMPERATURE & STORAGE RISKS: If the user asks about storage, spoil, or temperature, ALWAYS explain the exact scientific risks associated with incorrect storage (e.g., the food temperature "danger zone" of 40°F - 140°F or 4°C - 60°C where bacteria multiply rapidly, risks of pathogens like Salmonella, E. coli, Listeria, and Clostridium Botulinum).
            4. RESTAURANT & LOCAL STREET FOOD HYGIENE: Help users evaluate dishes from local juice stalls, bakeries, or restaurants, advising them on high-risk items (e.g., raw juices, unpasteurized dairy, stale fried snacks, raw egg mayonnaise, reheated meat) and safe dining precautions.
            5. CLEAR, EMPATHETIC, AND ACCESSIBLE: Break down complex chemical preservatives (e.g., Sodium Benzoate, MSG, BHA, Sulfites, artificial sweeteners like Aspartame) into simple, understandable terms.
        """.trimIndent()
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}

data class FoodPlacePreset(
    val barcode: String,
    val name: String,
    val ingredients: String
)
