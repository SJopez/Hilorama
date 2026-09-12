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
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.text.toInt

val Jakarta = FontFamily(
    Font(R.font.jakarta, FontWeight.Normal),
    Font(R.font.jakarta02, FontWeight.SemiBold),
    Font(R.font.jakarta03, FontWeight.Bold)
)
val SoftPrimary = Color(0xFF6054C9)

val ExtraSoftPrimary = Color(0xFF877FDA)
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
fun MainButton(
    onclick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    icon: Boolean = false,
    iconSrc: Int = 0,
    containerColor: Color = SoftPrimary,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onclick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
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
        if (icon) {
            Icon(
                painter = painterResource(iconSrc),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = contentColor
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
    context: Context,
    borders: Boolean = true
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SoftSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = if(borders) 6.dp else 0.dp),
        border = BorderStroke(if (borders) 1.5.dp else 0.dp, SoftBorder)
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
    title: String = "Rendering Video",
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
                        text = title,
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
                    AnimatedContent(
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

@Composable
fun BottomNavigationRow(
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onNextClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftPrimary
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Next Thread",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = Jakarta
                )
            }
        }
    }
}

@Composable
fun ThreadItem(
    index: Int,
    nail1: Int,
    nail2: Int,
    selected: Boolean,
    color: Color = Color.Red,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit,
    background: Color
) {
    val backgroundColor = if (selected) SoftPrimary.copy(alpha = 0.08f) else Color.Transparent
    val textColor = if (selected) Color.Black else Color.Gray

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onSelect(index) })
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(10.dp),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(SoftPrimary)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "${index + 1}",
            color = if (selected) TextPrimary else TextSecondary,
            fontSize = 14.sp,
            fontFamily = Jakarta,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(42.dp)
        )

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "From ",
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = Jakarta
            )
            Text(
                text = "${nail1}",
                color = textColor,
                fontSize = 14.sp,
                fontFamily = Jakarta,
                fontWeight = FontWeight.SemiBold
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(3.dp)
                    .background(background.copy(alpha = if (selected) 1f else 0.2f)),
                contentAlignment = Alignment.Center
            ){
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(color.copy(alpha = if (selected) 1f else 0.4f))
                )
            }

            Text(
                text = "To ",
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = Jakarta
            )
            Text(
                text = "${nail2}",
                color = textColor,
                fontSize = 14.sp,
                fontFamily = Jakarta,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ThreadList(
    indexToDraw: Int,
    nailsToDraw: MutableList<Thread>,
    current: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState,
    channel: List<Color>,
){
    LazyColumn(
        state = state,
        modifier = modifier.padding(horizontal = 8.dp)
    ){
        if (nailsToDraw.size > indexToDraw){
            for (i in 0..indexToDraw){
                val nail1 = nailsToDraw[i].nail1
                val nail2 = nailsToDraw[i].nail2
                val color = nailsToDraw[i].color

                item {
                    ThreadItem(
                        i,
                        nail1,
                        nail2,
                        i == current,
                        onSelect = onSelect,
                        color = channel[color],
                        background = if (channel[color] == Color.White) Color.Black else Color.Transparent)
                }
            }
        }
    }
}

@Composable
fun NailsDiagram(
    modifier: Modifier = Modifier,
    primaryColor: Color = SoftPrimary,
    textColor: Color = TextPrimary,
    accentColor: Color = TextSecondary
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        val centerPoint = center
        val radius = size.minDimension / 2f - 20.dp.toPx()
        val innerArcRadius = radius - 18.dp.toPx()
        val strokeWidth = 2.dp.toPx()

        drawCircle(
            color = SoftBorder,
            radius = radius,
            center = centerPoint,
            style = Stroke(width = strokeWidth)
        )

        val angles = listOf(
            0f to "0°",
            270f to "90°",
            180f to "180°",
            90f to "270°"
        )

        val textPaint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = textColor.toArgb()
            textSize = 12.dp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
        }

        angles.forEach { (angleDeg, label) ->
            val rad = angleDeg * (Math.PI / 180f)
            val nailX = centerPoint.x + radius * cos(rad).toFloat()
            val nailY = centerPoint.y + radius * sin(rad).toFloat()

            drawCircle(
                color = primaryColor,
                radius = 5.dp.toPx(),
                center = Offset(nailX, nailY)
            )

            val labelRadius = radius + 28.dp.toPx()
            val labelX = centerPoint.x + labelRadius * cos(rad).toFloat()
            val labelY = centerPoint.y + labelRadius * sin(rad).toFloat()

            val textBounds = android.graphics.Rect()
            textPaint.getTextBounds(label, 0, label.length, textBounds)
            val correctedY = labelY - (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2f

            drawContext.canvas.nativeCanvas.drawText(
                label,
                labelX,
                correctedY,
                textPaint
            )
        }

        val startAngle = 30f
        val sweepAngle = -60f
        val endAngleDeg = startAngle + sweepAngle

        drawArc(
            color = accentColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(centerPoint.x - innerArcRadius, centerPoint.y - innerArcRadius),
            size = Size(innerArcRadius * 2, innerArcRadius * 2)
        )

        val endRad = endAngleDeg * (Math.PI / 180f)
        val tipX = centerPoint.x + innerArcRadius * cos(endRad).toFloat()
        val tipY = centerPoint.y + innerArcRadius * sin(endRad).toFloat()

        val tangentAngle = endRad - (Math.PI / 2)
        val arrowLength = 8.dp.toPx()
        val arrowWingAngle = 25f * (Math.PI / 180f)

        val leftWingX = tipX - arrowLength * cos(tangentAngle - arrowWingAngle).toFloat()
        val leftWingY = tipY - arrowLength * sin(tangentAngle - arrowWingAngle).toFloat()

        val rightWingX = tipX - arrowLength * cos(tangentAngle + arrowWingAngle).toFloat()
        val rightWingY = tipY - arrowLength * sin(tangentAngle + arrowWingAngle).toFloat()

        val arrowPath = Path().apply {
            moveTo(leftWingX, leftWingY)
            lineTo(tipX, tipY)
            lineTo(rightWingX, rightWingY)
        }

        drawPath(
            path = arrowPath,
            color = accentColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
@Composable
fun ThreadStepHelpDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = SoftSurface
            ),
            border = BorderStroke(1.dp, SoftBorder),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = SoftPrimary.copy(alpha = 0.12f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.help),
                            contentDescription = null,
                            tint = SoftPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Instructions",
                        fontFamily = Jakarta,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                NailsDiagram()
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InstructionStep(
                        stepNumber = 1,
                        description = "Nails are numbered starting at 0° on the left side and counted counter-clockwise around the frame."
                    )

                    InstructionStep(
                        stepNumber = 2,
                        description = "Press the Next Thread button to advance to the next step and continue weaving."
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.Center
                ){
                    MainButton(
                        onclick = onDismissRequest,
                        modifier = Modifier.fillMaxWidth(0.5f),
                        text = "Got it",
                        containerColor = SoftPrimary,
                        contentColor = Color.White
                    )
                }
            }
        }
    }
}


@Composable
private fun InstructionStep(
    stepNumber: Int,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = SoftPrimary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                fontFamily = Jakarta,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = description,
            fontFamily = Jakarta,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun CircularButton(onclick: () -> Unit, description: String, icon: Int, tint: Color = SoftPrimary, modifier: Modifier = Modifier){
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(2.dp, SoftPrimary, CircleShape)
            .clickable {
                onclick()
            }
            .padding(10.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun ToolBar(row: @Composable () -> Unit){
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = SoftSurface,
        border = BorderStroke(1.5.dp, SoftBorder),
        shadowElevation = 6.dp
    ) {
        row()
    }
}