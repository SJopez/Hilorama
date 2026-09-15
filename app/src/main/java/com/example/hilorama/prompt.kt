package com.example.hilorama

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.random.Random


interface ImageApi {
    @Streaming
    @GET("prompt/{prompt}")
    suspend fun getImage(@Path("prompt") prompt: String,
                         @Query("seed") seed: Int): ResponseBody
}

val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://image.pollinations.ai/")
    .client(client)
    .build()
val caller = retrofit.create(ImageApi::class.java)

@Composable
fun AiPromptDialog(
    onDismissRequest: () -> Unit,
    loadBitmap: (Bitmap) -> Unit,
    launchPlaceholder: (Boolean) -> Unit,
    context: Context,
    scope: CoroutineScope,
    cancel: () -> Boolean
) {
    var aiPromptText by remember { mutableStateOf("") }

    val accentGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
    )

    val customTextSelectionColors = TextSelectionColors(
        handleColor = Color(0xFF8E2DE2),
        backgroundColor = Color(0xFF8E2DE2).copy(alpha = 0.2f)
    )

    Dialog(onDismissRequest = onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Palette.softBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.magic),
                        contentDescription = null,
                        tint = Color(0xFF8E2DE2),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Generate with AI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Palette.textPrimary,
                        fontFamily = Jakarta
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Palette.softSurface)
                            .padding(16.dp)
                    ) {
                        if (aiPromptText.isEmpty()) {
                            Text(
                                text = "Describe the image you want...",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontFamily = Jakarta
                            )
                        }

                        BasicTextField(
                            value = aiPromptText ,
                            onValueChange = { aiPromptText = it },
                            textStyle = TextStyle(
                                color = Palette.strongColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = Jakarta
                            ),
                            cursorBrush = SolidColor(Color(0xFF8E2DE2)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.05f))
                            .clickable { onDismissRequest() }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF555555),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            fontFamily = Jakarta
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val isEnabled = aiPromptText.isNotBlank()

                    Button(
                        onClick = {
                            if (isEnabled) {
                                scope.launch {
                                    try {
                                        val seed = Random.nextInt()
                                        launchPlaceholder(true)
                                        val bitmap = withContext(Dispatchers.IO) {
                                            caller.getImage(aiPromptText, seed).use { body ->
                                                BitmapFactory.decodeStream(body.byteStream())
                                            }
                                        }
                                        if (!cancel()){
                                            loadBitmap(bitmap)
                                        }
                                    } catch (e: IOException) {
                                        if (cancel()) return@launch
                                        launchPlaceholder(false)
                                        Toast.makeText(context, "❌ Connection Failed", Toast.LENGTH_SHORT).show()
                                    } catch (e: HttpException) {
                                        if (cancel()) return@launch
                                        launchPlaceholder(false)
                                        Toast.makeText(context, "❌ Try again later", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                onDismissRequest()
                            }
                        },
                        enabled = isEnabled,
                        shape = RoundedCornerShape(12.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.LightGray,
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.background(
                            brush = if (isEnabled) accentGradient else Brush.horizontalGradient(
                                listOf(Palette.softSurface, Palette.softSurface)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    ) {
                        Text(
                            text = "Generate",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Jakarta,
                            color = if (isEnabled) Color.White else Palette.textSecondary
                        )
                    }
                }
            }
        }
    }
}