package com.example.hilorama

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlin.math.sin

@Composable
fun MainMenu(controller: NavHostController) {
    val context = LocalContext.current
    val githubUrl = "https://github.com/SJopez/Hilorama"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .displayCutoutPadding()
    ) {
        StringArtBackgroundDecoration()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderSection()

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(SoftSurface)
                    .border(1.dp, SoftBorder, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.silouette),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(120.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MenuOptionCard(
                    title = "Start creating",
                    subtitle = "Create String Art from image",
                    tag = "STRING ART",
                    accentColor = SoftPrimary,
                    onClick = { controller.navigate(Screens.Core.route) }
                )

                MenuOptionCard(
                    title = "Steps",
                    subtitle = "Step-by-step guide",
                    tag = "STEP",
                    accentColor = ExtraSoftPrimary,
                    onClick = { controller.navigate(Screens.Step.route) }
                )

                MenuOptionCard(
                    title = "Bookmarks",
                    subtitle = "Saved projects",
                    tag = "BOOKMARK",
                    accentColor = SoftPrimary,
                    onClick = { controller.navigate(Screens.BookMark.route) }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.github),
                    contentDescription = "GitHub Repository",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GitHub",
                    fontFamily = Jakarta,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun HeaderSection(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(top = 16.dp)
    ) {
        Text(
            text = "HILORAMA",
            fontFamily = Jakarta,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = TextPrimary,
            letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "String Art Generator",
            fontFamily = Jakarta,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = SoftPrimary
        )
    }
}

@Composable
private fun MenuOptionCard(
    title: String,
    subtitle: String,
    tag: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SoftSurface)
            .border(
                width = 1.dp,
                color = SoftBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tag,
                    fontFamily = Jakarta,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = accentColor,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    fontFamily = Jakarta,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = Jakarta,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accentColor)
            )
        }
    }
}

@Composable
private fun StringArtBackgroundDecoration() {
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnimation")
    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = SmoothLoopEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val steps = 14

        for (i in 0..steps) {
            val fraction = i.toFloat() / steps
            val phaseShift = fraction * 2.5f
            val waveAlpha = ((sin(waveProgress - phaseShift) + 1f) / 2f) * 0.35f

            val startTop = Offset(width * fraction, 0f)
            val endTop = Offset(width, height * 0.3f * (1 - fraction))
            drawLine(
                color = SoftPrimary.copy(alpha = waveAlpha),
                start = startTop,
                end = endTop,
                strokeWidth = 2.5f
            )

            val startBottom = Offset(0f, height - (height * 0.3f * fraction))
            val endBottom = Offset(width * (1 - fraction), height)
            drawLine(
                color = ExtraSoftPrimary.copy(alpha = waveAlpha),
                start = startBottom,
                end = endBottom,
                strokeWidth = 2.5f
            )
        }
    }
}

private val SmoothLoopEasing = Easing { fraction -> fraction }