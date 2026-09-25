package com.example.generator.video

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.cos
import kotlin.math.sin

object VideoGenerator {

    data class VideoGenConfig(
        val title: String,
        val prompt: String,
        val styleName: String,
        val cameraMovement: String,
        val width: Int = 720,
        val height: Int = 1280,
        val fps: Int = 30,
        val durationSeconds: Int = 4,
        val primaryColor: Int = Color.parseColor("#00E5FF"),
        val accentColor: Int = Color.parseColor("#B388FF")
    )

    suspend fun generateMp4Video(
        context: Context,
        config: VideoGenConfig,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.Default) {
        val outputDir = File(context.filesDir, "generated_videos")
        if (!outputDir.exists()) outputDir.mkdirs()

        val fileName = "cinegen_${System.currentTimeMillis()}.mp4"
        val outputFile = File(outputDir, fileName)

        val totalFrames = config.fps * config.durationSeconds
        val bitRate = 2_500_000
        val frameDurationUs = 1_000_000L / config.fps

        // Check AVC encoder capabilities
        val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
        val codec = MediaCodec.createEncoderByType(mimeType)

        // Find supported color format
        val codecInfo = codec.codecInfo
        val capabilities = codecInfo.getCapabilitiesForType(mimeType)
        val supportedFormats = capabilities.colorFormats

        val selectedColorFormat = when {
            supportedFormats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) ->
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
            supportedFormats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) ->
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar
            else -> supportedFormats.firstOrNull() ?: MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
        }

        val format = MediaFormat.createVideoFormat(mimeType, config.width, config.height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, selectedColorFormat)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, config.fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false

        val bufferInfo = MediaCodec.BufferInfo()
        val bitmap = Bitmap.createBitmap(config.width, config.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val isSemiPlanar = (selectedColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)
        val yuvBuffer = ByteArray(config.width * config.height * 3 / 2)
        val argbArray = IntArray(config.width * config.height)

        try {
            for (frameIndex in 0 until totalFrames) {
                val progress = frameIndex.toFloat() / totalFrames
                withContext(Dispatchers.Main) {
                    onProgress(progress)
                }

                // Render dynamic frame
                renderFrame(canvas, config, frameIndex, totalFrames)

                // Convert Bitmap to YUV420
                bitmap.getPixels(argbArray, 0, config.width, 0, 0, config.width, config.height)
                encodeYUV420(argbArray, yuvBuffer, config.width, config.height, isSemiPlanar)

                // Feed to MediaCodec
                val presentationTimeUs = frameIndex * frameDurationUs
                var inputIndex = codec.dequeueInputBuffer(10_000)
                while (inputIndex < 0) {
                    drainEncoder(codec, muxer, bufferInfo, false) { index ->
                        trackIndex = index
                        muxerStarted = true
                    }
                    inputIndex = codec.dequeueInputBuffer(10_000)
                }

                if (inputIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputIndex)
                    inputBuffer?.clear()
                    inputBuffer?.put(yuvBuffer)
                    val isLast = (frameIndex == totalFrames - 1)
                    codec.queueInputBuffer(
                        inputIndex,
                        0,
                        yuvBuffer.size,
                        presentationTimeUs,
                        if (isLast) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0
                    )
                }

                drainEncoder(codec, muxer, bufferInfo, false) { index ->
                    trackIndex = index
                    muxerStarted = true
                }
            }

            // Finish draining encoder until EOS
            drainEncoder(codec, muxer, bufferInfo, true) { index ->
                trackIndex = index
                muxerStarted = true
            }

            withContext(Dispatchers.Main) {
                onProgress(1.0f)
            }
        } finally {
            try {
                codec.stop()
                codec.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
            if (muxerStarted) {
                try {
                    muxer.stop()
                    muxer.release()
                } catch (e: Exception) {
                    // Ignore muxer cleanup errors
                }
            }
            bitmap.recycle()
        }

        outputFile
    }

    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        onTrackAdded: (Int) -> Unit
    ) {
        var trackIndex = -1
        while (true) {
            val encoderStatus = codec.dequeueOutputBuffer(bufferInfo, 10_000)
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = codec.outputFormat
                trackIndex = muxer.addTrack(newFormat)
                muxer.start()
                onTrackAdded(trackIndex)
            } else if (encoderStatus >= 0) {
                val encodedData = codec.getOutputBuffer(encoderStatus)
                if (encodedData != null) {
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        bufferInfo.size = 0
                    }

                    if (bufferInfo.size != 0) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(if (trackIndex >= 0) trackIndex else 0, encodedData, bufferInfo)
                    }

                    codec.releaseOutputBuffer(encoderStatus, false)

                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break
                    }
                }
            }
        }
    }

    private fun renderFrame(
        canvas: Canvas,
        config: VideoGenConfig,
        frameIndex: Int,
        totalFrames: Int
    ) {
        val w = config.width.toFloat()
        val h = config.height.toFloat()
        val t = frameIndex.toFloat() / totalFrames
        val phase = t * 2 * Math.PI.toFloat()

        // 1. Dark Cinematic Gradient Background
        val bgGradient = LinearGradient(
            0f, 0f,
            w * sin(phase * 0.5f), h,
            intArrayOf(
                Color.parseColor("#06080C"),
                Color.parseColor("#0F141F"),
                Color.parseColor("#171D2B")
            ),
            null,
            Shader.TileMode.CLAMP
        )
        val bgPaint = Paint().apply { shader = bgGradient }
        canvas.drawRect(0f, 0f, w, h, bgPaint)

        // 2. Camera movement simulation (Scale / Pan / Zoom)
        val zoom = when (config.cameraMovement) {
            "Slow Zoom In" -> 1.0f + (t * 0.35f)
            "Drone Aerial" -> 1.3f - (t * 0.25f)
            "Orbit Right" -> 1.0f + sin(phase) * 0.1f
            else -> 1.05f + (t * 0.1f)
        }
        val panX = when (config.cameraMovement) {
            "Pan Left" -> (1f - t) * 100f - 50f
            "Orbit Right" -> sin(phase) * 80f
            else -> 0f
        }

        canvas.save()
        canvas.translate(w / 2f + panX, h / 2f)
        canvas.scale(zoom, zoom)
        canvas.translate(-w / 2f, -h / 2f)

        // 3. Dynamic Animated Light Orbs & Volumetric Beams
        val orbX = w * (0.5f + 0.3f * cos(phase + 0.5f))
        val orbY = h * (0.4f + 0.2f * sin(phase * 1.5f))
        val orbRadius = (w * 0.5f) * (0.8f + 0.2f * sin(phase))

        val orbPaint = Paint().apply {
            shader = RadialGradient(
                orbX, orbY, orbRadius,
                intArrayOf(config.primaryColor, config.accentColor, Color.TRANSPARENT),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            alpha = (140 + (50 * sin(phase))).toInt().coerceIn(0, 255)
        }
        canvas.drawCircle(orbX, orbY, orbRadius, orbPaint)

        // Secondary counter-rotating orb
        val orb2X = w * (0.5f - 0.25f * cos(phase * 0.8f))
        val orb2Y = h * (0.6f + 0.2f * cos(phase))
        val orb2Paint = Paint().apply {
            shader = RadialGradient(
                orb2X, orb2Y, orbRadius * 0.8f,
                intArrayOf(config.accentColor, Color.parseColor("#FF4081"), Color.TRANSPARENT),
                floatArrayOf(0f, 0.4f, 1f),
                Shader.TileMode.CLAMP
            )
            alpha = 110
        }
        canvas.drawCircle(orb2X, orb2Y, orbRadius * 0.8f, orb2Paint)

        // 4. Floating Cinematic Particle Stars
        val particlePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        for (i in 0 until 40) {
            val seedX = ((i * 137.5f) % w)
            val seedY = ((i * 283.1f) % h)
            val speed = (i % 5 + 1) * 15f
            val py = (seedY + t * speed * 20f) % h
            val px = (seedX + sin(phase + i) * 20f) % w
            val pRadius = (1.5f + (i % 4)).coerceAtMost(6f)
            particlePaint.alpha = (120 + 130 * sin(phase * 2f + i)).toInt().coerceIn(40, 255)
            canvas.drawCircle(px, py, pRadius, particlePaint)
        }

        canvas.restore()

        // 5. Cinematic Vignette
        val vignettePaint = Paint().apply {
            shader = RadialGradient(
                w / 2f, h / 2f, (w.coerceAtLeast(h) * 0.7f),
                intArrayOf(Color.TRANSPARENT, Color.argb(190, 0, 0, 0)),
                floatArrayOf(0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, w, h, vignettePaint)

        // 6. Lower Third Cinematic Title & Prompt Badge
        val barPaint = Paint().apply {
            color = Color.argb(160, 10, 14, 22)
            style = Paint.Style.FILL
        }
        val barTop = h - 220f
        canvas.drawRect(0f, barTop, w, h, barPaint)

        // Glow line above lower third
        val linePaint = Paint().apply {
            color = config.primaryColor
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(0f, barTop, w, barTop, linePaint)

        // Title Text
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(config.title.take(30), 40f, barTop + 55f, titlePaint)

        // Style & Motion Tag
        val tagPaint = Paint().apply {
            color = config.primaryColor
            textSize = 24f
            isAntiAlias = true
        }
        canvas.drawText("${config.styleName.uppercase()} • ${config.cameraMovement}", 40f, barTop + 95f, tagPaint)

        // Prompt snippet
        val promptPaint = Paint().apply {
            color = Color.parseColor("#B0BEC5")
            textSize = 20f
            isAntiAlias = true
        }
        val displayPrompt = if (config.prompt.length > 55) config.prompt.take(52) + "..." else config.prompt
        canvas.drawText("\"$displayPrompt\"", 40f, barTop + 135f, promptPaint)

        // Studio Watermark / Brand Header
        val brandPaint = Paint().apply {
            color = Color.argb(180, 255, 255, 255)
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("CINEGEN AI STUDIO", 40f, 60f, brandPaint)
    }

    // Convert ARGB pixel array to YUV420 Planar / Semi-Planar
    private fun encodeYUV420(
        argb: IntArray,
        yuv: ByteArray,
        width: Int,
        height: Int,
        isSemiPlanar: Boolean
    ) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var uIndex = frameSize
        var vIndex = frameSize + frameSize / 4

        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                val c = argb[index++]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF

                // RGB to YUV conversion formula
                val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                yuv[yIndex++] = (y.coerceIn(0, 255)).toByte()

                if (j % 2 == 0 && i % 2 == 0) {
                    val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                    val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128

                    if (isSemiPlanar) {
                        // NV12 format: U then V
                        yuv[uvIndex++] = (u.coerceIn(0, 255)).toByte()
                        yuv[uvIndex++] = (v.coerceIn(0, 255)).toByte()
                    } else {
                        // YUV420P format
                        yuv[uIndex++] = (u.coerceIn(0, 255)).toByte()
                        yuv[vIndex++] = (v.coerceIn(0, 255)).toByte()
                    }
                }
            }
        }
    }
}
