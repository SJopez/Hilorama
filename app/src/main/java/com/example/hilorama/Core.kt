package com.example.hilorama

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.collection.mutableIntListOf
import androidx.compose.animation.Animatable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger


@Composable
fun Core() {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current
    var bitmap by remember {
        mutableStateOf(BitmapFactory.decodeResource(context.resources, R.drawable.perl))
    }
    var toDrawBitmap by remember { mutableStateOf(bitmapToGray(bitmap)) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var nails by remember { mutableStateOf(floatArrayOf()) }
    val nailsToDraw = remember { mutableStateListOf<Thread>() }
    var threadCount by remember { mutableStateOf(6000) }
    var realThreadCount by remember { mutableStateOf(6000) }
    var nailCount by remember { mutableStateOf(360) }
    var isPlaying by remember { mutableStateOf(false) }
    var lastPlay by remember { mutableStateOf(0L) }
    var grayImage by remember { mutableStateOf(floatArrayOf()) }
    var croppedBitmap by remember { mutableStateOf(createBitmap(1, 1)) }
    var lastReset by rememberSaveable { mutableStateOf(0L) }
    var needToCut by rememberSaveable { mutableStateOf(true) }
    val layer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    var colorMode by rememberSaveable { mutableStateOf(2) }
    var channels by remember { mutableStateOf(imageToCMY(bitmap, threadCount)) }
    val generation = remember { AtomicInteger(0) }
    var isGenerating by remember { mutableStateOf(false) }

    var isGeneratingVideo by remember { mutableStateOf(false) }
    var progressVideo by remember { mutableStateOf(0f) }

    var showAiDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    var fadding by rememberSaveable { mutableStateOf(false) }
    var fadePosition by remember { mutableStateOf(Animatable(-1f)) }

    val controls = remember { mutableListOf(
        ControlStatus(360, 3000),
        ControlStatus(360, 3000),
        ControlStatus(360, 6000),
        ControlStatus(360, 6000)
    ) }

    var accumulatedBitmap by remember { mutableStateOf(createBitmap(1, 1)) }
    var redrawTrigger by remember { mutableStateOf(0) }

    val channelPaints = remember(colorMode) {
        when (colorMode) {
            1 -> listOf(
                Paint().apply { color = AndroidColor.argb(51, 255, 255, 255); strokeWidth = 1f }
            )
            2 -> listOf(
                Paint().apply { color = AndroidColor.rgb(204, 204, 204); strokeWidth = 1f; xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY) },
                Paint().apply { color = AndroidColor.rgb(194, 255, 255); strokeWidth = 1f; xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY) },
                Paint().apply { color = AndroidColor.rgb(255, 199, 255); strokeWidth = 1f; xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY) },
                Paint().apply { color = AndroidColor.rgb(255, 255, 184); strokeWidth = 1f; xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY) }
            )
            3 -> listOf(
                Paint().apply { color = AndroidColor.argb(26, 255, 255, 255); strokeWidth = 1f; xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN) },
                Paint().apply { color = AndroidColor.argb(56, 255, 0, 0); strokeWidth = 1f; xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN) },
                Paint().apply { color = AndroidColor.argb(51, 0, 255, 0); strokeWidth = 1f; xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN) },
                Paint().apply { color = AndroidColor.argb(64, 0, 0, 255); strokeWidth = 1f; xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN) }
            )
            else -> listOf(
                Paint().apply { color = AndroidColor.argb(51, 0, 0, 0); strokeWidth = 1f }
            )
        }
    }

    val backgroundColor = if (colorMode % 2 == 1) AndroidColor.BLACK else AndroidColor.WHITE
    val composeBackgroundColor = if (colorMode % 2 == 1) Color.Black else Color.White
    var startFromReset by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        bitmap = uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }
        if (bitmap != null) needToCut = true
        else bitmap = croppedBitmap
    }

    LaunchedEffect(lastReset) {
        if (canvasSize == IntSize.Zero || lastReset == 0L) return@LaunchedEffect

        withContext(Dispatchers.Default) {
            val width = canvasSize.width
            val height = canvasSize.height

            croppedBitmap = cropBitmap(bitmap, width, height)

            when(colorMode){
                0 -> { grayImage = imageToGray(croppedBitmap) }
                1 -> { grayImage = imageToGray(croppedBitmap, true) }
                2 -> { channels = imageToCMY(croppedBitmap, threadCount) }
                3 -> { channels = imageToRGB(croppedBitmap, threadCount) }
            }

            realThreadCount = if (colorMode > 1) {
                channels.channel0.threads + channels.channel1.threads + channels.channel2.threads + channels.channel3.threads
            } else {
                threadCount
            }

            val freshBitmap = createBitmap(width, height)
            AndroidCanvas(freshBitmap).drawColor(backgroundColor)
            accumulatedBitmap = freshBitmap
            redrawTrigger++

            nails = getNails(width / 2 - 20f, 20, nailCount)
            if (startFromReset) lastPlay = System.currentTimeMillis()
        }
    }

    LaunchedEffect(lastPlay) {
        if (canvasSize == IntSize.Zero || lastPlay == 0L || isPlaying) return@LaunchedEffect

        isPlaying = true

        val curr = generation.incrementAndGet()

        fun paintThread(nail1: Int, nail2: Int, color: Int) {
            if (curr != generation.get() || ((2 * maxOf(nail1, nail2) + 1) >= nails.size)) return

            val x0 = nails[2 * nail1]
            val y0 = nails[2 * nail1 + 1]
            val x1 = nails[2 * nail2]
            val y1 = nails[2 * nail2 + 1]

            synchronized(accumulatedBitmap) {
                AndroidCanvas(accumulatedBitmap).drawLine(x0, y0, x1, y1, channelPaints[color])
            }

            nailsToDraw.add(Thread(nail1, nail2, color))
            if (nailsToDraw.size % 8 == 0) {
                redrawTrigger++
            }
        }

        HiloramaEngine.changeStatus(false)

        withContext(Dispatchers.Default) {
            if (colorMode > 1) {
                if (evalChannel(nailsToDraw.size, 0, channels)){
                    HiloramaEngine.drawImage(0, channels.channel0.threads, channels.channel0.channel, nails, croppedBitmap.width,
                        object : ThreadAdding {
                            override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 0)
                        })
                }
                if (evalChannel(nailsToDraw.size, 1, channels)){
                    HiloramaEngine.drawImage(1, channels.channel1.threads, channels.channel1.channel, nails, croppedBitmap.width,
                        object : ThreadAdding {
                            override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 1)
                        })
                }
                if (evalChannel(nailsToDraw.size, 2, channels)){
                    HiloramaEngine.drawImage(2, channels.channel2.threads, channels.channel2.channel, nails, croppedBitmap.width,
                        object : ThreadAdding {
                            override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 2)
                        })
                }
                if (evalChannel(nailsToDraw.size, 3, channels)){
                    HiloramaEngine.drawImage(3, channels.channel3.threads, channels.channel3.channel, nails, croppedBitmap.width,
                        object : ThreadAdding {
                            override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 3)
                        })
                }
            } else {
                HiloramaEngine.drawImage(0, threadCount, grayImage, nails, croppedBitmap.width,
                    object : ThreadAdding {
                        override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 0)
                    })
            }
            redrawTrigger++
        }
    }

    LaunchedEffect(nailsToDraw.size, threadCount) {
        if (nailsToDraw.size + 8 >= realThreadCount) {
            isPlaying = false
        }
    }

    fun pauseCall() {
        HiloramaEngine.changeStatus(true)
        isPlaying = false
    }

    fun playCall() {
        HiloramaEngine.changeStatus(false)
        lastPlay = System.currentTimeMillis()
    }

    fun replay(start: Boolean = true) {
        if (isPlaying) pauseCall()
        HiloramaEngine.reset()
        nailsToDraw.clear()
        startFromReset = start
        lastReset = System.currentTimeMillis()
    }
    fun assignBitmap(bitmapToAssign: Bitmap, replay: Boolean = true){
        bitmap = bitmapToAssign
        toDrawBitmap = if (colorMode <= 1) bitmapToGray(bitmap) else bitmap
        toDrawBitmap = cropBitmap(toDrawBitmap, canvasSize.width, canvasSize.height)
        needToCut = false
        if (replay) replay()
    }

    val progress = nailsToDraw.size.toFloat() / realThreadCount

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SoftBackground
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (needToCut) {
                bitmap?.let {
                    ImageCutter(
                        bitmap,
                        (screenWidth - 20).dp,
                        assignBitmap = {
                            assignBitmap(it)
                        },
                        onCancel = {
                            pauseCall()
                            needToCut = false
                            bitmap = croppedBitmap
                        }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .displayCutoutPadding()
                    .navigationBarsPadding()
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .then(
                                if (fadding) {
                                    Modifier.background(SoftPrimary, CircleShape)
                                } else {
                                    Modifier.border(2.dp, SoftPrimary, CircleShape)
                                }
                            )
                            .clickable {
                                fadding = !fadding

                                scope.launch {
                                    fadePosition.animateTo(
                                        targetValue = if (fadding) canvasSize.width / 2f else canvasSize.width.toFloat(),
                                        animationSpec = tween(durationMillis = 400)
                                    )
                                }
                            }
                            .padding(10.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.fade),
                            contentDescription = "Toggle fade effect",
                            tint = if (fadding) Color.White else SoftPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .shadow(10.dp, RoundedCornerShape(16.dp), spotColor = SoftPrimary.copy(alpha = 0.5f))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(SoftPrimary, Color(0xFF6366F1))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                pauseCall()
                                showAiDialog = true
                            }
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.magic),
                                contentDescription = "Use AI",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Use AI",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                val canvasContainerSize = (screenWidth - 32).dp

                Box(
                    modifier = Modifier
                        .size(canvasContainerSize)
                        .shadow(16.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.12f))
                        .clip(CircleShape)
                        .background(SoftSurface)
                        .border(1.5.dp, SoftBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (nailsToDraw.size + 8 < realThreadCount && progress != 0f) {
                        CircularProgressIndicator(
                            progress = { progress },
                            color = SoftPrimary,
                            trackColor = Color(0xFFE2E8F0),
                            strokeWidth = 3.dp,
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(1f)
                        )
                    }
                    AiGenerationPlaceholder(
                        isGenerating,
                        canvasContainerSize,
                        cancel = {
                            isGenerating = false
                        }
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .clipToBounds()
                            .onSizeChanged {
                                size -> canvasSize = size
                                scope.launch { fadePosition.snapTo(size.width - 10f) }
                            }
                            .zIndex(0f)
                            .drawWithContent {
                                layer.record { this@drawWithContent.drawContent() }
                                drawContent()
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, _, _ ->
                                    if (fadePosition.value + pan.x in 10f..size.width.toFloat() - 10f && fadding){
                                        scope.launch { fadePosition.snapTo(fadePosition.value + pan.x) }
                                    }
                                }
                            }
                    ) {
                        redrawTrigger

                        drawCircle(
                            center = Offset(center.x, center.y),
                            radius = size.width / 2f - 20f,
                            color = composeBackgroundColor
                        )

                        val radiusPx = size.width / 2 - 20f
                        val left = center.x - radiusPx
                        val top = center.y - radiusPx
                        val right = center.x + radiusPx
                        val bottom = center.y + radiusPx

                        val ovalPath = Path().apply { addOval(Rect(left, top, right, bottom)) }

                        val rightHalfRect = Path().apply {
                            addRect(Rect(Offset(fadePosition.value, 0f), Size(size.width, size.height)))
                        }
                        val circleRightHalf = Path().apply {
                            op(rightHalfRect, ovalPath, PathOperation.Intersect)
                        }

                        clipPath(ovalPath) {
                            drawImage(
                                image = accumulatedBitmap.asImageBitmap(),
                                dstSize = IntSize(size.width.toInt(), size.height.toInt())
                            )
                        }

                        clipPath(circleRightHalf) {
                            drawImage(image = toDrawBitmap.asImageBitmap())
                        }

                        var i = 0
                        while (i < nails.size) {
                            val x = nails[i]
                            val y = nails[i + 1]
                            drawCircle(center = Offset(x, y), color = TextPrimary, radius = 1.2f)
                            i += 2
                        }
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = SoftSurface,
                    border = BorderStroke(1.5.dp, SoftBorder),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BarIcon(
                            src = R.drawable.upload,
                            description = "Upload image",
                            color = TextPrimary,
                            onclick = {
                                pauseCall()
                                launcher.launch("image/*")
                            },
                            active = !isGenerating,
                            context = context
                        )
                        BarIcon(
                            src = if (isPlaying) R.drawable.pause else R.drawable.play,
                            description = "Stop or Start",
                            isHighlighted = true,
                            onclick = {
                                if (isPlaying) pauseCall() else if (nailsToDraw.size < threadCount) playCall()
                            },
                            active = !isGenerating,
                            context = context
                        )
                        BarIcon(
                            src = R.drawable.replay,
                            description = "Replay draw",
                            color = TextPrimary,
                            onclick = {
                                println(bitmap.width)
                                replay() },
                            active = !isGenerating,
                            context = context
                        )
                        BarIcon(
                            src = R.drawable.save,
                            description = "Export hilorama",
                            color = TextPrimary,
                            onclick = {
                                showExportDialog = true
                            },
                            active = !isGenerating,
                            context = context
                        )
                        BarIcon(
                            src = R.drawable.share,
                            description = "Share hilorama",
                            color = TextPrimary,
                            onclick = {
                                makeCapture(
                                    scope = scope,
                                    capture = layer,
                                    name = "${System.currentTimeMillis()}",
                                    context = context,
                                    code = { result ->
                                        if (result != 1) {
                                            Toast.makeText(context, "Error sharing the image", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    share = true,
                                    shareFunction = { uri ->
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            type = "image/png"
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share with..."))
                                    }
                                )
                            },
                            active = !isGenerating,
                            context = context
                        )
                    }
                }

                HiloramaControls(
                    nailCount = nailCount,
                    onNailCountChange = { nails ->
                        pauseCall()
                        nailCount = nails
                        controls[colorMode] = ControlStatus(nailCount, threadCount)
                        replay(false)
                    },
                    threadCount = threadCount,
                    onThreadCountChange = { threads ->
                        pauseCall()
                        threadCount = threads
                        controls[colorMode] = ControlStatus(nailCount, threadCount)
                        replay(false)
                    },
                    colorMode = colorMode,
                    onColorModeChange = { channel ->
                        pauseCall()
                        if (channel > 1 && colorMode <= 1) toDrawBitmap = bitmap
                        else if (channel <= 1 && colorMode > 1) toDrawBitmap = bitmapToGray(bitmap)
                        toDrawBitmap = cropBitmap(toDrawBitmap, canvasSize.width, canvasSize.height)
                        colorMode = channel
                        nailCount = controls[colorMode].nailCount
                        threadCount = controls[colorMode].threadCount
                        replay()
                    },
                    active = !isGenerating,
                    context = context
                )
            }

            if (showExportDialog) {
                ExportOptionDialog(
                    onDismissRequest = { showExportDialog = false },
                    onExportImage = {
                        showExportDialog = false
                        makeCapture(
                            scope = scope,
                            capture = layer,
                            name = "hilorama_${System.currentTimeMillis()}",
                            context = context,
                            code = { result ->
                                val message = if (result == 1) "✅ Saved correctly!" else "❌ Error saving the image"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onExportVideo = {
                        pauseCall()
                        showExportDialog = false
                        isGeneratingVideo = true
                        scope.launch {
                            exportStringArtVideo(
                                context,
                                nails,
                                nailsToDraw,
                                channelPaints,
                                backgroundColor,
                                canvasSize.width,
                                canvasSize.height,
                                { index ->
                                    progressVideo = index / nailsToDraw.size.toFloat()
                                },
                                { isGeneratingVideo = false },
                                { isGeneratingVideo }
                            )
                        }
                    }
                )
            }

            if (showAiDialog) {
                AiPromptDialog(
                    onDismissRequest = {
                        showAiDialog = false
                    },
                    loadBitmap = { aiBitmap ->
                        bitmap = aiBitmap
                        isGenerating = false
                        needToCut = true
                    },
                    launchPlaceholder = { launch ->
                        isGenerating = launch
                    },
                    context,
                    scope,
                    cancel = { !isGenerating }
                )
            }

            if (isGeneratingVideo){
                ExportProgressDialog(
                    progressVideo,
                    onDismissRequest = {
                        isGeneratingVideo = false
                    }
                )
            }
        }
    }
}
