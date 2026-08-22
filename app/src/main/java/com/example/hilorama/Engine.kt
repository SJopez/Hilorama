package com.example.hilorama

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.collection.MutableFloatList
import androidx.collection.mutableFloatListOf
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object HiloramaEngine {
    init {
        System.loadLibrary("Hilorama")
    }
    external fun sumita(a: Int, b: Int): Int
}

@Composable
fun MainMenu(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    ){
        Core()
    }
}

fun polarToCartesian(rho: Float, angle: Float): Offset{
    val rad = angle * Math.PI / 180
    val x = rho * cos(rad)
    val y = rho * sin(rad)
    return Offset(x.toFloat(), y.toFloat())
}

fun getNails(radius: Float, count: Int): MutableFloatList {
    val angle = (360 / count).toFloat()
    val ans = mutableFloatListOf()
    var currAngle = 0f

    for (i in 0 until count){
        val point = polarToCartesian(radius, currAngle)
        ans.add(point.x)
        ans.add(-point.y)
        currAngle += angle
    }

    return ans
}

fun imageToGray(bitmap: Bitmap): FloatArray {
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

        ans[i] = ((r * 0.299f) + (g * 0.587f) + (b * 0.114f)) / 255f
    }

    return ans
}

@Composable
fun Core(){
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val context = LocalContext.current
    val nails by remember { mutableStateOf(mutableFloatListOf()) }
    val bitmap by remember { mutableStateOf(
        BitmapFactory.decodeResource(context.resources, R.drawable.perl)
    ) }

    Canvas(
        modifier = Modifier
            .border(width = 2.dp, color = Color.Black)
            .background(color = Color(245, 245, 220))
            .size(screenWidth.dp - 20.dp)
            .clipToBounds()
    ) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)

        nails.addAll(
            getNails(width / 2f - 20f, 180)
        )

        val image = bitmap.asImageBitmap()

        val scale = max(
            width / image.width,
            height / image.height
        )

        val scaledWidth = image.width * scale
        val scaledHeight = image.height * scale

        /*
        val grayImage = imageToGray(bitmap).asImageBitmap()

        drawImage(
            image = grayImage,
            dstOffset = IntOffset(0, 0),
            dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt())
        )
        */

        var i = 0

        while (i < nails.size){
            val x = nails[i] + center.x
            val y = nails[i + 1] + center.y

            drawCircle(
                center = Offset(x, y),
                color = Color.Black,
                radius = 2f
            )

            i += 2
        }


    }
}

