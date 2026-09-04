package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R

@Composable
fun OnboardingScreen(
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 9

    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val selectedTranslation by viewModel.selectedTranslation.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val readerFontSize by viewModel.readerFontSize.collectAsStateWithLifecycle()
    val reminderEnabled by viewModel.reminderEnabled.collectAsStateWithLifecycle()

    OnboardingBackground(currentPage = currentPage, totalPages = totalPages) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Skip Button (hidden on the last screen)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage < totalPages - 1) {
                    TextButton(
                        onClick = { viewModel.completeOnboarding() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)),
                        modifier = Modifier.testTag("onboarding_skip_top_btn")
                    ) {
                        Text("Skip", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Central Content with screen-specific slide transitions
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (currentPage) {
                    0 -> ScreenWelcome()
                    1 -> ScreenInteractiveTimeline()
                    2 -> ScreenAiScriptureAssistant()
                    3 -> ScreenParallelAndAudio()
                    4 -> ScreenChooseLanguage(viewModel, appLanguage)
                    5 -> ScreenChooseTranslation(viewModel, selectedTranslation)
                    6 -> ScreenDownloadOffline(viewModel, selectedTranslation)
                    7 -> ScreenPersonalize(viewModel, isDarkMode, readerFontSize)
                    8 -> ScreenDailyDevotions(viewModel, reminderEnabled)
                }
            }

            // Bottom Navigation Area
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Tracker
                ProgressTracker(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    onPageSelect = { currentPage = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Controls (Back, Next/Finish, Skip)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    if (currentPage > 0) {
                        TextButton(
                            onClick = { currentPage-- },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier.testTag("onboarding_back_btn")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }

                    // Next/Finish button
                    Button(
                        onClick = {
                            if (currentPage < totalPages - 1) {
                                currentPage++
                            } else {
                                viewModel.completeOnboarding()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("onboarding_next_btn"),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (currentPage == totalPages - 1) "Finish" else "Next",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (currentPage == totalPages - 1) Icons.Default.Check else Icons.Default.ArrowForward,
                            contentDescription = "Next",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Skip bottom button
                    TextButton(
                        onClick = { viewModel.completeOnboarding() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("onboarding_skip_bottom_btn")
                    ) {
                        Text("Skip", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingBackground(
    currentPage: Int,
    totalPages: Int,
    content: @Composable BoxScope.() -> Unit
) {
    val bgColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val progress = currentPage.toFloat() / (totalPages - 1).coerceAtLeast(1).toFloat()
    
    val glowAlpha = 0.05f + (0.15f * progress)
    val glowRadius = 250.dp + (350.dp * progress)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Box(
            modifier = Modifier
                .size(glowRadius)
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}

@Composable
fun ProgressTracker(
    currentPage: Int,
    totalPages: Int,
    onPageSelect: (Int) -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
    val completedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalPages) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (i == currentPage) activeColor else inactiveColor)
                    .clickable { onPageSelect(i) }
                    .testTag("progress_node_$i")
            )
            if (i < totalPages - 1) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(2.dp)
                        .background(if (i < currentPage) completedColor else inactiveColor)
                )
            }
        }
    }
}

// ==================== SCREEN 0 — WELCOME ====================
@Composable
fun ScreenWelcome() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        WelcomeVisual()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome to",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontWeight = FontWeight.Light,
            fontFamily = FontFamily.Serif
        )
        Text(
            text = "Bible Bridge",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Connecting Every Verse to Its Story",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Our Goal 🎯",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "To bridge scripture with its rich historical timelines, maps, cultural backgrounds, and dynamic translations, supported by secure personal AI insights, so you can study, connect, and grow globally.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun WelcomeVisual() {
    val infiniteTransition = rememberInfiniteTransition(label = "WelcomeAnimation")
    val primaryColor = MaterialTheme.colorScheme.primary
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloScale"
    )

    Box(
        modifier = Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .scale(haloScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        Card(
            shape = CircleShape,
            modifier = Modifier
                .size(100.dp)
                .border(2.5.dp, primaryColor, CircleShape),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_app_logo_1783953370391),
                contentDescription = "App Icon",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

// ==================== SCREEN 1 — INTERACTIVE TIMELINE & MAPS ====================
@Composable
fun ScreenInteractiveTimeline() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Biblical Picture Banner
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFFEF08A).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_bible_timeline_map_1785042272385),
                contentDescription = "Biblical Timeline Map",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = "FEATURE 1: TIMELINE & MAPS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Interactive Bible Timeline & Maps",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Explore biblical history chronologically. View historical locations, archeological contexts, and geographical maps linked directly to scripture.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Feature Highlight Chips
        Column(
            modifier = Modifier.fillMaxWidth(0.88f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OnboardingFeatureCard(
                icon = Icons.Default.Timeline,
                title = "Chronological Events",
                description = "Walk from Creation to the Apostolic Church through 6 historical eras."
            )
            OnboardingFeatureCard(
                icon = Icons.Default.Map,
                title = "Geographical Maps",
                description = "Pinpoint ancient Jerusalem, Exodus routes, and Paul's journeys."
            )
            OnboardingFeatureCard(
                icon = Icons.Default.AccountTree,
                title = "Cross-Ref Connections",
                description = "See how Old Testament prophecies connect to New Testament fulfillments."
            )
        }
    }
}

// ==================== SCREEN 2 — AI SCRIPTURE ASSISTANT ====================
@Composable
fun ScreenAiScriptureAssistant() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Biblical Picture Banner
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFFEF08A).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_ai_scripture_study_1785042287140),
                contentDescription = "AI Scripture Study",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = "FEATURE 2: AI SCRIPTURE STUDY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AI-Powered Scripture Assistant",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ask complex theological questions, generate personalized devotionals, and unpack deep verse explanations powered by Bible Bridge Chat.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.88f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OnboardingFeatureCard(
                icon = Icons.Default.AutoAwesome,
                title = "Bible Bridge Chat & Answers",
                description = "Get clear, scripture-backed answers to your tough Bible questions."
            )
            OnboardingFeatureCard(
                icon = Icons.Default.Psychology,
                title = "Verse-by-Verse Breakdown",
                description = "Examine original Hebrew & Greek roots, historical context, and application."
            )
            OnboardingFeatureCard(
                icon = Icons.Default.LocalFireDepartment,
                title = "Custom Devotional Engine",
                description = "Type any topic (e.g. 'Peace during anxiety') and receive a targeted devotional."
            )
        }
    }
}

