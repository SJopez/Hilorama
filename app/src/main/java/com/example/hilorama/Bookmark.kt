package com.example.hilorama

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BookmarkItem(
    data: DataStep,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val imageFile = remember(data.bitmapToDrawPath) {
        File(context.filesDir, data.bitmapToDrawPath)
    }

    val imageBitmap = remember(imageFile.absolutePath) {
        if (imageFile.exists()) {
            BitmapFactory.decodeFile(imageFile.absolutePath)?.asImageBitmap()
        } else {
            null
        }
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SoftSurface),
        border = BorderStroke(1.dp, SoftBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxHeight()
                    .background(SoftBackground),
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Hilorama preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "No Img",
                        fontFamily = Jakarta,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(end = 48.dp)
                ) {
                    Text(
                        text = "Nails: ${data.nails}",
                        fontFamily = Jakarta,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Threads: ${data.threads}",
                        fontFamily = Jakarta,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                ColorModeBadge(
                    colorMode = data.colorMode,
                    modifier = Modifier.align(Alignment.TopEnd)
                )

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = "Delete hilorama",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorModeBadge(
    colorMode: Int,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, annotatedText) = when (colorMode) {
        0 -> SoftSurface to buildAnnotatedString {
            withStyle(SpanStyle(color = TextPrimary)) { append("Gray") }
        }
        1 -> TextPrimary to buildAnnotatedString {
            withStyle(SpanStyle(color = SoftSurface)) { append("Mono") }
        }
        2 -> SoftBackground to buildAnnotatedString {
            withStyle(SpanStyle(color = Color(0xFF0097A7))) { append("C") }
            withStyle(SpanStyle(color = Color(0xFFC2185B))) { append("M") }
            withStyle(SpanStyle(color = Color(0xFFF57F17))) { append("Y") }
        }
        3 -> TextPrimary to buildAnnotatedString {
            withStyle(SpanStyle(color = Color(0xFFFF5252))) { append("R") }
            withStyle(SpanStyle(color = Color(0xFF69F0AE))) { append("G") }
            withStyle(SpanStyle(color = Color(0xFF448AFF))) { append("B") }
        }
        else -> SoftBackground to buildAnnotatedString { append("?") }
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp),
        border = if (colorMode == 0 || colorMode == 2) BorderStroke(1.dp, SoftBorder) else null,
        modifier = modifier
    ) {
        Text(
            text = annotatedText,
            fontFamily = Jakarta,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = if (colorMode == 2 || colorMode == 3) 1.5.sp else 0.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Preview
@Composable
fun BookmarkMenu(){
    val context = LocalContext.current
    val dir = context.filesDir
    val fileList = dir.listFiles { file ->
        file.isFile && file.name.endsWith("HILORAMA.json")
    }

    Surface (
        modifier = Modifier
            .fillMaxSize()
            .background(color = SoftSurface)
            .displayCutoutPadding()
    ){
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            fileList?.forEach { file ->
                val data = try {
                    file.inputStream().use { stream ->
                        Json.decodeFromStream<DataStep>(stream)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }

                if (data is DataStep){
                    item {
                        BookmarkItem(
                            data
                        )
                    }
                }

            }
        }
    }
}