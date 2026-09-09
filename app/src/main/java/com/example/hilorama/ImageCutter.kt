package com.example.hilorama

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex


@Composable
fun ImageCutter(
    bitmap: Bitmap,
    sizeDp: Dp,
    onCancel: () -> Unit = {},
    assignBitmap: (Bitmap) -> Unit
) {
    val density = LocalDensity.current
    val cutoutPx = remember(sizeDp, density) { with(density) { sizeDp.toPx() } }

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    val baseScale = remember(bitmap, cutoutPx) {
        maxOf(cutoutPx / bitmap.width.toFloat(), cutoutPx / bitmap.height.toFloat())
    }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(100f),
        color = Color.Black
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val centerPivot = Offset(cutoutPx / 2f, cutoutPx / 2f)

                        val oldScale = scale
                        val newScale = (scale * zoom).coerceIn(1f, 4f)
                        val actualZoom = newScale / oldScale

                        var newX = (offset.x + pan.x - centerPivot.x) * actualZoom + centerPivot.x
                        var newY = (offset.y + pan.y - centerPivot.y) * actualZoom + centerPivot.y

                        val scaledWidth = bitmap.width * baseScale * newScale
                        val scaledHeight = bitmap.height * baseScale * newScale

                        val minX = minOf(0f, cutoutPx - scaledWidth)
                        val minY = minOf(0f, cutoutPx - scaledHeight)

                        newX = newX.coerceIn(minX, 0f)
                        newY = newY.coerceIn(minY, 0f)

                        scale = newScale
                        offset = Offset(newX, newY)
                    }
                }
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                val overlayHeight = (maxHeight - sizeDp) / 2

                Box(
                    modifier = Modifier
                        .background(color = Color.Black.copy(alpha = 0.5f))
                        .fillMaxWidth()
                        .height(overlayHeight)
                        .align(Alignment.TopStart)
                        .zIndex(2f)
                )
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Box(
                        modifier = Modifier
                            .background(color = Color.Black.copy(alpha = 0.5f))
                            .height(sizeDp)
                            .width(10.dp)
                            .zIndex(2f),
                    )
                    Box(
                        modifier = Modifier
                            .size(sizeDp)
                            .zIndex(1f)
                    ) {
                        Canvas(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            val totalScale = baseScale * scale

                            withTransform({
                                translate(
                                    left = offset.x,
                                    top = offset.y
                                )
                                scale(totalScale, totalScale, pivot = Offset.Zero)
                            }) {
                                drawImage(imageBitmap)
                            }

                            val radiusPx = size.width / 2 - 20f
                            val maskPath = Path().apply {
                                addRect(Rect(Offset.Zero, size))
                                val left = center.x - radiusPx
                                val top = center.y - radiusPx
                                val right = center.x + radiusPx
                                val bottom = center.y + radiusPx

                                addOval(
                                    Rect(
                                        left = left,
                                        top = top,
                                        right = right,
                                        bottom = bottom
                                    )
                                )
                                fillType = PathFillType.EvenOdd
                            }
                            drawPath(path = maskPath, color = Color.Black.copy(alpha = 0.6f))

                            val thinStrokeW = 1.dp.toPx()
                            val thinLineColor = Color.White.copy(alpha = 0.6f)
                            drawRect(
                                color = thinLineColor,
                                topLeft = Offset.Zero,
                                size = size,
                                style = Stroke(width = thinStrokeW)
                            )
                            drawCircle(
                                color = thinLineColor,
                                radius = radiusPx,
                                center = center,
                                style = Stroke(width = thinStrokeW)
                            )

                            val cornerLength = 40f
                            val strokeW = 4f
                            val halfStroke = strokeW / 2f
                            val lineColor = Color.White

                            val topLPath = Path().apply {
                                moveTo(cornerLength, halfStroke); lineTo(
                                halfStroke,
                                halfStroke
                            ); lineTo(halfStroke, cornerLength)
                            }
                            drawPath(
                                path = topLPath,
                                color = lineColor,
                                style = Stroke(width = strokeW, cap = StrokeCap.Square)
                            )

                            val topRPath = Path().apply {
                                moveTo(
                                    size.width - cornerLength,
                                    halfStroke
                                ); lineTo(
                                size.width - halfStroke,
                                halfStroke
                            ); lineTo(size.width - halfStroke, cornerLength)
                            }
                            drawPath(
                                path = topRPath,
                                color = lineColor,
                                style = Stroke(width = strokeW, cap = StrokeCap.Square)
                            )

                            val bottomLPath = Path().apply {
                                moveTo(
                                    cornerLength,
                                    size.height - halfStroke
                                ); lineTo(halfStroke, size.height - halfStroke); lineTo(
                                halfStroke,
                                size.height - cornerLength
                            )
                            }
                            drawPath(
                                path = bottomLPath,
                                color = lineColor,
                                style = Stroke(width = strokeW, cap = StrokeCap.Square)
                            )

                            val bottomRPath = Path().apply {
                                moveTo(
                                    size.width - cornerLength,
                                    size.height - halfStroke
                                ); lineTo(
                                size.width - halfStroke,
                                size.height - halfStroke
                            ); lineTo(size.width - halfStroke, size.height - cornerLength)
                            }
                            drawPath(
                                path = bottomRPath,
                                color = lineColor,
                                style = Stroke(width = strokeW, cap = StrokeCap.Square)
                            )

                            val sideMarkLength = 40f
                            val halfMark = sideMarkLength / 2f
                            drawLine(
                                color = lineColor,
                                start = Offset(center.x - halfMark, halfStroke),
                                end = Offset(center.x + halfMark, halfStroke),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Square
                            )
                            drawLine(
                                color = lineColor,
                                start = Offset(center.x - halfMark, size.height - halfStroke),
                                end = Offset(center.x + halfMark, size.height - halfStroke),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Square
                            )
                            drawLine(
                                color = lineColor,
                                start = Offset(halfStroke, center.y - halfMark),
                                end = Offset(halfStroke, center.y + halfMark),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Square
                            )
                            drawLine(
                                color = lineColor,
                                start = Offset(size.width - halfStroke, center.y - halfMark),
                                end = Offset(size.width - halfStroke, center.y + halfMark),
                                strokeWidth = strokeW,
                                cap = StrokeCap.Square
                            )
                            drawLine(
                                start = Offset(size.width / 2f - 20f, size.height / 2f),
                                end = Offset(size.width / 2f + 20f, size.height / 2f),
                                strokeWidth = strokeW,
                                color = lineColor,
                                cap = StrokeCap.Square
                            )
                            drawLine(
                                start = Offset(size.width / 2f, size.height / 2f - 20f),
                                end = Offset(size.width / 2f, size.height / 2f + 20f),
                                strokeWidth = strokeW,
                                color = lineColor,
                                cap = StrokeCap.Square
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(color = Color.Black.copy(alpha = 0.5f))
                            .height(sizeDp)
                            .width(10.dp)
                            .zIndex(2f),
                    )
                }
                Box(
                    modifier = Modifier
                        .background(color = Color.Black.copy(alpha = 0.5f))
                        .fillMaxWidth()
                        .height(overlayHeight)
                        .align(Alignment.BottomStart)
                        .zIndex(2f)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .zIndex(3f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        MainButton(
                            onclick = { onCancel() },
                            modifier = Modifier.fillMaxWidth(),
                            text = "Cancel",
                            containerColor = SoftBackground,
                            contentColor = TextPrimary
                        )
                    }

                    MainButton(
                        onclick = {
                            val totalScale = baseScale * scale
                            val cropSize = cutoutPx / totalScale

                            val startX = (-offset.x / totalScale).toInt().coerceAtLeast(0)
                            val startY = (-offset.y / totalScale).toInt().coerceAtLeast(0)

                            val width = minOf(cropSize.toInt(), bitmap.width - startX)
                            val height = minOf(cropSize.toInt(), bitmap.height - startY)

                            val finalCroppedBitmap = Bitmap.createBitmap(
                                bitmap, startX, startY, width, height
                            )
                            assignBitmap(finalCroppedBitmap)
                        },
                        text = "Confirm",
                        icon = true,
                        iconSrc = R.drawable.check
                    )
                }
            }
        }
    }
}
