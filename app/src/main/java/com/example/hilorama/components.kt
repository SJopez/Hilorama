package com.example.hilorama

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.text.toInt

val Jakarta = FontFamily(
    Font(R.font.jakarta, FontWeight.Normal),
    Font(R.font.jakarta02, FontWeight.SemiBold),
    Font(R.font.jakarta03, FontWeight.Bold)
)
val SoftPrimary = Color(0xFF6054C9)
val SoftBackground = Color(0xFFF8F9FA)
val SoftSurface = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF2D3436)
val TextSecondary = Color(0xFF636E72)
val SoftBorder = Color(0xFFD1D5DB)

@Composable
fun BarIcon(
    src: Int,
    description: String,
    color: Color = TextPrimary,
    onclick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    isHighlighted: Boolean = false,
    active: Boolean,
    context: Context
) {
    val bgColor = if(!active && isHighlighted) Color.LightGray else if (isHighlighted) SoftPrimary else Color(0xFFF1F5F9)
    val iconColor = if (isHighlighted) Color.White else color

    Surface(
        onClick = { if (active) onclick() else Toast.makeText(context, "\uD83D\uDD27 Finish the AI generation image to use!", Toast.LENGTH_SHORT).show() },
        shape = CircleShape,
        color = bgColor,
        modifier = modifier.size(42.dp),
        shadowElevation = if (isHighlighted) 4.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = src),
                contentDescription = description,
                tint = iconColor,
                modifier = Modifier.size(size),
            )
        }
    }
}

