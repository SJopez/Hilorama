package com.example.hilorama

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.graphics.blue
import androidx.core.graphics.createBitmap
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
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
    var croppedBitmap by remember { mutableStateOf(createBitmap(1, 1)) }

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

    val strongChannel = remember(colorMode) {
        when(colorMode) {
            1 -> listOf(
                Color.White
            )
            2 -> listOf (
                Color.Black,
                Color.Cyan,
                Color.Magenta,
                Color.Yellow
            )
            3 -> listOf(
                Color.White,
                Color.Red,
                Color.Green,
                Color.Blue
            )
            else -> listOf(Color.Black)
        }
    }

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

    var showHelp by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var id by remember { mutableStateOf(System.currentTimeMillis()) }
    var needToCut by remember { mutableStateOf(true) }
    var confiStep by remember { mutableStateOf(false) }
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
                HiloramaEngine.drawImage(0, channels.channel0.threads, channels.channel0.channel, nails, croppedBitmap.width,
                    object : ThreadAdding {
                        override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 0)
                    })
                HiloramaEngine.drawImage(2, channels.channel2.threads, channels.channel2.channel, nails, croppedBitmap.width,
                    object : ThreadAdding {
                        override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 2)
                    })
                HiloramaEngine.drawImage(3, channels.channel3.threads, channels.channel3.channel, nails, croppedBitmap.width,
                    object : ThreadAdding {
                        override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 3)
                    })
                HiloramaEngine.drawImage(1, channels.channel1.threads, channels.channel1.channel, nails, croppedBitmap.width,
                    object : ThreadAdding {
                        override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 1)
                    })
            } else {
                HiloramaEngine.drawImage(0, threadCount, grayImage, nails, croppedBitmap.width,
                    object : ThreadAdding {
                        override fun addThread(nail1: Int, nail2: Int) = paintThread(nail1, nail2, 0)
                    })
            }
            redrawTrigger++
        }
    }

    var curr0 by remember { mutableStateOf(-1) }
    var curr1 by remember { mutableStateOf(-1) }

    fun selectThread(index: Int, paint: Boolean = false){
        val thread = nailsToDraw[index]
        curr0 = thread.nail1
        curr1 = thread.nail2

        val curr0X = nails[2 * curr0]
        val curr0Y = nails[2 * curr0 + 1]
        val curr1X = nails[2 * curr1]
        val curr1Y = nails[2 * curr1 + 1]
        val color = nailsToDraw[index].color

        if (paint){
            AndroidCanvas(accumulatedBitmap).drawLine(curr0X, curr0Y, curr1X, curr1Y, channelPaints[color])
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(color = SoftBackground)
    ) {
        if (needToCut) {
            bitmap?.let {
                ImageCutter(
                    bitmap,
                    (screenWidth - 20).dp,
                    assignBitmap = {
                        saved = false
                        id = System.currentTimeMillis()
                        bitmap = it
                        toDrawBitmap = if (colorMode <= 1) bitmapToGray(bitmap) else bitmap
                        toDrawBitmap = cropBitmap(toDrawBitmap, canvasSize.width, canvasSize.height)

                        confiStep = true
                        needToCut = false
                    },
                    onCancel = {
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
            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center
            ){
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ){
                    CircularButton(
                        onclick = {
                            fadding = !fadding

                            scope.launch {
                                fadePosition.animateTo(
                                    targetValue = if (fadding) canvasSize.width / 2f else canvasSize.width.toFloat(),
                                    animationSpec = tween(durationMillis = 400)
                                )
                            }
                        },
                        description = "Fade",
                        icon = R.drawable.fade,
                        tint = if (fadding) Color.White else SoftPrimary,
                        modifier = Modifier.then(
                            if (fadding) {
                                Modifier.background(SoftPrimary, CircleShape)
                            } else {
                                Modifier.border(2.dp, SoftPrimary, CircleShape)
                            }
                        )
                    )
                    var lapse by remember { mutableStateOf(false) }

                    CircularButton(
                        onclick = {
                            if (!lapse) {
                                lapse = true
                                scope.launch {
                                    saveDataStep(
                                        toDrawBitmap,
                                        accumulatedBitmap,
                                        nailsToDraw,
                                        currIndex,
                                        id,
                                        nailCount,
                                        threadCount,
                                        colorMode,
                                        context
                                    )
                                }
                                saved = true
                                lapse = false
                                Toast.makeText(context, "✅ Bookmark saved!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        description = "Add to complete later...",
                        tint = if (saved) Color.White else SoftPrimary,
                        icon = if (saved) R.drawable.bookmark_check else R.drawable.bookmark,
                        modifier = Modifier.then(
                            if (saved) {
                                Modifier.background(SoftPrimary, CircleShape)
                            }
                            else {
                                Modifier.border(2.dp, SoftPrimary, CircleShape)
                            }
                        )
                    )
                    CircularButton(
                        onclick = {
                            showHelp = true
                        },
                        description = "Throw help menu",
                        icon = R.drawable.help
                    )
                    CircularButton(
                        onclick = {
                            launcher.launch("image/*")
                        },
                        description = "Throw image selector",
                        icon = R.drawable.upload
                    )
                }
            }

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
                            nails = getNails(size.width / 2 - 20f, 20, nailCount)
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
                        drawCircle(
                            center = Offset(x, y),
                            color = if (colorMode % 2 == 0) Color.Black else Color.White,
                            radius = 1.2f
                        )

                        i += 2
                    }

                    if (curr0 >= 0 && curr1 >= 0 && nailsToDraw.size > 0){
                        val curr0X = nails[2 * curr0]
                        val curr0Y = nails[2 * curr0 + 1]
                        val curr1X = nails[2 * curr1]
                        val curr1Y = nails[2 * curr1 + 1]
                        val colorIndex = nailsToDraw[currIndex].color

                        val lineBg = Paint().apply {
                            color = backgroundColor;
                            strokeWidth = 6f;
                        }

                        val lineStroke = Paint().apply {
                            color = strongChannel[colorIndex].toArgb();
                            strokeWidth = 3f;
                            xfermode = channelPaints[colorIndex].xfermode
                        }

                        drawContext.canvas.nativeCanvas.drawLine(
                            curr0X, curr0Y, curr1X, curr1Y, lineBg
                        )

                        drawContext.canvas.nativeCanvas.drawLine(
                            curr0X, curr0Y, curr1X, curr1Y, lineStroke
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SoftBackground)
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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
                    border = BorderStroke(1.dp, SoftBorder)
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
                            state = listState,
                            channel = strongChannel,
                        )
                    }
                }

                BottomNavigationRow(
                    onNextClick = {
                        if (indexToDraw + 2 < nailsToDraw.size){
                            indexToDraw += 1
                            currIndex = indexToDraw
                            selectThread(currIndex, true)
                        }
                    }
                )
            }
        }

        LaunchedEffect(threadsProg) {
            if (nailsToDraw.size == realThreadCount) {
                loadingThreads = false
                indexToDraw = 0
                currIndex = 0
                selectThread(0, true)
            }
        }

        if (loadingThreads){
            ExportProgressDialog(
                threadsProg,
                title = "Creating Threads",
                {

                }
            )
        }
        if (showHelp){
            ThreadStepHelpDialog(
                onDismissRequest = {
                    showHelp = false
                }
            )
        }
        if (confiStep){
            Dialog(onDismissRequest = { }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = SoftSurface,
                    border = BorderStroke(1.5.dp, SoftBorder),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(
                            text = "Configuration",
                            fontFamily = Jakarta,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        HiloramaControls(
                            nailCount = nailCount,
                            onNailCountChange = { nails ->
                                nailCount = nails
                            },
                            threadCount = threadCount,
                            onThreadCountChange = { threads ->
                                threadCount = threads
                            },
                            threadCountRange = 1000..8000,
                            colorMode = colorMode,
                            onColorModeChange = { channel ->
                                if (channel > 1 && colorMode <= 1) toDrawBitmap = bitmap
                                else if (channel <= 1 && colorMode > 1) toDrawBitmap = bitmapToGray(bitmap)
                                toDrawBitmap = cropBitmap(toDrawBitmap, canvasSize.width, canvasSize.height)
                                colorMode = channel
                            },
                            active = true,
                            context = context,
                            borders = false
                        )
                        MainButton(
                            onclick = {
                                curr0 = -1
                                curr1 = -1
                                loadingThreads = true
                                confiStep = false
                                nailsToDraw.clear()
                                lastAssign = System.currentTimeMillis()
                            },
                            text = "Create",
                        )
                    }
                }
            }
        }
    }
}

