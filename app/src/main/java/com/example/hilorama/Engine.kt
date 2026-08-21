package com.example.hilorama

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.cosh
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

@Composable
fun Core(){
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val nails by remember { mutableStateOf(mutableFloatListOf()) }

    Canvas(
        modifier = Modifier
            .border(width = 2.dp, color = Color.Black)
            .background(color = Color(245, 245, 220))
            .size(screenWidth.dp - 20.dp)
    ) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)

        nails.addAll(
            getNails(width / 2f - 20f, 180)
        )

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