@Composable
fun MainButton(onclick: () -> Unit, modifier: Modifier = Modifier, text: String, icon: Boolean = false, iconSrc: Int = 0){
    Button(
        onClick = onclick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SoftPrimary,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 1.dp
        ),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
        modifier = modifier
            .zIndex(2f)
            .padding(bottom = 16.dp),
    ) {
        if (icon){
            Icon(
                painter = painterResource(iconSrc),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontFamily = Jakarta,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ImageCutter(bitmap: Bitmap, sizeDp: Dp, assignBitmap: (Bitmap) -> Unit) {
    val direction = remember { bitmap.width <= bitmap.height }
    var offsetX by remember { mutableStateOf(-1f) }
    var offsetY by remember { mutableStateOf(-1f) }
    var croppedWidth by remember { mutableStateOf(0) }
    var croppedHeight by remember { mutableStateOf(0) }
    var cropped by remember { mutableStateOf(createBitmap(1, 1)) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f),
        color = Color.Black
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, _, _ ->
                        if (direction) {
                            val increment = offsetY - pan.y
                            if (increment in 0f..croppedHeight.toFloat() - croppedWidth) offsetY = increment
                        } else {
                            val increment = offsetX - pan.x
                            if (increment in 0f..croppedWidth.toFloat() - croppedHeight) offsetX = increment
                        }
                    }
                }
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                val overlayHeight = (maxHeight - sizeDp) / 2

                Box(
                    modifier = Modifier
                        .background(color = Color.Black.copy(alpha = 0.5f))
                        .fillMaxWidth()
                        .height(overlayHeight)
                        .align(Alignment.TopStart)
                        .zIndex(2f)
                )
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Box(
                        modifier = Modifier
                            .background(color = Color.Black.copy(alpha = 0.5f))
                            .height(sizeDp)
                            .width(10.dp)
                            .zIndex(2f),
                    )
                    Box(
                        modifier = Modifier
                            .size(sizeDp)
                            .background(color = Color.White)
                            .zIndex(1f)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize(),
                        ) {
                            val width = size.width.toInt()
                            val height = size.height.toInt()
                            cropped = cropBitmap(bitmap, width, height, true)
                            croppedWidth = cropped.width
                            croppedHeight = cropped.height

                            if (offsetX == -1f && offsetY == -1f) {
                                offsetX = (croppedWidth - width) / 2f
                                offsetY = (croppedHeight - height) / 2f
                            }

                            drawImage(
                                image = cropped.asImageBitmap(),
                                dstOffset = IntOffset(-offsetX.toInt(), -offsetY.toInt())
                            )

                            val radiusPx = size.width / 2 - 20f

                            val maskPath = Path().apply {
                                addRect(Rect(Offset.Zero, size))
                                val left = center.x - radiusPx
                                val top = center.y - radiusPx
                                val right = center.x + radiusPx
                                val bottom = center.y + radiusPx

                                addOval(
                                    Rect(
                                        left = left,
                                        top = top,
                                        right = right,
                                        bottom = bottom
                                    )
                                )
                                fillType = PathFillType.EvenOdd
                            }

                            drawPath(
                                path = maskPath,
                                color = Color.Black.copy(alpha = 0.6f)
                            )

                            val thinStrokeW = 1.dp.toPx()
                            val thinLineColor = Color.White.copy(alpha = 0.6f)

                            drawRect(
                                color = thinLineColor,
                                topLeft = Offset.Zero,
                                size = size,
                                style = Stroke(width = thinStrokeW)
                            )

                            drawCircle(
                                color = thinLineColor,
                                radius = radiusPx,
                                center = center,
                                style = Stroke(width = thinStrokeW)
                            )

                            val cornerLength = 40f
                            val strokeW = 4f
                            val halfStroke = strokeW / 2f
                            val lineColor = Color.White

                            val topLPath = Path().apply {
                                moveTo(cornerLength, halfStroke)
                                lineTo(halfStroke, halfStroke)
                                lineTo(halfStroke, cornerLength)
                            }
                            drawPath(path = topLPath, color = lineColor, style = Stroke(width = strokeW, cap = StrokeCap.Square))

                            val topRPath = Path().apply {
                                moveTo(size.width - cornerLength, halfStroke)
                                lineTo(size.width - halfStroke, halfStroke)
                                lineTo(size.width - halfStroke, cornerLength)
                            }
                            drawPath(path = topRPath, color = lineColor, style = Stroke(width = strokeW, cap = StrokeCap.Square))

                            val bottomLPath = Path().apply {
                                moveTo(cornerLength, size.height - halfStroke)
                                lineTo(halfStroke, size.height - halfStroke)
                                lineTo(halfStroke, size.height - cornerLength)
                            }
                            drawPath(path = bottomLPath, color = lineColor, style = Stroke(width = strokeW, cap = StrokeCap.Square))

                            val bottomRPath = Path().apply {
                                moveTo(size.width - cornerLength, size.height - halfStroke)
                                lineTo(size.width - halfStroke, size.height - halfStroke)
                                lineTo(size.width - halfStroke, size.height - cornerLength)
                            }
                            drawPath(path = bottomRPath, color = lineColor, style = Stroke(width = strokeW, cap = StrokeCap.Square))

                            val sideMarkLength = 40f
                            val halfMark = sideMarkLength / 2f

                            drawLine(color = lineColor, start = Offset(center.x - halfMark, halfStroke), end = Offset(center.x + halfMark, halfStroke), strokeWidth = strokeW, cap = StrokeCap.Square)
                            drawLine(color = lineColor, start = Offset(center.x - halfMark, size.height - halfStroke), end = Offset(center.x + halfMark, size.height - halfStroke), strokeWidth = strokeW, cap = StrokeCap.Square)
                            drawLine(color = lineColor, start = Offset(halfStroke, center.y - halfMark), end = Offset(halfStroke, center.y + halfMark), strokeWidth = strokeW, cap = StrokeCap.Square)
                            drawLine(color = lineColor, start = Offset(size.width - halfStroke, center.y - halfMark), end = Offset(size.width - halfStroke, center.y + halfMark), strokeWidth = strokeW, cap = StrokeCap.Square)

                            drawLine(
                                start = Offset(width / 2f - 20f, height / 2f),
                                end = Offset(width / 2f + 20f, height / 2f),
                                strokeWidth = strokeW,
                                color = lineColor,
                                cap = StrokeCap.Square
                            )

                            drawLine(
                                start = Offset(width / 2f, height / 2f - 20f),
                                end = Offset(width / 2f, height / 2f + 20f),
                                strokeWidth = strokeW,
                                color = lineColor,
                                cap = StrokeCap.Square
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(color = Color.Black.copy(alpha = 0.5f))
                            .height(sizeDp)
                            .width(10.dp)
                            .zIndex(2f),
                    )
                }
                Box(
                    modifier = Modifier
                        .background(color = Color.Black.copy(alpha = 0.5f))
                        .fillMaxWidth()
                        .height(overlayHeight)
                        .align(Alignment.BottomStart)
                        .zIndex(2f),
                    contentAlignment = Alignment.Center
                ) {}
                MainButton(
                    onclick = {
                        val side = min(croppedWidth, croppedHeight)
                        val ans = Bitmap.createBitmap(cropped, offsetX.toInt(), offsetY.toInt(), side, side)
                        assignBitmap(ans)
                    },
                    modifier = Modifier.align(Alignment.BottomCenter),
                    text = "Confirm",
                    icon = true,
                    iconSrc = R.drawable.check
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySlider(
    start: Float,
    end: Float,
    value: Float,
    steps: Int = 0,
    action: (Float) -> Unit,
    final: () -> Unit = {},
    active: Boolean = true,
    thumbColor: Color = SoftPrimary,
    activeColor: Color = SoftPrimary,
    inactiveColor: Color = Color(0xFFE2E8F0)
) {
    Slider(
        value = value,
        onValueChange = action,
        onValueChangeFinished = final,
        valueRange = start..end,
        steps = steps,
        enabled = active,
        colors = SliderDefaults.colors(
            activeTrackColor = activeColor,
            inactiveTrackColor = inactiveColor,
            thumbColor = thumbColor,


        ),
        modifier = Modifier.padding(vertical = 0.dp),
        thumb = {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(if(active) thumbColor else Color.Gray, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    )
}

val ColorChannelModes = listOf(
    "Gray",
    "Mono",
    "CMY",
    "RGB"
)

@Composable
fun HiloramaControls(
    nailCount: Int,
    onNailCountChange: (Int) -> Unit,
    threadCount: Int,
    onThreadCountChange: (Int) -> Unit,
    colorMode: Int,
    onColorModeChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    nailCountRange: IntRange = 100..360,
    threadCountRange: IntRange = 1000..12000,
    active: Boolean,
    context: Context
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SoftSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.5.dp, SoftBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nails",
                    fontFamily = Jakarta,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Text(
                    text = "$nailCount",
                    fontFamily = Jakarta,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (active) SoftPrimary else Color.LightGray
                )
            }
            MySlider(
                start = nailCountRange.first.toFloat(),
                end = nailCountRange.last.toFloat(),
                value = nailCount.toFloat(),
                action = { onNailCountChange(it.toInt()) },
                active = active
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Threads",
                    fontFamily = Jakarta,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Text(
                    text = "$threadCount",
                    fontFamily = Jakarta,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (active) SoftPrimary else Color.LightGray
                )
            }
            MySlider(
                start = threadCountRange.first.toFloat(),
                end = threadCountRange.last.toFloat(),
                value = threadCount.toFloat(),
                action = { onThreadCountChange(it.toInt()) },
                active = active
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Coloring mode",
                fontFamily = Jakarta,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ColorChannelModes.forEachIndexed { index, mode ->
                    val isSelected = index == colorMode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SoftSurface else Color.Transparent)
                            .clickable { if (active) onColorModeChange(index) else Toast.makeText(context, "\uD83D\uDD27 Finish the AI generation image to use!", Toast.LENGTH_SHORT).show() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode,
                            fontFamily = Jakarta,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected && active) SoftPrimary else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AiGenerationPlaceholder(
    isGenerating: Boolean,
    size: Dp,
    modifier: Modifier = Modifier,
    cancel: () -> Unit
) {
    if (!isGenerating) return

    val phrases = remember {
        listOf(
            "Synthesizing visual concepts...",
            "Sampling diffusion process...",
            "Rendering pixel details...",
            "Applying neural aesthetic styles...",
            "Translating prompt into pixels...",
            "Refining visual features...",
            "Bringing your image to life..."
        )
    }

    var currentPhraseIndex by remember { mutableIntStateOf(phrases.indices.random()) }

    LaunchedEffect(isGenerating) {
        while (isGenerating) {
            delay(2500L)
            var newIndex: Int
            do {
                newIndex = phrases.indices.random()
            } while (newIndex == currentPhraseIndex && phrases.size > 1)
            currentPhraseIndex = newIndex
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.75f))
            .zIndex(10f),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            CircularProgressIndicator(
                color = SoftPrimary,
                trackColor = Color.White.copy(alpha = 0.2f),
                strokeWidth = 4.dp,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = phrases[currentPhraseIndex],
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "AiPhraseAnimation"
            ) { phrase ->
                Text(
                    text = phrase,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            MainButton(
                onclick = cancel,
                text = "Cancel"
            )
        }
    }
}
@Composable
fun ExportOptionDialog(
    onDismissRequest: () -> Unit,
    onExportImage: () -> Unit,
    onExportVideo: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SoftSurface,
            border = BorderStroke(1.5.dp, SoftBorder),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Save String Art",
                        fontFamily = Jakarta,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Choose your preferred output format",
                        fontFamily = Jakarta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSecondary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SoftBackground)
                            .clickable { onExportImage() }
                            .padding(vertical = 16.dp, horizontal = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.image),
                                contentDescription = "Export Image",
                                tint = SoftPrimary,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Image",
                            fontFamily = Jakarta,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "PNG format",
                            fontFamily = Jakarta,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SoftBackground)
                            .clickable { onExportVideo() }
                            .padding(vertical = 16.dp, horizontal = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.video),
                                contentDescription = "Export Video",
                                tint = SoftPrimary,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Video",
                            fontFamily = Jakarta,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "MP4 format",
                            fontFamily = Jakarta,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExportProgressDialog(
    progress: Float,
    onDismissRequest: () -> Unit
) {
    val phrases = remember {
        listOf(
            "Weaving initial thread paths...",
            "Tracing pin connections...",
            "Calculating vector coordinates...",
            "Rendering time-lapse frames...",
            "Optimizing thread density...",
            "Synthesizing frame sequence...",
            "Finalizing video export..."
        )
    }

    var currentPhraseIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2800L)
            currentPhraseIndex = (currentPhraseIndex + 1) % phrases.size
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SoftSurface,
            border = BorderStroke(1.5.dp, SoftBorder),
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rendering Video",
                        fontFamily = Jakarta,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Surface(
                        shape = CircleShape,
                        color = SoftPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontFamily = Jakarta,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = SoftPrimary,
                    trackColor = SoftBackground,
                    strokeCap = StrokeCap.Round
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SoftBackground
                ) {
                    AnimatedContent (
                        targetState = phrases[currentPhraseIndex],
                        transitionSpec = {
                            slideInVertically { height -> height } + fadeIn() togetherWith
                                    slideOutVertically { height -> -height } + fadeOut() using
                                    SizeTransform(clip = false)
                        },
                        label = "PhraseTransition",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ) { targetPhrase ->
                        Text(
                            text = targetPhrase,
                            fontFamily = Jakarta,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}