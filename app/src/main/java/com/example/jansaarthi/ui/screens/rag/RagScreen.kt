package com.example.jansaarthi.ui.screens.rag

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jansaarthi.data.model.RagSource
import com.example.jansaarthi.ui.theme.AppLogo
import com.example.jansaarthi.ui.theme.isAppInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RagScreen(
    onBack: () -> Unit,
    viewModel: RagViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isAppInDarkTheme()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Dialog & Menu States
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showModeInfoDialog by remember { mutableStateOf(false) }

    // Design Tokens matching Reference
    val bgColor = if (isDark) Color(0xFF0B1220) else Color(0xFFFAF8F2)
    val navyColor = if (isDark) Color(0xFFF5F7FA) else Color(0xFF17243A)
    val secondaryTextColor = if (isDark) Color(0xFFAAB4C3) else Color(0xFF657080)
    val cardBgColor = if (isDark) Color(0xFF141F32) else Color(0xFFFFFFFF)
    val saffronColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
    val saffronBadgeBg = if (isDark) Color(0xFF332008) else Color(0xFFFEF3E2)

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size)
        }
    }

    // Chat History Dialog
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = navyColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Conversation History",
                        fontWeight = FontWeight.Bold,
                        color = navyColor,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (uiState.messages.isEmpty()) {
                        Text(
                            text = "No active conversation. Start by asking a question below!",
                            color = secondaryTextColor,
                            fontSize = 14.sp
                        )
                    } else {
                        Text(
                            text = "Current session contains ${uiState.messages.size} message(s).",
                            color = secondaryTextColor,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val userQueries = uiState.messages.filter { it.isUser }
                        if (userQueries.isNotEmpty()) {
                            Text(
                                text = "Recent Questions:",
                                color = navyColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            userQueries.takeLast(4).forEach { q ->
                                Text(
                                    text = "• ${q.text}",
                                    color = secondaryTextColor,
                                    fontSize = 12.5.sp,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (uiState.messages.isNotEmpty()) {
                    TextButton(onClick = {
                        viewModel.clearConversation()
                        showHistoryDialog = false
                    }) {
                        Text(
                            text = "Clear History",
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text(
                        text = "Close",
                        color = saffronColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            containerColor = cardBgColor,
            shape = RoundedCornerShape(18.dp)
        )
    }

    // Search Mode Info Dialog
    if (showModeInfoDialog) {
        AlertDialog(
            onDismissRequest = { showModeInfoDialog = false },
            title = {
                Text(
                    text = "JANSAARTHI Search Modes",
                    fontWeight = FontWeight.Bold,
                    color = navyColor,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SearchModeInfoRow(
                        title = "Hybrid Search (Recommended)",
                        description = "Combines semantic context understanding with exact keyword verification for optimal accuracy.",
                        icon = Icons.Default.Bolt,
                        iconTint = Color(0xFFF59E0B),
                        isDark = isDark
                    )
                    SearchModeInfoRow(
                        title = "Semantic Search",
                        description = "Understands natural conversational language and conceptual eligibility matching.",
                        icon = Icons.Default.Layers,
                        iconTint = navyColor,
                        isDark = isDark
                    )
                    SearchModeInfoRow(
                        title = "Keyword Search",
                        description = "Performs strict lexical matching across official scheme names, departments, and policy numbers.",
                        icon = Icons.Default.Search,
                        iconTint = navyColor,
                        isDark = isDark
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showModeInfoDialog = false }) {
                    Text(
                        text = "Got it",
                        color = saffronColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = cardBgColor,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            // Header matching Reference Image
            Surface(
                color = bgColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Navigation
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = navyColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // JANSAARTHI Brand Logo
                    AppLogo(
                        modifier = Modifier.size(34.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Title & Saffron "AI Assistant" Pill
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "JANSAARTHI",
                            fontSize = 16.5.sp,
                            fontWeight = FontWeight.Black,
                            color = navyColor,
                            letterSpacing = 0.5.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = saffronBadgeBg,
                            modifier = Modifier.padding(top = 1.dp)
                        ) {
                            Text(
                                text = "AI Assistant",
                                color = saffronColor,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Right Icon: Chat History
                    IconButton(onClick = { showHistoryDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = navyColor,
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    // Right Icon: Overflow Menu
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = navyColor,
                                modifier = Modifier.size(23.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            modifier = Modifier.background(cardBgColor)
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Conversation", fontSize = 13.5.sp, color = navyColor) },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = navyColor, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.clearConversation()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Search Mode Info", fontSize = 13.5.sp, color = navyColor) },
                                leadingIcon = {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = navyColor, modifier = Modifier.size(18.dp))
                                },
                                onClick = {
                                    showMoreMenu = false
                                    showModeInfoDialog = true
                                }
                            )
                            if (uiState.messages.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Clear Chat", fontSize = 13.5.sp, color = Color(0xFFDC2626)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    },
                                    onClick = {
                                        showMoreMenu = false
                                        viewModel.clearConversation()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // ChatGPT-Style Message Composer Container
            AiMessageComposer(
                input = uiState.queryInput,
                onInputChange = { viewModel.onQueryInputChanged(it) },
                onSend = {
                    keyboardController?.hide()
                    viewModel.submitQuery()
                },
                currentMode = uiState.selectedMode,
                onModeSelected = { viewModel.onModeSelected(it) },
                onNewConversation = { viewModel.clearConversation() },
                onClearChat = { viewModel.clearConversation() },
                isLoading = uiState.isLoading,
                isDark = isDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Faint bottom decorative curve behind empty state
            if (uiState.messages.isEmpty() && !uiState.isLoading) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    val w = size.width
                    val h = size.height
                    val waveColor = if (isDark) Color(0xFF131D2E).copy(alpha = 0.5f) else Color(0xFFF3ECE0).copy(alpha = 0.6f)
                    val path = Path().apply {
                        moveTo(0f, h * 0.45f)
                        quadraticTo(w * 0.35f, h * 0.25f, w * 0.65f, h * 0.55f)
                        quadraticTo(w * 0.85f, h * 0.75f, w, h * 0.50f)
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(path, color = waveColor)
                }
            }

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Error banner if any
                AnimatedVisibility(
                    visible = uiState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    uiState.errorMessage?.let { error ->
                        Surface(
                            color = if (isDark) Color(0xFF2A1515) else Color(0xFFFEE2E2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF5A1E1E) else Color(0xFFFCA5A5))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    color = if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                if (uiState.lastFailedQuery != null) {
                                    TextButton(onClick = { viewModel.retryLastQuery() }) {
                                        Text(
                                            "Retry",
                                            color = Color(0xFFDC2626),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                IconButton(onClick = { viewModel.clearError() }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFFDC2626),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Empty Conversation State OR Active Conversation Messages
                if (uiState.messages.isEmpty() && !uiState.isLoading) {
                    AiEmptyConversationState(
                        isDark = isDark,
                        navyColor = navyColor,
                        secondaryTextColor = secondaryTextColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            RagMessageItem(
                                message = message,
                                sourcesTitle = "Verified Scheme Sources",
                                isDark = isDark
                            )
                        }

                        if (uiState.isLoading) {
                            item {
                                RagTypingIndicator(isDark = isDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Centered Empty State matching Reference Image:
 * - Speech bubble with "..." and 4-point sparkle stars on soft warm organic background
 * - Bold Heading: "JANSAARTHI AI Assistant"
 * - Subtitle: "Ask anything about Indian government schemes, eligibility, benefits and application process."
 * - No suggestions or prompt cards.
 */
@Composable
private fun AiEmptyConversationState(
    isDark: Boolean,
    navyColor: Color,
    secondaryTextColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Chat bubble with 4-point stars on warm organic background
        AiAssistantEmptyIllustration(
            isDark = isDark,
            modifier = Modifier.size(140.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "JANSAARTHI AI Assistant",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = navyColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Ask anything about Indian government\nschemes, eligibility, benefits and application process.",
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Normal,
            color = secondaryTextColor,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

/**
 * Custom Canvas Illustration of Speech Bubble with "..." and two 4-point sparkle stars.
 */
@Composable
fun AiAssistantEmptyIllustration(
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val blobColor = if (isDark) Color(0xFF231D16) else Color(0xFFFDF4EA)
    val navyColor = if (isDark) Color(0xFFE2E8F0) else Color(0xFF17243A)
    val starColor = Color(0xFFD97706)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // 1. Warm organic rounded background blob
        val bw = w * 0.78f
        val bh = h * 0.72f
        val bx = cx - bw / 2f
        val by = cy - bh / 2f
        val blobPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = bx,
                    top = by,
                    right = bx + bw,
                    bottom = by + bh,
                    radiusX = bw * 0.38f,
                    radiusY = bh * 0.38f
                )
            )
        }
        drawPath(blobPath, color = blobColor)

        // 2. Chat Bubble outline
        val bubbleW = w * 0.40f
        val bubbleH = h * 0.29f
        val bubbleLeft = cx - bubbleW / 2f - 2.dp.toPx()
        val bubbleTop = cy - bubbleH / 2f + 2.dp.toPx()
        val cornerR = 9.dp.toPx()
        val strokeW = 2.4.dp.toPx()

        val bubblePath = Path().apply {
            moveTo(bubbleLeft + cornerR, bubbleTop)
            lineTo(bubbleLeft + bubbleW - cornerR, bubbleTop)
            quadraticTo(bubbleLeft + bubbleW, bubbleTop, bubbleLeft + bubbleW, bubbleTop + cornerR)
            lineTo(bubbleLeft + bubbleW, bubbleTop + bubbleH - cornerR)
            quadraticTo(bubbleLeft + bubbleW, bubbleTop + bubbleH, bubbleLeft + bubbleW - cornerR, bubbleTop + bubbleH)

            // Bottom edge with tail at bottom-left
            lineTo(bubbleLeft + cornerR + 11.dp.toPx(), bubbleTop + bubbleH)
            lineTo(bubbleLeft + 2.dp.toPx(), bubbleTop + bubbleH + 8.dp.toPx()) // Tail point
            lineTo(bubbleLeft + cornerR + 4.dp.toPx(), bubbleTop + bubbleH)

            lineTo(bubbleLeft + cornerR, bubbleTop + bubbleH)
            quadraticTo(bubbleLeft, bubbleTop + bubbleH, bubbleLeft, bubbleTop + bubbleH - cornerR)
            lineTo(bubbleLeft, bubbleTop + cornerR)
            quadraticTo(bubbleLeft, bubbleTop, bubbleLeft + cornerR, bubbleTop)
            close()
        }

        drawPath(
            path = bubblePath,
            color = navyColor,
            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 3. Three horizontal dots inside the bubble
        val dotRadius = 1.9.dp.toPx()
        val dotSpacing = 5.8.dp.toPx()
        val dotsY = bubbleTop + bubbleH / 2f
        val dotsCenterX = bubbleLeft + bubbleW / 2f
        drawCircle(color = navyColor, radius = dotRadius, center = Offset(dotsCenterX - dotSpacing, dotsY))
        drawCircle(color = navyColor, radius = dotRadius, center = Offset(dotsCenterX, dotsY))
        drawCircle(color = navyColor, radius = dotRadius, center = Offset(dotsCenterX + dotSpacing, dotsY))

        // 4. Two Four-Point Sparkle Stars on Top-Right
        // Larger Star 1
        val star1X = bubbleLeft + bubbleW + 2.dp.toPx()
        val star1Y = bubbleTop - 4.dp.toPx()
        val star1Size = 7.2.dp.toPx()
        drawSparkleStar(this, star1X, star1Y, star1Size, starColor)

        // Smaller Star 2
        val star2X = bubbleLeft + bubbleW + 12.dp.toPx()
        val star2Y = bubbleTop + 7.dp.toPx()
        val star2Size = 4.2.dp.toPx()
        drawSparkleStar(this, star2X, star2Y, star2Size, starColor)
    }
}

private fun drawSparkleStar(
    scope: DrawScope,
    centerX: Float,
    centerY: Float,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(centerX, centerY - radius)
        quadraticTo(centerX, centerY, centerX + radius, centerY)
        quadraticTo(centerX, centerY, centerX, centerY + radius)
        quadraticTo(centerX, centerY, centerX - radius, centerY)
        quadraticTo(centerX, centerY, centerX, centerY - radius)
        close()
    }
    scope.drawPath(path, color = color)
}

/**
 * Redesigned ChatGPT-Style Message Composer Container:
 * - Top row: Multiline text input expanding naturally with placeholder
 * - Bottom row: Plus action, Search-mode selector pill with floating menu, Circular Navy Send button
 */
@Composable
fun AiMessageComposer(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    currentMode: String,
    onModeSelected: (String) -> Unit,
    onNewConversation: () -> Unit,
    onClearChat: () -> Unit,
    isLoading: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    var showPlusMenu by remember { mutableStateOf(false) }
    var showModeDropdown by remember { mutableStateOf(false) }

    val cardBg = if (isDark) Color(0xFF141F32) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF24344D) else Color(0xFFE5E7EB)
    val inputTextColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF17243A)
    val placeholderColor = if (isDark) Color(0xFF8897AE) else Color(0xFF94A3B8)
    val pillBg = if (isDark) Color(0xFF1E2D42) else Color(0xFFF1F3F5)
    val navySendBg = if (isDark) Color(0xFF253B5E) else Color(0xFF17243A)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(26.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp, start = 14.dp, end = 10.dp)
        ) {
            // Top Section: Multiline Text Input
            BasicTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 28.dp, max = 130.dp),
                textStyle = TextStyle(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = inputTextColor,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(if (isDark) Color(0xFFFBBF24) else Color(0xFF17243A)),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (input.isEmpty()) {
                            Text(
                                text = "Ask a question about government schemes...",
                                style = TextStyle(
                                    fontSize = 14.5.sp,
                                    color = placeholderColor,
                                    lineHeight = 20.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Section Row: Plus Button, Mode Selector Pill, Send Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Plus Action Button
                Box {
                    IconButton(
                        onClick = { showPlusMenu = true },
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = pillBg,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Actions",
                            tint = if (isDark) Color(0xFFCBD5E1) else Color(0xFF4B5563),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showPlusMenu,
                        onDismissRequest = { showPlusMenu = false },
                        modifier = Modifier.background(cardBg)
                    ) {
                        DropdownMenuItem(
                            text = { Text("New Conversation", fontSize = 13.5.sp, color = inputTextColor) },
                            leadingIcon = {
                                Icon(Icons.Default.Add, contentDescription = null, tint = inputTextColor, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showPlusMenu = false
                                onNewConversation()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Chat", fontSize = 13.5.sp, color = Color(0xFFDC2626)) },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showPlusMenu = false
                                onClearChat()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Search-Mode Selector Pill
                Box {
                    val modeLabel = when (currentMode) {
                        "hybrid" -> "Hybrid"
                        "semantic" -> "Semantic"
                        "lexical" -> "Keyword"
                        else -> "Hybrid"
                    }
                    val modeIcon = when (currentMode) {
                        "hybrid" -> Icons.Default.Bolt
                        "semantic" -> Icons.Default.Layers
                        "lexical" -> Icons.Default.Search
                        else -> Icons.Default.Bolt
                    }
                    val modeIconTint = when (currentMode) {
                        "hybrid" -> Color(0xFFF59E0B)
                        else -> if (isDark) Color(0xFFCBD5E1) else Color(0xFF4B5563)
                    }

                    Surface(
                        onClick = { showModeDropdown = true },
                        shape = RoundedCornerShape(16.dp),
                        color = pillBg,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = modeIcon,
                                contentDescription = null,
                                tint = modeIconTint,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = modeLabel,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1F2937)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select mode",
                                tint = if (isDark) Color(0xFF94A3B8) else Color(0xFF6B7280),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Dropdown Menu with 3 Search Options & Checkmark
                    DropdownMenu(
                        expanded = showModeDropdown,
                        onDismissRequest = { showModeDropdown = false },
                        modifier = Modifier
                            .background(cardBg)
                            .widthIn(min = 160.dp)
                    ) {
                        val modes = listOf(
                            Triple("hybrid", "Hybrid", Icons.Default.Bolt),
                            Triple("semantic", "Semantic", Icons.Default.Layers),
                            Triple("lexical", "Keyword", Icons.Default.Search)
                        )

                        modes.forEach { (modeKey, label, icon) ->
                            val isSelected = currentMode == modeKey
                            val itemBg = if (isSelected) {
                                if (isDark) Color(0xFF23354E) else Color(0xFFEBF3FE)
                            } else {
                                Color.Transparent
                            }

                            DropdownMenuItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(itemBg),
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isDark) Color.White else Color(0xFF1F2937)
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF1D4ED8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (modeKey == "hybrid") Color(0xFFF59E0B) else (if (isDark) Color(0xFFCBD5E1) else Color(0xFF4B5563)),
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    onModeSelected(modeKey)
                                    showModeDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Circular Navy Send Button
                val canSend = input.isNotBlank() && !isLoading
                val sendBtnBg = when {
                    canSend -> navySendBg
                    else -> if (isDark) Color(0xFF1E2D42) else Color(0xFFCBD5E1)
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(sendBtnBg, CircleShape)
                        .clip(CircleShape)
                        .clickable(enabled = canSend) { onSend() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchModeInfoRow(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color(0xFFF1F5F9) else Color(0xFF17243A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = if (isDark) Color(0xFFAAB4C3) else Color(0xFF657080),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun RagMessageItem(
    message: RagMessage,
    sourcesTitle: String,
    isDark: Boolean = false
) {
    val navyColor = if (isDark) Color(0xFFF5F7FA) else Color(0xFF17243A)
    val userBubbleBg = if (isDark) Color(0xFF1E2F4A) else Color(0xFF17243A)
    val aiBubbleBg = if (isDark) Color(0xFF141F32) else Color(0xFFFFFFFF)
    val aiBorderColor = if (isDark) Color(0xFF24344D) else Color(0xFFE5E7EB)
    val textPrimary = if (isDark) Color(0xFFF1F5F9) else Color(0xFF17243A)

    if (message.isUser) {
        // User Message Bubble (Aligned Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                color = userBubbleBg,
                modifier = Modifier.widthIn(max = 310.dp)
            ) {
                Text(
                    text = message.text,
                    style = TextStyle(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    } else {
        // AI Response Bubble (Aligned Left)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Column(
                modifier = Modifier.widthIn(max = 345.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp),
                    color = aiBubbleBg,
                    border = BorderStroke(1.dp, aiBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // AI Label & Mode Tag Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppLogo(modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "JANSAARTHI AI",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = navyColor
                                )
                            }

                            if (!message.retrievalMode.isNullOrEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isDark) Color(0xFF1F2D42) else Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = message.retrievalMode.uppercase(),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFF93C5FD) else Color(0xFF2563EB),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        FormattedMessageText(
                            text = message.text,
                            textColor = textPrimary
                        )
                    }
                }

                // Expandable Sources List if available
                if (message.sources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    RagSourcesSection(
                        sources = message.sources,
                        title = sourcesTitle,
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
fun RagSourcesSection(
    sources: List<RagSource>,
    title: String,
    isDark: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val cardBg = if (isDark) Color(0xFF141F32) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0xFF24344D) else Color(0xFFE5E7EB)
    val textColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF17243A)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$title (${sources.size})",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = if (isDark) Color(0xFFAAB4C3) else Color(0xFF657080),
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sources.forEach { source ->
                        RagSourceCard(source = source, isDark = isDark)
                    }
                }
            }
        }
    }
}

@Composable
fun RagSourceCard(
    source: RagSource,
    isDark: Boolean = false
) {
    val context = LocalContext.current
    val hasOfficialUrl = source.sourcePlatform.equals("myScheme.gov.in", ignoreCase = true)
    val officialUrl = if (hasOfficialUrl) "https://www.myscheme.gov.in/" else null

    val cardBg = if (isDark) Color(0xFF1E2D42) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF2C3E5A) else Color(0xFFE2E8F0)
    val titleColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF17243A)
    val descColor = if (isDark) Color(0xFFAAB4C3) else Color(0xFF657080)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = source.schemeName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    modifier = Modifier.weight(1f)
                )

                if (source.schemeId.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isDark) Color(0xFF2A3D5C) else Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = source.schemeId,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (source.category.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = source.category,
                    fontSize = 10.5.sp,
                    color = descColor
                )
            }

            if (source.snippet.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = source.snippet,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = descColor
                )
            }

            if (officialUrl != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(officialUrl))
                            context.startActivity(intent)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Source: ${source.sourcePlatform}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open Source",
                        tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RagTypingIndicator(
    isDark: Boolean = false
) {
    val bubbleBg = if (isDark) Color(0xFF141F32) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF24344D) else Color(0xFFE5E7EB)
    val textColor = if (isDark) Color(0xFFAAB4C3) else Color(0xFF657080)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = bubbleBg,
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
                )
                Text(
                    text = "JANSAARTHI AI is researching schemes...",
                    fontSize = 12.5.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = textColor
                )
            }
        }
    }
}
