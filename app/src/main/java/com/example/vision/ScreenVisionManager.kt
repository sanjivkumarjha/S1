package com.example.vision

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ScreenVisionManager private constructor(private val context: Context) {

    private val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private val _isScreenVisionActive = MutableStateFlow(false)
    val isScreenVisionActive: StateFlow<Boolean> = _isScreenVisionActive.asStateFlow()

    private val _lastCapturedScreenBase64 = MutableStateFlow<String?>(null)
    val lastCapturedScreenBase64: StateFlow<String?> = _lastCapturedScreenBase64.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: ScreenVisionManager? = null

        fun getInstance(context: Context): ScreenVisionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScreenVisionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun createScreenCaptureIntent(): Intent {
        return mediaProjectionManager.createScreenCaptureIntent()
    }

    fun onScreenCapturePermissionResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            try {
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
                setupVirtualDisplay()
                _isScreenVisionActive.value = true
                Log.d("ScreenVisionManager", "MediaProjection active. Live Screen Vision enabled.")
            } catch (e: Exception) {
                Log.e("ScreenVisionManager", "Failed to start MediaProjection: ${e.message}")
                _isScreenVisionActive.value = false
            }
        } else {
            _isScreenVisionActive.value = false
        }
    }

    private fun setupVirtualDisplay() {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val width = (metrics.widthPixels / 2).coerceAtLeast(360)
        val height = (metrics.heightPixels / 2).coerceAtLeast(640)
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "SnaperScreenVision",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )
    }

    suspend fun captureLiveScreenBase64(): String? = withContext(Dispatchers.IO) {
        val reader = imageReader ?: return@withContext null
        var image: Image? = null
        try {
            image = reader.acquireLatestImage() ?: reader.acquireNextImage()
            if (image != null) {
                val planes = image.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * image.width

                val bitmap = Bitmap.createBitmap(
                    image.width + rowPadding / pixelStride,
                    image.height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)

                // Crop to exact width
                val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, image.width, image.height)
                
                val outputStream = ByteArrayOutputStream()
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val bytes = outputStream.toByteArray()
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

                _lastCapturedScreenBase64.value = base64
                return@withContext base64
            }
        } catch (e: Exception) {
            Log.e("ScreenVisionManager", "Error capturing screen frame: ${e.message}")
        } finally {
            image?.close()
        }
        return@withContext _lastCapturedScreenBase64.value
    }

    fun stopScreenVision() {
        try {
            virtualDisplay?.release()
            virtualDisplay = null
            imageReader?.close()
            imageReader = null
            mediaProjection?.stop()
            mediaProjection = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isScreenVisionActive.value = false
        _lastCapturedScreenBase64.value = null
    }
}
