package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScanHistory
import com.example.data.UserProfile
import com.example.ui.theme.*
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.FoodPlace
import com.example.viewmodel.FoodSafetyViewModel
import com.example.viewmodel.ScanResultState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: FoodSafetyViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val userProfile by viewModel.userProfile.collectAsState()

    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                val items = listOf(
                    Triple(0, "Scanner", Icons.Default.QrCodeScanner),
                    Triple(1, "Restaurants", Icons.Default.Restaurant),
                    Triple(2, "AI Chat", Icons.Default.Forum),
                    Triple(3, "Health Profile", Icons.Default.AccountCircle)
                )
                items.forEach { (index, label, icon) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_$index")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Status bar spacer/header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "Logo",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "EatRite Food Safety",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val profileText = if (userProfile != null) {
                            "Health Profile Active (Age: ${userProfile?.age}, ${userProfile?.healthIssues})"
                        } else {
                            "⚠️ Set Health Profile for custom warnings!"
                        }
                        Text(
                            text = profileText,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> ScannerTab(viewModel = viewModel)
                    1 -> RestaurantsTab(viewModel = viewModel)
                    2 -> ChatTab(viewModel = viewModel)
                    3 -> ProfileTab(viewModel = viewModel)
                }
            }
        }
    }
}

// --- SCANNER SCREEN ---
@Composable
fun ScannerTab(viewModel: FoodSafetyViewModel) {
    val scanResult by viewModel.scanResult.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()
    var inputBarcode by remember { mutableStateOf("") }
    var showPresetSelector by remember { mutableStateOf(false) }
    var isSimulatingScan by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // Pulsing line simulation for scanning
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = { it }),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Food Barcode & Ingredient Checker",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Scan barcodes, snap ingredient labels, or enter manually to reveal harmful preservatives, added chemicals, and safety advisories.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Mock Viewfinder Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isSimulatingScan) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Analyzing packaging...",
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(top = 60.dp)
                    )
                } else {
                    // Static scanning overlay lines
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val height = size.height
                        val width = size.width
                        val yPos = height * pulseOffset
                        
                        // Drawing corner brackets
                        val bracketLen = 40.dp.toPx()
                        val stroke = 3.dp.toPx()
                        
                        // Top Left
                        drawLine(Color.Green, Offset(10.dp.toPx(), 10.dp.toPx()), Offset(10.dp.toPx() + bracketLen, 10.dp.toPx()), stroke)
                        drawLine(Color.Green, Offset(10.dp.toPx(), 10.dp.toPx()), Offset(10.dp.toPx(), 10.dp.toPx() + bracketLen), stroke)
                        
                        // Top Right
                        drawLine(Color.Green, Offset(width - 10.dp.toPx(), 10.dp.toPx()), Offset(width - 10.dp.toPx() - bracketLen, 10.dp.toPx()), stroke)
                        drawLine(Color.Green, Offset(width - 10.dp.toPx(), 10.dp.toPx()), Offset(width - 10.dp.toPx(), 10.dp.toPx() + bracketLen), stroke)

                        // Bottom Left
                        drawLine(Color.Green, Offset(10.dp.toPx(), height - 10.dp.toPx()), Offset(10.dp.toPx() + bracketLen, height - 10.dp.toPx()), stroke)
                        drawLine(Color.Green, Offset(10.dp.toPx(), height - 10.dp.toPx()), Offset(10.dp.toPx(), height - 10.dp.toPx() - bracketLen), stroke)

                        // Bottom Right
                        drawLine(Color.Green, Offset(width - 10.dp.toPx(), height - 10.dp.toPx()), Offset(width - 10.dp.toPx() - bracketLen, height - 10.dp.toPx()), stroke)
                        drawLine(Color.Green, Offset(width - 10.dp.toPx(), height - 10.dp.toPx()), Offset(width - 10.dp.toPx(), height - 10.dp.toPx() - bracketLen), stroke)

                        // Pulsing red scan line
                        drawLine(Color.Red, Offset(15.dp.toPx(), yPos), Offset(width - 15.dp.toPx(), yPos), 4.dp.toPx())
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Align Barcode / Ingredients List inside Frame",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showPresetSelector = !showPresetSelector },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("preset_scan_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Fastfood, contentDescription = "Presets", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Select Preset", fontSize = 13.sp)
            }

            // Snap photo scan (Mock trigger)
            Button(
                onClick = {
                    isSimulatingScan = true
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(1200)
                        isSimulatingScan = false
                        // Randomly choose one preset to scan via mock label photo
                        val randomPreset = viewModel.barcodePresets.random()
                        viewModel.scanProduct(randomPreset.barcode)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("photo_scan_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = "Photo Scan", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Snap Label Photo", fontSize = 13.sp)
            }
        }

        // Expanded Presets List
        AnimatedVisibility(visible = showPresetSelector) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Demo Food Presets:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    viewModel.barcodePresets.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPresetSelector = false
                                    inputBarcode = preset.barcode
                                    viewModel.scanProduct(preset.barcode)
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(preset.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Barcode: ${preset.barcode}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manual Input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputBarcode,
                onValueChange = { inputBarcode = it },
                placeholder = { Text("Enter Barcode or Product Name", fontSize = 14.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        if (inputBarcode.isNotBlank()) {
                            viewModel.scanProduct(inputBarcode)
                        }
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("barcode_input_field"),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    keyboardController?.hide()
                    if (inputBarcode.isNotBlank()) {
                        viewModel.scanProduct(inputBarcode)
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .size(54.dp)
                    .testTag("submit_scan_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scan results display
        when (val state = scanResult) {
            is ScanResultState.Loading -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Running ingredient safety analysis with Gemini...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            is ScanResultState.Success -> {
                val scan = state.scan
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Gemini AI Analysis Result",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = scan.productName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "FSSAI Safety Rating: ${scan.fssaiRating}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (scan.fssaiRating.contains("4") || scan.fssaiRating.contains("5")) SafeGreen else WarningOrange
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Preservatives Detected:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(scan.preservatives, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Added Ingredients & Additives:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(scan.addedIngredients, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(12.dp)
                                )
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.HealthAndSafety,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Personalized Health Advisory:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = scan.safetyAdvice,
                                    fontSize = 12.5.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.resetScanner() },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Clear Result")
                        }
                    }
                }
            }
            is ScanResultState.Error -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                    }
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(8.dp))

        // History Log
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Scan & Check History",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (scanHistory.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearScanHistory() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }

        if (scanHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history available yet. Start checking foods!",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                scanHistory.forEach { history ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    history.productName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "FSSAI: ${history.fssaiRating}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Code: ${history.barcodeOrName}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.deleteScan(history.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- RESTAURANTS TAB ---
@Composable
fun RestaurantsTab(viewModel: FoodSafetyViewModel) {
    val selectedPlace by viewModel.selectedPlace.collectAsState()
    val placeAdvice by viewModel.placeAdvice.collectAsState()
    val placeAdviceLoading by viewModel.placeAdviceLoading.collectAsState()
    
    var filterCategory by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Thoothukudi Food Establishments",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Select any restaurant, juice park, or snack corner in Thoothukudi to view Google ratings and use AI to evaluate food hygiene, spoilage markers, and custom dietary risks.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Restaurant", "Juice Shop", "Snack Shop").forEach { category ->
                val selected = filterCategory == category
                Box(
                    modifier = Modifier
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(20.dp)
                        )
                        .border(
                            1.dp,
                            if (selected) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { filterCategory = category }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = category + "s",
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        // Restaurant Selection & Details Card
        selectedPlace?.let { place ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val categoryIcon = when (place.type) {
                                    "Juice Shop" -> Icons.Default.LocalDrink
                                    "Snack Shop" -> Icons.Default.Fastfood
                                    else -> Icons.Default.Restaurant
                                }
                                Icon(
                                    imageVector = categoryIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = place.type.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = place.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { viewModel.selectPlace(null) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${place.rating} Google Rating",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Thoothukudi, IN",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Notes: ${place.specialNotes}",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        if (placeAdviceLoading) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Consulting EatRite AI...", fontSize = 12.sp)
                            }
                        } else {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Advice",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Personalized Hygiene & Safety Advice:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = placeAdvice,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Grid/List of places
        val filteredList = if (filterCategory == "All") {
            viewModel.localPlaces
        } else {
            viewModel.localPlaces.filter { it.type == filterCategory }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            filteredList.forEach { place ->
                val isSelected = selectedPlace?.id == place.id
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectPlace(place) }
                        .testTag("restaurant_card_${place.id}"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    when (place.type) {
                                        "Juice Shop" -> SafeGreen.copy(alpha = 0.1f)
                                        "Snack Shop" -> WarningOrange.copy(alpha = 0.1f)
                                        else -> InfoBlue.copy(alpha = 0.1f)
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val placeIcon = when (place.type) {
                                "Juice Shop" -> Icons.Default.LocalDrink
                                "Snack Shop" -> Icons.Default.Fastfood
                                else -> Icons.Default.Restaurant
                            }
                            Icon(
                                imageVector = placeIcon,
                                contentDescription = null,
                                tint = when (place.type) {
                                    "Juice Shop" -> SafeGreen
                                    "Snack Shop" -> WarningOrange
                                    else -> InfoBlue
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = place.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = place.type,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${place.rating}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- CHAT SCREEN ---
@Composable
fun ChatTab(viewModel: FoodSafetyViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var chatInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Automatically scroll to bottom of chat thread when new message is added
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI Food Safety Assistant",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Multi-turn consultant on diet & spoilage risks",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(Icons.Default.Clear, contentDescription = "Clear Chat", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Bubble Flow
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chatMessages) { message ->
                ChatBubbleItem(message = message)
            }

            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Gemini is analyzing your request...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hot suggestions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "🌡️ Temperature dangers?",
                "🤢 Food spoilage markers?",
                "🍎 Allergy advice?"
            ).forEach { prompt ->
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.sendChatMessage(prompt) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = { Text("Ask about food safety, storage temperatures...", fontSize = 14.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                ),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (chatInput.isNotBlank()) {
                            viewModel.sendChatMessage(chatInput)
                            chatInput = ""
                            keyboardController?.hide()
                        }
                    }
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        viewModel.sendChatMessage(chatInput)
                        chatInput = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .size(54.dp)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatBubbleItem(message: ChatMessage) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgBrush = if (isUser) {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }

    val textColor = if (isUser) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = bgBrush,
                    shape = if (isUser) {
                        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
                    } else {
                        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
                    }
                )
                .padding(12.dp)
                .widthIn(max = 290.dp)
        ) {
            Text(
                text = message.content,
                color = textColor,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

// --- PROFILE TAB ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(viewModel: FoodSafetyViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()

    var gender by remember { mutableStateOf("Male") }
    var age by remember { mutableStateOf("25") }
    var healthIssues by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("170") }
    var weight by remember { mutableStateOf("65") }

    var expandedGenderMenu by remember { mutableStateOf(false) }

    // Sync input fields when existing profile is loaded from SQLite Room
    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            gender = profile.gender
            age = profile.age.toString()
            healthIssues = profile.healthIssues
            height = profile.height.toString()
            weight = profile.weight.toString()
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Personal Health Profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "This information is securely stored offline in your device database. The AI assistant uses this context to alert you to unsafe preservatives and customized dietary risks.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Status Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (userProfile != null) SafeGreen.copy(alpha = 0.1f) else WarningOrange.copy(alpha = 0.1f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (userProfile != null) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (userProfile != null) SafeGreen else WarningOrange
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (userProfile != null) "Personalization Active" else "Personalization Disabled",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = if (userProfile != null) SafeGreen else WarningOrange
                    )
                    Text(
                        text = if (userProfile != null) "AI recommendations are customized for your health issues." else "Fill in the details below to receive custom allergy and ingredient advice.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Form fields
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Gender Select Dropdown
                Text("Gender", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, modifier = Modifier.padding(bottom = 4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedGenderMenu,
                    onExpandedChange = { expandedGenderMenu = !expandedGenderMenu }
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGenderMenu) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("gender_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGenderMenu,
                        onDismissRequest = { expandedGenderMenu = false }
                    ) {
                        listOf("Male", "Female", "Other").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    gender = item
                                    expandedGenderMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Age Input
                Text("Age (Years)", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("age_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Height & Weight row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Height (cm)", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, modifier = Modifier.padding(bottom = 4.dp))
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("height_input_field"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Weight (kg)", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, modifier = Modifier.padding(bottom = 4.dp))
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("weight_input_field"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Health Issues & Allergies
                Text("Health Issues & Allergies", fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, modifier = Modifier.padding(bottom = 4.dp))
                OutlinedTextField(
                    value = healthIssues,
                    onValueChange = { healthIssues = it },
                    placeholder = { Text("e.g. Diabetes, Hypertension, Peanut allergy, Acid reflux, Gluten intolerance", fontSize = 13.5.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("health_issues_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        val parsedAge = age.toIntOrNull() ?: 25
                        val parsedHeight = height.toDoubleOrNull() ?: 170.0
                        val parsedWeight = weight.toDoubleOrNull() ?: 65.0
                        viewModel.saveProfile(
                            gender = gender,
                            age = parsedAge,
                            healthIssues = healthIssues.ifBlank { "None" },
                            height = parsedHeight,
                            weight = parsedWeight
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_profile_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Health Profile", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        val customApiKey by viewModel.customApiKeyFlow.collectAsState()
        var apiKeyInput by remember { mutableStateOf("") }
        LaunchedEffect(customApiKey) {
            apiKeyInput = customApiKey
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gemini API Key Configuration",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Custom API Key Override",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "By default, EatRite uses the system-provided Gemini key. You can optionally override it here with your own personal Gemini API Key if desired.",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    placeholder = { Text("System key is active. Enter custom key to override...", fontSize = 12.5.sp) },
                    singleLine = true,
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.saveCustomApiKey(apiKeyInput.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("save_api_key_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = "Save Key", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Custom API Key", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Food storage tips
        Text(
            text = "Essential Temperature Guidelines:",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("The Bacteria Danger Zone", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = DangerRed)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Leaving cooked or raw foods between 4°C and 60°C (40°F - 140°F) for over 2 hours allows rapid pathogen multiplication. Keep hot foods hot (> 60°C) and cold foods cold (< 4°C).",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
        }
    }
}
