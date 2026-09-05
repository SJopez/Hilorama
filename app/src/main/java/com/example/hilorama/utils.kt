package com.example.hilorama

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.graphics.createBitmap
import kotlin.math.max
import androidx.core.graphics.scale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

fun cropBitmap(original: Bitmap, width: Int,height: Int, noCut: Boolean = false): Bitmap{
    val image = original.asImageBitmap()

    val scale = max(
        width / image.width.toFloat(),
        height / image.height.toFloat()
    )

    val scaledWidth = (image.width * scale).toInt()
    val scaledHeight = (image.height * scale).toInt()
    val scaledBitmap = original.scale(scaledWidth, scaledHeight)

    if (noCut) return scaledBitmap

    val offsetX = (scaledWidth - width) / 2
    val offsetY = (scaledHeight - height) / 2
    return Bitmap.createBitmap(scaledBitmap, offsetX, offsetY, width, height)
}

fun polarToCartesian(rho: Float, angle: Float): Offset{
    val rad = angle * Math.PI / 180
    val x = rho * cos(rad)
    val y = rho * sin(rad)
    return Offset(x.toFloat(), y.toFloat())
}

fun getNails(radius: Float, padding: Int, count: Int): FloatArray {
    val angle = (360f / count)
    val ans = mutableListOf<Float>()
    var currAngle = 0f

    for (i in 0 until count){
        val point = polarToCartesian(radius, currAngle)
        ans.add(point.x + radius + padding)
        ans.add(-point.y + radius + padding)
        currAngle += angle
    }

    return ans.toFloatArray()
}

fun imageToGray(bitmap: Bitmap, mono: Boolean = false): FloatArray {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    val ans = FloatArray(width * height)

    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    for (i in 0 until pixels.size){
        val hex = pixels[i]
        val r = (hex shr 16) and 0xFF
        val g = (hex shr 8) and 0xFF
        val b = hex and 0xFF
        val light = ((r * 0.299f) + (g * 0.587f) + (b * 0.114f)) / 255f

        ans[i] = if (mono) light else 1f - light
    }

    return ans
}

class ChannelProp(
    val channel: FloatArray,
    val threads: Int
)

class Channels(
    val channel0: ChannelProp,
    val channel1: ChannelProp,
    val channel2: ChannelProp,
    val channel3: ChannelProp
)

fun balanceThreadShares(
    sums: List<Float>,
    threadCount: Int,
    minShare: Float = 0.05f,
    maxShare: Float = 0.40f,
    presenceThreshold: Float = 0.02f
): List<Int> {
    val total = sums.sum()
    val rawShares = sums.map { it / total }
    val clipped = rawShares.map { share ->
        if (share < presenceThreshold) 0f else share.coerceIn(minShare, maxShare)
    }
    val clippedTotal = clipped.sum()
    return clipped.map { ((it / clippedTotal) * threadCount).roundToInt() }
}

fun imageToCMY(bitmap: Bitmap, threadCount: Int): Channels{
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)

    val black = FloatArray(width * height)
    val cyan = FloatArray(width * height)
    val magenta = FloatArray(width * height)
    val yellow = FloatArray(width * height)

    var blackSum = 0f
    var cyanSum = 0f
    var magentaSum = 0f
    var yellowSum = 0f

    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    for (i in 0 until pixels.size){
        val hex = pixels[i]
        val r = ((hex shr 16) and 0xFF) / 255f
        val g = ((hex shr 8) and 0xFF) / 255f
        val b = (hex and 0xFF) / 255f
        val k = 1f - maxOf(r, g, b)
        black[i] = k

        if (k >= 0.9){
            cyan[i] = 0f
            magenta[i] = 0f
            yellow[i] = 0f
        }
        else {
            cyan[i] = (1f - r - k) / (1f - k)
            magenta[i] = (1f - g - k) / (1f - k)
            yellow[i] = (1f - b - k) / (1f - k)
        }

        blackSum += black[i]
        cyanSum += cyan[i]
        magentaSum += magenta[i]
        yellowSum += yellow[i]
    }

    val threadsPerChannel = balanceThreadShares(listOf(
        blackSum, cyanSum, magentaSum, yellowSum
    ), threadCount)

    return Channels(
        ChannelProp(black, threadsPerChannel[0]),
        ChannelProp(cyan, threadsPerChannel[1]),
        ChannelProp(magenta, threadsPerChannel[2]),
        ChannelProp(yellow, threadsPerChannel[3])
    )
}

