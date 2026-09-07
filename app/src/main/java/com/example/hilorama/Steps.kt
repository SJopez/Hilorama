package com.example.hilorama

import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Preview
@Composable
fun StepsCore() {
    var fadding by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var fadePosition by remember { mutableStateOf(Animatable(-1f)) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val context = LocalContext.current
    var colorMode by rememberSaveable { mutableStateOf(2) }
    var bitmap by remember {
        mutableStateOf(BitmapFactory.decodeResource(context.resources, R.drawable.perl))
    }
    var accumulatedBitmap by remember { mutableStateOf(createBitmap(1, 1)) }
    var stepBitmap by remember { mutableStateOf(createBitmap(1, 1)) }
    var toDrawBitmap by remember { mutableStateOf(bitmapToGray(bitmap)) }
    var grayImage by remember { mutableStateOf(floatArrayOf()) }

    var nails by remember { mutableStateOf(floatArrayOf()) }
    val nailsToDraw = remember { mutableStateListOf<Thread>() }
    var threadCount by remember { mutableStateOf(1000) }
    var realThreadCount by remember { mutableStateOf(1000) }
    var nailCount by remember { mutableStateOf(360) }

    var loadingThreads by remember { mutableStateOf(false) }
    var threadsProg by remember { mutableStateOf(0f) }

    val screenWidth = LocalConfiguration.current.screenWidthDp

    var currIndex by remember { mutableStateOf(0) }
    var indexToDraw by remember { mutableStateOf(0) }

    val channelPaints = remember(colorMode) {
        when (colorMode) {
            1 -> listOf(
                Paint().apply { color = AndroidColor.argb(51, 255, 255, 255); strokeWidth = 1f }
            )

            2 -> listOf(
                Paint().apply {
                    color = AndroidColor.rgb(204, 204, 204); strokeWidth = 1f; xfermode =
                    PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                },
                Paint().apply {
                    color = AndroidColor.rgb(194, 255, 255); strokeWidth = 1f; xfermode =
                    PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                },
                Paint().apply {
                    color = AndroidColor.rgb(255, 199, 255); strokeWidth = 1f; xfermode =
                    PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                },
                Paint().apply {
                    color = AndroidColor.rgb(255, 255, 184); strokeWidth = 1f; xfermode =
                    PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                }
            )

            3 -> listOf(
                Paint().apply {
                    color = AndroidColor.argb(26, 255, 255, 255); strokeWidth = 1f; xfermode =
                    PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                },
                Paint().apply {
                    color = AndroidColor.argb(56, 255, 0, 0); strokeWidth = 1f; xfermode =
                    PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                },
                Paint().apply {
                    color = AndroidColor.argb(51, 0, 255, 0); strokeWidth = 1f; xfermode =
                    PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                },
                Paint().apply {
                    color = AndroidColor.argb(64, 0, 0, 255); strokeWidth = 1f; xfermode =
                    PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                }
            )

            else -> listOf(
                Paint().apply { color = AndroidColor.argb(51, 0, 0, 0); strokeWidth = 1f }
            )
        }
    }

    var needToCut by rememberSaveable { mutableStateOf(true) }
    var redrawTrigger by remember { mutableStateOf(0) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        bitmap = uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }
        needToCut = true
    }

    var channels by remember { mutableStateOf(imageToCMY(bitmap, threadCount)) }
    var lastAssign by remember { mutableStateOf(0L) }
    var lastCalc by remember { mutableStateOf(0L) }

    val backgroundColor = if (colorMode % 2 == 1) AndroidColor.BLACK else AndroidColor.WHITE
    val composeBackgroundColor = if (colorMode % 2 == 1) Color.Black else Color.White
    val canvasContainerSize = (screenWidth - 32).dp

    LaunchedEffect(lastAssign) {
        if (canvasSize == IntSize.Zero || lastAssign == 0L) return@LaunchedEffect

        withContext(Dispatchers.Default) {
            val width = canvasSize.width
            val height = canvasSize.height

            when(colorMode){
                0 -> { grayImage = imageToGray(bitmap) }
                1 -> { grayImage = imageToGray(bitmap, true) }
                2 -> { channels = imageToCMY(bitmap, threadCount) }
                3 -> { channels = imageToRGB(bitmap, threadCount) }
            }

            realThreadCount = if (colorMode > 1) {
                channels.channel0.threads + channels.channel1.threads + channels.channel2.threads + channels.channel3.threads
            } else {
                threadCount
            }

            val freshBitmap = createBitmap(width, height)
            AndroidCanvas(freshBitmap).drawColor(backgroundColor)
            accumulatedBitmap = freshBitmap
            stepBitmap = freshBitmap

            nails = getNails(width / 2 - 20f, 20, nailCount)
            lastCalc = System.currentTimeMillis()
        }
    }

    LaunchedEffect(lastCalc) {
        if (canvasSize == IntSize.Zero || lastCalc == 0L) return@LaunchedEffect

        fun paintThread(nail1: Int, nail2: Int, color: Int) {
            if ((2 * maxOf(nail1, nail2) + 1) >= nails.size) return

            nailsToDraw.add(Thread(nail1, nail2, color))
            threadsProg = nailsToDraw.size / realThreadCount.toFloat()
        }

        HiloramaEngine.changeStatus(false)

        withContext(Dispatchers.Default) {
            if (colorMode > 1) {
                HiloramaEngine.drawImage(0, channels.channel0.threads, channels.channel0.channel, nails, bitmap.width,
                    object : ThreadAdding {
                        override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 0)
                    })
                HiloramaEngine.drawImage(2, channels.channel2.threads, channels.channel2.channel, nails, bitmap.width,
                    object : ThreadAdding {
                        override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 2)
                    })
                HiloramaEngine.drawImage(3, channels.channel3.threads, channels.channel3.channel, nails, bitmap.width,
                    object : ThreadAdding {
                        override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 3)
                    })
                HiloramaEngine.drawImage(1, channels.channel1.threads, channels.channel1.channel, nails, bitmap.width,
                    object : ThreadAdding {
                        override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 1)
                    })
            } else {
                HiloramaEngine.drawImage(0, threadCount, grayImage, nails, bitmap.width,
                    object : ThreadAdding {
                        override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 0)
                    })
            }
            redrawTrigger++
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (needToCut) {
            bitmap?.let {
                ImageCutter(
                    bitmap,
                    (screenWidth - 32).dp,
                    assignBitmap = {
                        bitmap = it
                        needToCut = false
                        loadingThreads = true
                        lastAssign = System.currentTimeMillis()
                    },
                    onCancel = {
                        needToCut = false
                        bitmap = toDrawBitmap
                    }
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .displayCutoutPadding()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                BarIcon(
                    src = R.drawable.upload,
                    description = "Upload image",
                    color = TextPrimary,
                    onclick = {
                        launcher.launch("image/*")
                    },
                    active = true,
                    context = context
                )

            }

            var curr0 by remember { mutableStateOf(-1) }
            var curr1 by remember { mutableStateOf(-1) }

            Box(
                modifier = Modifier
                    .size(canvasContainerSize),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size ->
                            canvasSize = size
                            scope.launch { fadePosition.snapTo(size.width - 10f) }
                        }
                        .zIndex(0f)
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, _, _ ->
                                if (fadePosition.value + pan.x in 10f..size.width.toFloat() - 10f && fadding) {
                                    scope.launch { fadePosition.snapTo(fadePosition.value + pan.x) }
                                }
                            }
                        }
                ) {
                    redrawTrigger

                    drawCircle(
                        center = Offset(center.x, center.y),
                        radius = size.width / 2f - 20f,
                        color = composeBackgroundColor,
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

                    if (curr0 >= 0 && curr1 >= 0){
                        val curr0X = nails[2 * curr0]
                        val curr0Y = nails[2 * curr0 + 1]
                        val curr1X = nails[2 * curr1]
                        val curr1Y = nails[2 * curr1 + 1]
                        val color = nailsToDraw[currIndex].color

                        drawContext.canvas.nativeCanvas.drawLine(
                            curr0X, curr0Y, curr1X, curr1Y, channelPaints[color].apply { strokeWidth = 2f }
                        )

                        NumberCircle(
                            number = curr0,
                            center = Offset(curr0X, curr0Y),
                            radius = 24f,
                            canvas = this,
                            drawContext = drawContext
                        )

                        NumberCircle(
                            number = curr1,
                            center = Offset(curr1X, curr1Y),
                            radius = 24f,
                            canvas = this,
                            drawContext = drawContext
                        )
                    }

                }
            }

            val listState = rememberLazyListState()

            LaunchedEffect(indexToDraw) {
                listState.animateScrollToItem(indexToDraw)
            }

            fun selectThread(index: Int, paint: Boolean = false){
                val thread = nailsToDraw[index]
                curr0 = thread.nail1
                curr1 = thread.nail2

                val curr0X = nails[2 * curr0]
                val curr0Y = nails[2 * curr0 + 1]
                val curr1X = nails[2 * curr1]
                val curr1Y = nails[2 * curr1 + 1]
                val color = nailsToDraw[currIndex].color

                if (paint){
                    AndroidCanvas(accumulatedBitmap).drawLine(curr0X, curr0Y, curr1X, curr1Y, channelPaints[color])
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SoftBackground)
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (nailsToDraw.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp),
                                clip = false
                            ),
                        shape = RoundedCornerShape(20.dp),
                        color = SoftSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SoftBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                        ) {
                            ThreadList(
                                indexToDraw = indexToDraw,
                                nailsToDraw = nailsToDraw,
                                current = currIndex,
                                onSelect = { index ->
                                    currIndex = index
                                    selectThread(currIndex)
                                },
                                modifier = Modifier.fillMaxSize(),
                                state = listState
                            )
                        }
                    }
                }

                BottomNavigationRow(
                    onNextClick = {
                        if (indexToDraw + 1 < nailsToDraw.size){
                            indexToDraw += 1
                            currIndex = indexToDraw
                            selectThread(currIndex, true)
                        }
                    }
                )
            }
        }

        if (loadingThreads){
            ExportProgressDialog(
                threadsProg,
                title = "Creating Threads",
                {
                    loadingThreads = false
                }
            )
        }
    }
}
