package com.example.hilorama

import android.graphics.Bitmap
import android.media.Image
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File
class StringArtEncoder(
    outputFile: File,
    private val width: Int,
    private val height: Int,
    frameDurationMs: Long
) {
    private val frameDurationUs = frameDurationMs * 1_000L
    private val frameSize = width * height * 3 / 2
    private var frameIndex = 0L

    private val codec: MediaCodec
    private val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    private var muxerTrackIndex = -1
    private var muxerStarted = false
    private val argbPixels = IntArray(width * height)

    init {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
            setInteger(MediaFormat.KEY_BIT_RATE, 12_000_000)
            setInteger(MediaFormat.KEY_FRAME_RATE, (1000L / frameDurationMs).toInt().coerceAtLeast(1))
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    }

    fun start() {
        codec.start()
    }

    fun encodeFrame(bitmap: Bitmap) {
        bitmap.getPixels(argbPixels, 0, width, 0, 0, width, height)

        val inputIndex = codec.dequeueInputBuffer(10_000L)

        if (inputIndex >= 0) {
            val image = codec.getInputImage(inputIndex)
            val presentationTimeUs = frameIndex * frameDurationUs

            if (image != null) {
                fillImagePlanes(image, argbPixels, width, height)
                codec.queueInputBuffer(inputIndex, 0, frameSize, presentationTimeUs, 0)
            } else {
                codec.queueInputBuffer(inputIndex, 0, 0, presentationTimeUs, 0)
            }
            frameIndex++
        }

        drainEncoder(endOfStream = false)
    }

    fun finish() {
        val inputIndex = codec.dequeueInputBuffer(10_000L)
        if (inputIndex >= 0) {
            codec.queueInputBuffer(inputIndex, 0, 0, frameIndex * frameDurationUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
        }
        drainEncoder(endOfStream = true)

        codec.stop()
        codec.release()

        if (muxerStarted) {
            muxer.stop()
        }
        muxer.release()
    }

    private fun drainEncoder(endOfStream: Boolean) {
        val bufferInfo = MediaCodec.BufferInfo()

        while (true) {
            val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000L)

            when {
                outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                    if (!endOfStream) return
                }
                outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                    check(!muxerStarted) { "Output format changed" }
                    muxerTrackIndex = muxer.addTrack(codec.outputFormat)
                    muxer.start()
                    muxerStarted = true
                }
                outputIndex >= 0 -> {
                    val outputBuffer = codec.getOutputBuffer(outputIndex)

                    if (bufferInfo.size > 0 && outputBuffer != null && muxerStarted) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(muxerTrackIndex, outputBuffer, bufferInfo)
                    }

                    codec.releaseOutputBuffer(outputIndex, false)

                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        return
                    }
                }
            }
        }
    }
}
private fun fillImagePlanes(image: Image, argb: IntArray, width: Int, height: Int) {
    val yPlane = image.planes[0]
    val uPlane = image.planes[1]
    val vPlane = image.planes[2]

    val yBuffer = yPlane.buffer
    val uBuffer = uPlane.buffer
    val vBuffer = vPlane.buffer

    for (row in 0 until height) {
        val rowBase = row * width
        var yPos = row * yPlane.rowStride
        for (col in 0 until width) {
            val pixel = argb[rowBase + col]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            val y = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)
            yBuffer.put(yPos, y.toByte())
            yPos++
        }
    }

    val chromaWidth = width / 2
    val chromaHeight = height / 2

    for (row in 0 until chromaHeight) {
        var uPos = row * uPlane.rowStride
        var vPos = row * vPlane.rowStride

        for (col in 0 until chromaWidth) {
            val pixel = argb[(row * 2) * width + (col * 2)]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            val u = (-0.169f * r - 0.331f * g + 0.5f * b + 128f).toInt().coerceIn(0, 255)
            val v = (0.5f * r - 0.419f * g - 0.081f * b + 128f).toInt().coerceIn(0, 255)

            uBuffer.put(uPos, u.toByte())
            vBuffer.put(vPos, v.toByte())
            uPos += uPlane.pixelStride
            vPos += vPlane.pixelStride
        }
    }
}