fun imageToRGB(bitmap: Bitmap, threadCount: Int): Channels{
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)

    val white = FloatArray(width * height)
    val red = FloatArray(width * height)
    val green = FloatArray(width * height)
    val blue = FloatArray(width * height)

    var whiteSum = 0f
    var redSum = 0f
    var greenSum = 0f
    var blueSum = 0f

    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

    for (i in 0 until pixels.size){
        val hex = pixels[i]
        var r = ((hex shr 16) and 0xFF) / 255f
        var g = ((hex shr 8) and 0xFF) / 255f
        var b = (hex and 0xFF) / 255f
        var w = minOf(r, g, b)

        if (maxOf(r, g, b) < 0.15){
            r = 0f
            g = 0f
            b = 0f
            w = 0f
        }
        white[i] = w

        red[i] = r - w
        green[i] = g - w
        blue[i] = b - w

        whiteSum += white[i]
        redSum += red[i]
        greenSum += green[i]
        blueSum += blue[i]
    }

    val threadsPerChannel = balanceThreadShares(listOf(
        whiteSum, redSum, greenSum, blueSum
    ), threadCount)

    return Channels(
        ChannelProp(white, threadsPerChannel[0]),
        ChannelProp(red, threadsPerChannel[1]),
        ChannelProp(green, threadsPerChannel[2]),
        ChannelProp(blue, threadsPerChannel[3])
    )
}

fun bitmapToGray(source: Bitmap): Bitmap {
    val output = createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint()

    val colorMatrix = ColorMatrix().apply {
        setSaturation(0f)
    }

    val filter = ColorMatrixColorFilter(colorMatrix)
    paint.colorFilter = filter

    canvas.drawBitmap(source, 0f, 0f, paint)
    return output
}

fun makeCapture(scope: CoroutineScope, capture: GraphicsLayer, name: String, context: Context, code: (Int) -> Unit, share: Boolean = false, shareFunction: (Uri?) -> Unit = {}){
    scope.launch {
        try {
            val bitmap = capture.toImageBitmap().asAndroidBitmap()
            var uri: Uri? = Uri.EMPTY

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                val image = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "${name}.png")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    image
                )

                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                }
            }
            else{
                val id = MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    bitmap,
                    name,
                    "Hilorama Picture"
                )

                if (id != null && share){
                    uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                }
            }

            if (share){
                shareFunction(uri)
            }
            else {
                code(1)
            }
            bitmap.recycle()

        } catch (e: Exception){
            if (e.message?.contains("unique") ?: false){
                code(2)
            }
            else {
                code(3)
            }
        }
    }
}

fun saveVideoToMediaStore(context: Context, sourceFile: File): Uri? {
    val contentResolver = context.contentResolver

    val contentValues = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, "Hilorama_${System.currentTimeMillis()}.mp4")
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Hilorama")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }
    }

    val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }

    val itemUri = contentResolver.insert(collectionUri, contentValues) ?: return null

    try {
        contentResolver.openOutputStream(itemUri)?.use { outputStream ->
            sourceFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            contentResolver.update(itemUri, contentValues, null, null)
        }

        return itemUri
    } catch (e: Exception) {
        contentResolver.delete(itemUri, null, null)
        return null
    }
}

suspend fun exportStringArtVideo(
    context: Context,
    nails: FloatArray,
    states: MutableList<Thread>,
    channelPaint: List<Paint>,
    backgroundColor: Int,
    width: Int,
    height: Int,
    progressTracker: (Int) -> Unit,
    finish: () -> Unit,
    keepGenerating: () -> Boolean
) {
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    canvas.drawCircle(width / 2f, height / 2f, width / 2f - 20, Paint().apply { color = backgroundColor })

    val temp = File(context.cacheDir, "hiloramaVideo_${System.currentTimeMillis()}.mp4")
    val encoder = StringArtEncoder(temp, width, height, 30)
    var kill = false

    withContext(Dispatchers.Default) {
        encoder.start()

        states.forEachIndexed { index, thread ->
            if (!keepGenerating()){
                kill = true
                return@forEachIndexed
            }
            val nailFrom = thread.nail1
            val nailTo = thread.nail2
            val color = thread.color

            val x0 = nails[2 * nailFrom]
            val y0 = nails[2 * nailFrom + 1]
            val x1 = nails[2 * nailTo]
            val y1 = nails[2 * nailTo + 1]

            canvas.drawLine(x0, y0, x1, y1, channelPaint[color])
            if (index % 8 == 0) encoder.encodeFrame(bitmap)
            progressTracker(index)
        }

        encoder.finish()
    }

    if (kill){
        temp.delete()
        return
    }

    val videoUri = withContext(Dispatchers.IO) {
        saveVideoToMediaStore(context, temp)
    }
    temp.delete()

    finish()
    withContext(Dispatchers.Main) {
        if (videoUri != null) {
            Toast.makeText(context, "✅ Video saved correctly!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "❌ Error saving the video", Toast.LENGTH_SHORT).show()
        }
    }
}
