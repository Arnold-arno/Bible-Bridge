package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.BibleVerse
import java.io.File
import java.io.FileOutputStream

data class ShareBgOption(
    val name: String,
    val iconRes: Int?,
    val isImage: Boolean,
    val gradientColors: List<Color> = emptyList(),
    val bgColor: Color = Color.Transparent
)

val shareBgOptions = listOf(
    ShareBgOption("Bible Companion", R.drawable.john_14_26_helper_icon_1785381323913, true),
    ShareBgOption("Sunset Mountain", R.drawable.img_bg_mountain_sunset, true),
    ShareBgOption("Starry Cosmos", R.drawable.img_bg_starry_night, true),
    ShareBgOption("Golden Dawn", null, false, listOf(Color(0xFF2C1654), Color(0xFF8A2387), Color(0xFFE94057), Color(0xFFF27121))),
    ShareBgOption("Emerald Forest", null, false, listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))),
    ShareBgOption("Velvet Twilight", null, false, listOf(Color(0xFF141E30), Color(0xFF243B55))),
    ShareBgOption("Vintage Parchment", null, false, bgColor = Color(0xFFF5EFEB)),
    ShareBgOption("Obsidian Gold", null, false, bgColor = Color(0xFF121212))
)

@Composable
fun ScriptureShareStudioDialog(
    verse: BibleVerse,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedBgIndex by remember { mutableStateOf(0) }
    var selectedAspect by remember { mutableStateOf("1:1") } // 1:1, 9:16, 4:5
    var selectedCardStyle by remember { mutableStateOf(0) } // 0: Dark Glass, 1: Soft Light, 2: Full Bleed, 3: Golden Frame
    var showTranslation by remember { mutableStateOf(true) }

    val referenceStr = "${verse.bookName} ${verse.chapter}:${verse.verseNumber}"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Scripture Share Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Create social media overlay cards",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_share_studio")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // LIVE WYSIWYG PREVIEW CARD
                    val aspectModifier = when (selectedAspect) {
                        "9:16" -> Modifier.aspectRatio(9f / 16f)
                        "4:5" -> Modifier.aspectRatio(4f / 5f)
                        else -> Modifier.aspectRatio(1f)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .then(aspectModifier),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background Layer
                        val bgOption = shareBgOptions[selectedBgIndex]
                        if (bgOption.isImage && bgOption.iconRes != null) {
                            Image(
                                painter = painterResource(id = bgOption.iconRes),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Subtle overlay for photo readability
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f))
                            )
                        } else if (bgOption.gradientColors.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(bgOption.gradientColors))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(bgOption.bgColor)
                            )
                        }

                        // Border for Vintage Parchment
                        if (selectedBgIndex == 5) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(12.dp))
                            )
                        }

                        // Foreground Container Card
                        val cardBgModifier = when (selectedCardStyle) {
                            0 -> Modifier
                                .background(Color(0xB3181824), RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            1 -> Modifier
                                .background(Color(0xF5F8F9FA), RoundedCornerShape(16.dp))
                                .shadow(2.dp, RoundedCornerShape(16.dp))
                            3 -> Modifier
                                .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            else -> Modifier // Full Bleed
                        }

                        val textColor = when {
                            selectedCardStyle == 1 -> Color(0xFF1A1A1A)
                            selectedBgIndex == 5 && selectedCardStyle == 2 -> Color(0xFF2A2A2A)
                            else -> Color.White
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            modifier = Modifier
                                .fillMaxSize(0.88f)
                                .then(cardBgModifier)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "“",
                                    color = Color(0xFFFFC107),
                                    fontSize = 32.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = verse.text,
                                    color = textColor,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp,
                                    maxLines = if (selectedAspect == "9:16") 10 else 6
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val fullCitation = if (showTranslation) "$referenceStr (${verse.translation})" else referenceStr
                                Text(
                                    text = "— $fullCitation —".uppercase(),
                                    color = Color(0xFFFFC107),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "✨ Holy Scripture App",
                                    color = textColor.copy(alpha = 0.6f),
                                    fontSize = 9.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // CONTROLS 1: BACKGROUND THEME
                    Text(
                        text = "BACKGROUND ART THEME",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        shareBgOptions.forEachIndexed { idx, opt ->
                            val isSelected = selectedBgIndex == idx
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedBgIndex = idx },
                                label = { Text(opt.name, fontSize = 12.sp) },
                                leadingIcon = {
                                    if (opt.isImage) {
                                        Icon(Icons.Default.Landscape, contentDescription = null, modifier = Modifier.size(16.dp))
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (opt.gradientColors.isNotEmpty()) Brush.linearGradient(opt.gradientColors)
                                                    else SolidColor(opt.bgColor)
                                                )
                                        )
                                    }
                                },
                                modifier = Modifier.testTag("bg_chip_$idx")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // CONTROLS 2: ASPECT RATIO FORMAT
                    Text(
                        text = "FORMAT / ASPECT RATIO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val aspects = listOf(
                            Triple("1:1", "1:1 Square (Post)", Icons.Default.CropSquare),
                            Triple("9:16", "9:16 Story / Reels", Icons.Default.Smartphone),
                            Triple("4:5", "4:5 Portrait", Icons.Default.CropPortrait)
                        )
                        aspects.forEach { (code, label, icon) ->
                            FilterChip(
                                selected = selectedAspect == code,
                                onClick = { selectedAspect = code },
                                label = { Text(label, fontSize = 11.sp) },
                                leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                modifier = Modifier.weight(1f).testTag("aspect_$code")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // CONTROLS 3: CONTAINER OVERLAY STYLE
                    Text(
                        text = "CARD OVERLAY STYLE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val cardStyles = listOf("Glass Dark", "Soft Light", "Full Bleed", "Golden Frame")
                        cardStyles.forEachIndexed { idx, styleName ->
                            FilterChip(
                                selected = selectedCardStyle == idx,
                                onClick = { selectedCardStyle = idx },
                                label = { Text(styleName, fontSize = 12.sp) },
                                modifier = Modifier.testTag("card_style_$idx")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // CONTROLS 4: OPTIONS TOGGLES
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Include Translation Label (${verse.translation})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = showTranslation,
                            onCheckedChange = { showTranslation = it },
                            modifier = Modifier.scale(0.85f).testTag("toggle_translation_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ACTION BUTTONS
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                shareScriptureCard(
                                    context = context,
                                    verse = verse,
                                    bgIndex = selectedBgIndex,
                                    aspect = selectedAspect,
                                    cardIndex = selectedCardStyle,
                                    showTrans = showTranslation
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("share_to_social_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Image Card", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                shareScriptureText(
                                    context = context,
                                    verse = verse,
                                    showTrans = showTranslation
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("share_text_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Text", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                saveScriptureCardToDevice(
                                    context = context,
                                    verse = verse,
                                    bgIndex = selectedBgIndex,
                                    aspect = selectedAspect,
                                    cardIndex = selectedCardStyle,
                                    showTrans = showTranslation
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("save_image_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Image", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val textToCopy = "“${verse.text}”\n— $referenceStr (${verse.translation}) —\n\nShared via Holy Scripture App"
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Scripture Quote", textToCopy))
                                Toast.makeText(context, "Scripture text copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("copy_text_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Text", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun SolidColor(color: Color): androidx.compose.ui.graphics.Brush {
    return Brush.horizontalGradient(listOf(color, color))
}

fun createScriptureCardBitmap(
    context: Context,
    verseText: String,
    reference: String,
    translation: String,
    bgStyleIndex: Int,
    aspectRatio: String,
    cardStyleIndex: Int,
    showTranslation: Boolean,
    watermarkText: String
): Bitmap {
    val (targetWidth, targetHeight) = when (aspectRatio) {
        "9:16" -> Pair(1080, 1920)
        "4:5" -> Pair(1080, 1350)
        else -> Pair(1080, 1080)
    }

    val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)

    // 1. Draw Background
    when (bgStyleIndex) {
        0 -> { // Sunset Mountain
            val bgDrawable = ContextCompat.getDrawable(context, R.drawable.img_bg_mountain_sunset)
            bgDrawable?.let {
                val bitmapBg = (it as? BitmapDrawable)?.bitmap
                if (bitmapBg != null) {
                    canvas.drawBitmap(bitmapBg, null, Rect(0, 0, targetWidth, targetHeight), null)
                } else {
                    it.setBounds(0, 0, targetWidth, targetHeight)
                    it.draw(canvas)
                }
            }
        }
        1 -> { // Starry Cosmos
            val bgDrawable = ContextCompat.getDrawable(context, R.drawable.img_bg_starry_night)
            bgDrawable?.let {
                val bitmapBg = (it as? BitmapDrawable)?.bitmap
                if (bitmapBg != null) {
                    canvas.drawBitmap(bitmapBg, null, Rect(0, 0, targetWidth, targetHeight), null)
                } else {
                    it.setBounds(0, 0, targetWidth, targetHeight)
                    it.draw(canvas)
                }
            }
        }
        2 -> { // Golden Dawn
            val paint = Paint()
            val gradient = LinearGradient(
                0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(),
                intArrayOf(0xFF2C1654.toInt(), 0xFF8A2387.toInt(), 0xFFE94057.toInt(), 0xFFF27121.toInt()),
                null, Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), paint)
        }
        3 -> { // Emerald Forest
            val paint = Paint()
            val gradient = LinearGradient(
                0f, 0f, 0f, targetHeight.toFloat(),
                intArrayOf(0xFF0F2027.toInt(), 0xFF203A43.toInt(), 0xFF2C5364.toInt()),
                null, Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), paint)
        }
        4 -> { // Velvet Twilight
            val paint = Paint()
            val gradient = LinearGradient(
                0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(),
                intArrayOf(0xFF141E30.toInt(), 0xFF243B55.toInt()),
                null, Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), paint)
        }
        5 -> { // Vintage Parchment
            val paint = Paint().apply { color = 0xFFF5EFEB.toInt() }
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), paint)
            val borderPaint = Paint().apply {
                color = 0xFFD4AF37.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 14f
            }
            canvas.drawRect(36f, 36f, targetWidth - 36f, targetHeight - 36f, borderPaint)
        }
        else -> { // Obsidian Gold
            val paint = Paint().apply { color = 0xFF121212.toInt() }
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), paint)
        }
    }

    // Semi-dark overlay for photo backgrounds
    if (bgStyleIndex in listOf(0, 1)) {
        val darkOverlay = Paint().apply { color = 0x55000000 }
        canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), darkOverlay)
    }

    // 2. Draw Card Container Overlay
    val cardMarginX = targetWidth * 0.08f
    val cardMarginY = targetHeight * 0.12f
    val cardRect = RectF(cardMarginX, cardMarginY, targetWidth - cardMarginX, targetHeight - cardMarginY)

    if (cardStyleIndex == 0) { // Dark Glass
        val cardPaint = Paint().apply {
            color = 0xB3181824.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, 44f, 44f, cardPaint)
        val borderPaint = Paint().apply {
            color = 0x44FFFFFF
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(cardRect, 44f, 44f, borderPaint)
    } else if (cardStyleIndex == 1) { // Light Card
        val cardPaint = Paint().apply {
            color = 0xF5F8F9FA.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, 44f, 44f, cardPaint)
    } else if (cardStyleIndex == 3) { // Golden Frame
        val borderPaint = Paint().apply {
            color = 0xFFD4AF37.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        canvas.drawRoundRect(cardRect, 36f, 36f, borderPaint)
    }

    val textColor = when {
        cardStyleIndex == 1 -> 0xFF1A1A1A.toInt()
        bgStyleIndex == 5 && cardStyleIndex == 2 -> 0xFF2A2A2A.toInt()
        else -> 0xFFFFFFFF.toInt()
    }

    // 3. Opening Quote Symbol
    val quotePaint = Paint().apply {
        color = 0xFFFFC107.toInt()
        textSize = targetWidth * 0.12f
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("“", targetWidth / 2f, cardRect.top + targetHeight * 0.09f, quotePaint)

    // 4. Verse Body Text
    val paddingX = cardMarginX + targetWidth * 0.06f
    val maxTextWidth = (targetWidth - paddingX * 2).toInt()

    val bodyPixelSize = targetWidth * 0.044f
    val textPaint = TextPaint().apply {
        color = textColor
        textSize = bodyPixelSize
        typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        isAntiAlias = true
        if (cardStyleIndex == 2 && bgStyleIndex != 5) {
            setShadowLayer(10f, 2f, 4f, 0xFF000000.toInt())
        }
    }

    val cleanVerseText = verseText.trim()
    val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder.obtain(cleanVerseText, 0, cleanVerseText.length, textPaint, maxTextWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.25f)
            .build()
    } else {
        @Suppress("DEPRECATION")
        StaticLayout(cleanVerseText, textPaint, maxTextWidth, Layout.Alignment.ALIGN_CENTER, 1.25f, 0f, false)
    }

    val textStartY = cardRect.top + targetHeight * 0.13f
    canvas.save()
    canvas.translate(paddingX, textStartY)
    staticLayout.draw(canvas)
    canvas.restore()

    // 5. Reference Citation
    val citationY = textStartY + staticLayout.height + targetHeight * 0.06f
    val fullRefText = if (showTranslation && translation.isNotBlank()) {
        "— $reference ($translation) —"
    } else {
        "— $reference —"
    }

    val refPaint = TextPaint().apply {
        color = 0xFFFFC107.toInt()
        textSize = targetWidth * 0.038f
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        if (cardStyleIndex == 2 && bgStyleIndex != 5) {
            setShadowLayer(8f, 2f, 3f, 0xFF000000.toInt())
        }
    }
    canvas.drawText(fullRefText.uppercase(), targetWidth / 2f, citationY, refPaint)

    // 6. Watermark Footer
    if (watermarkText.isNotBlank()) {
        val wmPaint = TextPaint().apply {
            color = textColor
            alpha = 160
            textSize = targetWidth * 0.026f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("✨ $watermarkText", targetWidth / 2f, cardRect.bottom - targetHeight * 0.035f, wmPaint)
    }

    return bitmap
}

fun shareScriptureCard(
    context: Context,
    verse: BibleVerse,
    bgIndex: Int,
    aspect: String,
    cardIndex: Int,
    showTrans: Boolean
) {
    val reference = "${verse.bookName} ${verse.chapter}:${verse.verseNumber}"
    val bitmap = createScriptureCardBitmap(
        context = context,
        verseText = verse.text,
        reference = reference,
        translation = verse.translation,
        bgStyleIndex = bgIndex,
        aspectRatio = aspect,
        cardStyleIndex = cardIndex,
        showTranslation = showTrans,
        watermarkText = "Holy Scripture App"
    )

    try {
        val cachePath = File(context.cacheDir, "shared_images")
        cachePath.mkdirs()
        val file = File(cachePath, "scripture_card_${System.currentTimeMillis()}.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri: Uri? = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        if (contentUri != null) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Scripture Verse Card: $reference")
                putExtra(Intent.EXTRA_TEXT, "\"${verse.text}\"\n— $reference (${verse.translation})\n\nShared via Holy Scripture App")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Scripture Card to Social Media"))
            Toast.makeText(context, "Opening share options...", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to share image card: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun saveScriptureCardToDevice(
    context: Context,
    verse: BibleVerse,
    bgIndex: Int,
    aspect: String,
    cardIndex: Int,
    showTrans: Boolean
) {
    val reference = "${verse.bookName} ${verse.chapter}:${verse.verseNumber}"
    val bitmap = createScriptureCardBitmap(
        context = context,
        verseText = verse.text,
        reference = reference,
        translation = verse.translation,
        bgStyleIndex = bgIndex,
        aspectRatio = aspect,
        cardStyleIndex = cardIndex,
        showTranslation = showTrans,
        watermarkText = "Holy Scripture App"
    )

    try {
        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.cacheDir
        picturesDir.mkdirs()
        val file = File(picturesDir, "ScriptureCard_${verse.bookName.replace(" ", "_")}_${verse.chapter}_${verse.verseNumber}.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        Toast.makeText(context, "Saved image card to Pictures folder!", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to save image card: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

fun shareScriptureText(
    context: Context,
    verse: BibleVerse,
    showTrans: Boolean
) {
    val referenceStr = "${verse.bookName} ${verse.chapter}:${verse.verseNumber}"
    val fullRef = if (showTrans) "$referenceStr (${verse.translation})" else referenceStr
    val formattedText = "“${verse.text}”\n— $fullRef —\n\nShared via Holy Scripture App"
    try {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Scripture Verse: $referenceStr")
            putExtra(Intent.EXTRA_TEXT, formattedText)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Scripture Verse"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to share text: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
