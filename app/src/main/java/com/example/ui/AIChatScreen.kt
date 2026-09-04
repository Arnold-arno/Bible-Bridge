package com.example.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AIChatRole
import com.example.data.ChatMessage
import kotlinx.coroutines.launch

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AIChatScreen(
    viewModel: BibleViewModel,
    speakText: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val overrideModel by viewModel.overrideModel.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingChat.collectAsStateWithLifecycle()
    val contextBanner by viewModel.chatContextBanner.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    var chatHeaderImageIdx by remember { mutableIntStateOf(0) } // 0: Children Reading Bible, 1: Bible Study Desk
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    // Auto-scroll to bottom on new messages
    LaunchedEffect(chatMessages.size, isGenerating) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Top Hero Banner Section ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .height(135.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val bannerDrawable = if (chatHeaderImageIdx == 0) {
                    R.drawable.img_chat_header_banner_1785646733250
                } else {
                    R.drawable.img_read_header_plants_1785646748508
                }
                
                Image(
                    painter = painterResource(id = bannerDrawable),
                    contentDescription = "Bible Study Chat Hero Header",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { chatHeaderImageIdx = (chatHeaderImageIdx + 1) % 2 }
                )
                
                // Dark Gradient Overlay for optimal legibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.35f),
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )
                
                // Top Action Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Switch Image Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.clickable { chatHeaderImageIdx = (chatHeaderImageIdx + 1) % 2 }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (chatHeaderImageIdx == 0) "Fellowship View" else "Study Desk View",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    // Clear Chat Button
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.45f),
                        modifier = Modifier.size(32.dp).clickable { viewModel.clearChatMessages() }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Chat",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Bottom Banner Info
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00E676),
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BIBLE BRIDGE AI CHAT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676),
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when(currentRole) {
                            AIChatRole.GENERAL_ASSISTANT -> "Scripture Assistant & Fellowship Companion"
                            AIChatRole.DEEP_RESEARCH -> "Exegetical Study & Root Word Analyzer"
                            AIChatRole.DEVOTIONAL_PARTNER -> "Devotional Meditation & Quiet Time Guide"
                            AIChatRole.QUICK_HELPER -> "Quick Verse & Passage Reference Helper"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }

                // Context Banner (e.g. Daily Devotion or Research context)
                contextBanner?.let { bannerText ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = bannerText,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- Role & Model Selector Chips ---
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AIChatRole.entries.forEachIndexed { index, role ->
                        SegmentedButton(
                            selected = currentRole == role,
                            onClick = { viewModel.setChatRole(role) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = AIChatRole.entries.size),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when(role) {
                                    AIChatRole.GENERAL_ASSISTANT -> "General"
                                    AIChatRole.DEEP_RESEARCH -> "Research"
                                    AIChatRole.DEVOTIONAL_PARTNER -> "Devotion"
                                    AIChatRole.QUICK_HELPER -> "Quick"
                                },
                                fontSize = 11.sp,
                                fontWeight = if (currentRole == role) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

        // --- Chat Thread ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(chatMessages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        onCopy = { clipboardManager.setText(AnnotatedString(message.text)) },
                        onSpeak = { speakText("AI Assistant", message.text) }
                    )
                }

                if (isGenerating) {
                    item {
                        GeneratingIndicatorItem(roleTitle = currentRole.title, modelName = overrideModel ?: currentRole.defaultModel)
                    }
                }
            }
        }

        // --- Quick Research Prompt Suggestions ---
        if (chatMessages.size <= 2 && !isGenerating) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val suggestions = when(currentRole) {
                    AIChatRole.DEEP_RESEARCH -> listOf(
                        "Original Hebrew meaning of Hesed",
                        "Historical context of Psalm 23",
                        "Greek word Agape vs Phileo"
                    )
                    AIChatRole.DEVOTIONAL_PARTNER -> listOf(
                        "How to find peace in worry?",
                        "Help me write a daily prayer",
                        "Apply Proverbs 3:5-6 to work"
                    )
                    AIChatRole.QUICK_HELPER -> listOf(
                        "Key theme of Romans 8",
                        "Fast summary of Sermon on the Mount",
                        "Verses about hope"
                    )
                    else -> listOf(
                        "Explain John 3:16",
                        "What is the Fruit of the Spirit?",
                        "How to study the Psalms"
                    )
                }

                suggestions.forEach { prompt ->
                    SuggestionChip(
                        onClick = { viewModel.sendMessageToAI(prompt) },
                        label = { Text(prompt, fontSize = 12.sp) },
                        icon = { Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // --- Bottom Input Bar ---
        Surface(
            tonalElevation = 6.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = when(currentRole) {
                                AIChatRole.DEEP_RESEARCH -> "Ask for exegetical research, Greek/Hebrew root words..."
                                AIChatRole.DEVOTIONAL_PARTNER -> "Share reflection thoughts or ask for a prayer prompt..."
                                AIChatRole.QUICK_HELPER -> "Ask for a quick verse lookup or fast summary..."
                                else -> "Ask Bible Bridge Chat about scripture, prayer, or study..."
                            },
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input_field"),
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = {
                        if (textInput.isNotBlank() && !isGenerating) {
                            val msg = textInput
                            textInput = ""
                            viewModel.sendMessageToAI(msg)
                        }
                    },
                    enabled = textInput.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("ai_chat_send_button"),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message"
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onCopy: () -> Unit,
    onSpeak: () -> Unit
) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = R.drawable.john_14_26_helper_icon_1785381323913),
                contentDescription = "Bible Bridge Chat Icon",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            border = if (!isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)) else null
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bible Bridge Chat",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                val formattedText = remember(message.text) {
                    com.example.data.GeminiService.formatHumanText(message.text)
                }

                Text(
                    text = formattedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                if (!isUser) {
                    val context = LocalContext.current
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Read Aloud",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, message.text)
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Scripture Message")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Message",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratingIndicatorItem(roleTitle: String, modelName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Bible Bridge Chat is thinking...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