// ==================== SCREEN 3 — PARALLEL READING & AUDIO ====================
@Composable
fun ScreenParallelAndAudio() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Biblical Picture Banner
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFFEF08A).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_ancient_scrolls_1785042299836),
                contentDescription = "Ancient Bible Scrolls",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = "FEATURE 3: PARALLEL READ & AUDIO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Parallel Reading & Audio Bible",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Compare translations side-by-side, read deep book overviews with themes & key figures, and listen hands-free with high quality TTS Audio.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.88f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OnboardingFeatureCard(
                icon = Icons.Default.CompareArrows,
                title = "Side-by-Side Parallel View",
                description = "Compare GNT, NIV, ESV, or KJV verses in parallel split columns."
            )
            OnboardingFeatureCard(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Book Overviews & Themes",
                description = "Detailed overviews with key themes, author dates, characters & major events."
            )
            OnboardingFeatureCard(
                icon = Icons.Default.VolumeUp,
                title = "Hands-Free Audio Bible",
                description = "Listen to scriptures and book overviews with customizable speech rates."
            )
        }
    }
}

@Composable
fun OnboardingFeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ==================== SCREEN 4 — APP LANGUAGE ====================
@Composable
fun ScreenChooseLanguage(
    viewModel: BibleViewModel,
    selectedLang: String
) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf("English", "Spanish", "French", "Swahili", "Luganda", "Chinese", "Hindi", "Arabic", "Portuguese", "Russian", "Bengali")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
    ) {
        // Biblical Picture Banner
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFFEF08A).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_ancient_scrolls_1785042299836),
                contentDescription = "Global Scriptures",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌍", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Global Language Support", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "What language should\nBible Bridge use?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentSize(Alignment.TopStart)
        ) {
            Card(
                onClick = { expanded = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("language_selector_dropdown")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedLang,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                languages.forEach { lang ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = lang,
                                color = if (selectedLang == lang) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selectedLang == lang) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            viewModel.setAppLanguage(lang)
                            expanded = false
                        },
                        modifier = Modifier.testTag("language_item_$lang")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This changes the application interface.\nYour Bible translation can be configured separately.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// ==================== SCREEN 5 — BIBLE TRANSLATION ====================
private data class OnboardingTranslation(
    val code: String,
    val name: String,
    val language: String,
    val philosophy: String,
    val description: String,
    val isRecommended: Boolean = false
)

private val OnboardingTranslationsList = listOf(
    OnboardingTranslation("GNT", "Good News Version (GNT)", "English", "Clear & Modern", "Clear, easy to understand modern Good News Translation.", true),
    OnboardingTranslation("NIV", "New International Version", "English", "Balanced Blend", "Clear, beautiful, contemporary reading experience.", true),
    OnboardingTranslation("ESV", "English Standard Version", "English", "Highly Literal", "Extremely accurate word-for-word modern literary text.", true),
    OnboardingTranslation("NKJV", "New King James Version", "English", "Literal / Majestic", "Modern update to the majestic King James tradition.", true),
    OnboardingTranslation("NLT", "New Living Translation", "English", "Thought-for-Thought", "Warm, highly accessible and modern phrasing.", true),
    OnboardingTranslation("KJV", "King James Version", "English", "Traditional Classic", "The historic 1611 majestic translation.", false),
    OnboardingTranslation("RVR1960", "Reina-Valera 1960", "Español", "Clásica Tradicional", "La traducción clásica en español más amada por siglos.", false)
)

@Composable
fun ScreenChooseTranslation(
    viewModel: BibleViewModel,
    selectedTrans: String
) {
    val downloadedVersions by viewModel.downloadedVersions.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()

    val recommended = OnboardingTranslationsList.filter { it.isRecommended }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Biblical Picture Banner
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFFEF08A).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_bg_mountain_sunset),
                contentDescription = "Bible Translations Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
            )
            Text(
                text = "📜 Multiple Dynamic Translations",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Choose Your Bible Translation",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Pick the version that matches your study style. You can add or swap anytime.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Recommended Cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            recommended.forEach { item ->
                val isSelected = selectedTrans == item.code
                val isDownloaded = downloadedVersions.contains(item.code)
                val progress = downloadProgress[item.code]

                OutlinedCard(
                    onClick = { viewModel.selectTranslation(item.code) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("translation_recommended_${item.code}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = item.code,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = item.philosophy,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                if (isDownloaded) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Offline ready",
                                        tint = Color(0xFF4ADE80),
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else if (progress != null) {
                                    CircularProgressIndicator(
                                        progress = { progress / 100f },
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.selectTranslation(item.code) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

// ==================== SCREEN 6 — OFFLINE READING DOWNLOAD ====================
@Composable
fun ScreenDownloadOffline(
    viewModel: BibleViewModel,
    selectedTrans: String
) {
    val context = LocalContext.current
    val downloadedVersions by viewModel.downloadedVersions.collectAsStateWithLifecycle()
    val downloadProgressMap by viewModel.downloadProgress.collectAsStateWithLifecycle()
    
    val isDownloaded = downloadedVersions.contains(selectedTrans)
    val downloadProgress = downloadProgressMap[selectedTrans]

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Cloud illustration picture
        Image(
            painter = painterResource(id = R.drawable.img_onboarding_download_1783916239810),
            contentDescription = "Cloud Download Illustration",
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFFEF08A).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Download $selectedTrans for\nOffline Reading?",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Offline Scripture Reading", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Instant Fast Search", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("No Wi-Fi / Data Required", fontSize = 13.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isDownloaded) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ready! Downloaded Successfully.", color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
            }
        } else if (downloadProgress != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                LinearProgressIndicator(
                    progress = { downloadProgress.toFloat() / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Downloading... $downloadProgress%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                Button(
                    onClick = {
                        viewModel.downloadVersion(selectedTrans)
                        Toast.makeText(context, "Download started for $selectedTrans!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("onboarding_download_btn")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        Toast.makeText(context, "Skipped. You can download anytime in settings.", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("onboarding_skip_download_btn")
                ) {
                    Text("Skip for now")
                }
            }
        }
    }
}

// ==================== SCREEN 7 — PERSONALIZE READING ====================
@Composable
fun ScreenPersonalize(
    viewModel: BibleViewModel,
    isDarkMode: Boolean,
    fontSize: Float
) {
    var sliderVal by remember(fontSize) { mutableFloatStateOf(fontSize) }
    var selectedMode by remember(fontSize) {
        mutableStateOf(
            when {
                fontSize <= 15f -> "Compact"
                fontSize >= 23f -> "Large Print"
                else -> "Comfortable"
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Personalize Your Reading",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Large Preview Box
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(140.dp)
                .testTag("onboarding_preview_card"),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFCF6E8)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "“The Lord is my shepherd; I shall not want. He maketh me to lie down in green pastures.”\n— Psalms 23:1-2",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = sliderVal.sp,
                        fontFamily = FontFamily.Serif,
                        lineHeight = (sliderVal * 1.35f).sp
                    ),
                    color = if (isDarkMode) Color.White else Color(0xFF2C2C2C),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Font size slider
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text("A", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("A", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("A", fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("A", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = sliderVal,
                onValueChange = { 
                    sliderVal = it
                    viewModel.setReaderFontSize(it)
                },
                valueRange = 13f..27f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("onboarding_font_slider")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Reading Mode Chips
        Text(
            text = "Reading Mode",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).padding(horizontal = 28.dp, vertical = 2.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Compact", "Comfortable", "Large Print").forEach { mode ->
                val isSelected = selectedMode == mode
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedMode = mode
                        val newSize = when (mode) {
                            "Compact" -> 14f
                            "Comfortable" -> 18f
                            else -> 24f
                        }
                        sliderVal = newSize
                        viewModel.setReaderFontSize(newSize)
                    },
                    label = { Text(mode, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("onboarding_mode_chip_$mode")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dark Mode
        Text(
            text = "App Theme",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).padding(horizontal = 28.dp, vertical = 2.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WbSunny, contentDescription = "Light Mode", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Light", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }

            Switch(
                checked = isDarkMode,
                onCheckedChange = { checked ->
                    viewModel.setThemePreference(if (checked) "Dark" else "Light")
                },
                modifier = Modifier.testTag("onboarding_theme_switch")
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Dark", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.NightsStay, contentDescription = "Dark Mode", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ==================== SCREEN 8 — DAILY DEVOTIONS ====================
@Composable
fun ScreenDailyDevotions(
    viewModel: BibleViewModel,
    reminderEnabled: Boolean
) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Devotional sunlight image picture
        Image(
            painter = painterResource(id = R.drawable.img_onboarding_devotional_1783916253407),
            contentDescription = "Morning Sunlight Illustration",
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFFEF08A).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Grow Every Day",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Receive a daily verse and devotional to strengthen your walk with God.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedCard(
                onClick = {
                    viewModel.updateReminderSettings(context, true, 8, 0)
                    Toast.makeText(context, "Daily devotion reminder set for 8:00 AM!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (reminderEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = if (reminderEnabled) 2.dp else 1.dp,
                    color = if (reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_notify_yes")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Yes, keep me inspired daily",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    RadioButton(
                        selected = reminderEnabled,
                        onClick = {
                            viewModel.updateReminderSettings(context, true, 8, 0)
                            Toast.makeText(context, "Daily devotion reminder set for 8:00 AM!", Toast.LENGTH_SHORT).show()
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }

            OutlinedCard(
                onClick = {
                    viewModel.updateReminderSettings(context, false, 8, 0)
                    Toast.makeText(context, "Devotions reminders can be turned on anytime.", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (!reminderEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(
                    width = if (!reminderEnabled) 2.dp else 1.dp,
                    color = if (!reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_notify_later")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Remind me later",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    RadioButton(
                        selected = !reminderEnabled,
                        onClick = {
                            viewModel.updateReminderSettings(context, false, 8, 0)
                            Toast.makeText(context, "Devotions reminders can be turned on anytime.", Toast.LENGTH_SHORT).show()
                        },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}
