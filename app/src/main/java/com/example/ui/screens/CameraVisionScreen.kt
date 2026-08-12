package com.example.ui.screens

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.data.api.AiRepository
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
fun CameraVisionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraObj: Camera? by remember { mutableStateOf(null) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }

    var isCapturing by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var showFlashEffect by remember { mutableStateOf(false) }

    var lastCapturedFile by remember { mutableStateOf<File?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPhotoPreviewDialog by remember { mutableStateOf(false) }
    var showLocalGallerySheet by remember { mutableStateOf(false) }

    var capturedPhotosList by remember { mutableStateOf<List<File>>(emptyList()) }

    var photoToDelete by remember { mutableStateOf<File?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var isTaggingPhoto by remember { mutableStateOf(false) }
    var currentPhotoTag by remember { mutableStateOf("") }

    var visionResultText by remember {
        mutableStateOf("Point the camera at any object or scene. Tap 'Analyze Scene' for AI vision or the Shutter button to take photos 📷")
    }

    val userPrefs = remember { UserPreferencesRepository(context) }
    val aiRepo = remember { AiRepository(context) }

    fun refreshCapturedPhotos() {
        coroutineScope.launch(Dispatchers.IO) {
            val photos = loadCapturedPhotosList(context)
            withContext(Dispatchers.Main) {
                capturedPhotosList = photos
                if (photos.isNotEmpty() && lastCapturedFile == null) {
                    val latest = photos.first()
                    lastCapturedFile = latest
                    previewBitmap = BitmapFactory.decodeFile(latest.absolutePath)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
        refreshCapturedPhotos()
    }

    // Flash animation trigger
    LaunchedEffect(showFlashEffect) {
        if (showFlashEffect) {
            kotlinx.coroutines.delay(120)
            showFlashEffect = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Camera & Vision", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFDC2626))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🔴 LIVE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Open Local Photo Gallery
                    IconButton(onClick = {
                        refreshCapturedPhotos()
                        showLocalGallerySheet = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Local Photo Gallery",
                            tint = Color.White
                        )
                    }

                    // Flash Mode Toggle
                    IconButton(onClick = {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                            else -> ImageCapture.FLASH_MODE_OFF
                        }
                        imageCapture?.flashMode = flashMode
                    }) {
                        val (icon, label) = when (flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn to "Flash On"
                            ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto to "Flash Auto"
                            else -> Icons.Default.FlashOff to "Flash Off"
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (flashMode != ImageCapture.FLASH_MODE_OFF) Color(0xFFFBBF24) else Color.White
                        )
                    }

                    // Flip Camera
                    IconButton(onClick = {
                        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Switch Camera",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.8f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            if (cameraPermissionState.status.isGranted) {
                // CameraX PreviewView
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val capture = ImageCapture.Builder()
                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .setFlashMode(flashMode)
                                .build()
                            imageCapture = capture

                            try {
                                cameraProvider.unbindAll()
                                cameraObj = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    capture
                                )
                            } catch (e: Exception) {
                                Log.e("CameraVision", "Use case binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // White Flash Animation overlay on Capture
                if (showFlashEffect) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.75f))
                    )
                }

                // Camera Controls & AI Box at bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Zoom Slider
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Zoom", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Slider(
                            value = zoomRatio,
                            onValueChange = { newZoom ->
                                zoomRatio = newZoom
                                cameraObj?.cameraControl?.setZoomRatio(newZoom)
                            },
                            valueRange = 1f..5f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF8B5CF6),
                                activeTrackColor = Color(0xFF8B5CF6),
                                inactiveTrackColor = Color.DarkGray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("%.1fx", zoomRatio),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // AI Visual Analysis Text Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E1B2E))
                            .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Visual Intelligence",
                                    color = Color(0xFFA78BFA),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = visionResultText,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Action Bar: [Thumbnail Gallery] --- [Shutter Button] --- [AI Vision Action]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Last Captured Thumbnail or Open Gallery
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF27272A))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable {
                                    refreshCapturedPhotos()
                                    if (previewBitmap != null) {
                                        showPhotoPreviewDialog = true
                                    } else {
                                        showLocalGallerySheet = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (previewBitmap != null) {
                                Image(
                                    bitmap = previewBitmap!!.asImageBitmap(),
                                    contentDescription = "Last captured photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "Gallery",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // 2. Primary Shutter Button (Capture Photo)
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(3.dp, Color.White, CircleShape)
                                .clickable(enabled = !isCapturing && !isAnalyzing) {
                                    val capture = imageCapture ?: return@clickable
                                    isCapturing = true
                                    showFlashEffect = true

                                    val targetDir = context.getExternalFilesDir("captured_photos") ?: File(context.filesDir, "captured_photos").apply { mkdirs() }
                                    val photoFile = File(targetDir, "SnaperPhoto_${System.currentTimeMillis()}.jpg")
                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                    capture.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                coroutineScope.launch {
                                                    val bitmap = withContext(Dispatchers.IO) {
                                                        BitmapFactory.decodeFile(photoFile.absolutePath)
                                                    }
                                                    lastCapturedFile = photoFile
                                                    previewBitmap = bitmap
                                                    isCapturing = false
                                                    currentPhotoTag = getPhotoTag(photoFile)
                                                    refreshCapturedPhotos()
                                                    Toast.makeText(context, "Photo captured & saved to gallery!", Toast.LENGTH_SHORT).show()
                                                    showPhotoPreviewDialog = true

                                                    // Auto-tag photo using Gemini AI Vision in background
                                                    if (currentPhotoTag.isBlank()) {
                                                        isTaggingPhoto = true
                                                        val tag = autoClassifyPhoto(context, photoFile, aiRepo, userPrefs)
                                                        withContext(Dispatchers.Main) {
                                                            currentPhotoTag = tag
                                                            isTaggingPhoto = false
                                                            refreshCapturedPhotos()
                                                        }
                                                    }
                                                }
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                isCapturing = false
                                                Toast.makeText(context, "Capture error: ${exception.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (isCapturing) Color.Red else Color.White)
                            )
                        }

                        // 3. AI Scene Scanner Button
                        IconButton(
                            onClick = {
                                val capture = imageCapture ?: return@IconButton
                                isAnalyzing = true
                                visionResultText = "Analyzing camera frame with AI..."

                                val photoFile = File(context.cacheDir, "camera_vision_temp.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                                capture.takePicture(
                                    outputOptions,
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                            coroutineScope.launch {
                                                try {
                                                    val bitmap = withContext(Dispatchers.IO) {
                                                        BitmapFactory.decodeFile(photoFile.absolutePath)
                                                    }
                                                    previewBitmap = bitmap
                                                    lastCapturedFile = photoFile

                                                    val settings = userPrefs.userSettingsFlow.firstOrNull() ?: UserSettings()
                                                    val prompt = "Describe what you see in this image in detail. If there is text, read it out clearly. Respond in ${if (settings.languageCode == "hi") "Hindi" else "English"}."

                                                    val byteArrayOutputStream = ByteArrayOutputStream()
                                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
                                                    val base64Image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)

                                                    val response = aiRepo.generateAssistantResponse(
                                                        prompt = prompt,
                                                        history = emptyList(),
                                                        memories = emptyList(),
                                                        userSettings = settings,
                                                        attachedImageBase64 = base64Image
                                                    )
                                                    visionResultText = response.ifBlank { "Could not analyze frame." }
                                                } catch (e: Exception) {
                                                    visionResultText = "Analysis error: ${e.localizedMessage}"
                                                } finally {
                                                    isAnalyzing = false
                                                }
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            isAnalyzing = false
                                            visionResultText = "Capture failed: ${exception.message}"
                                        }
                                    }
                                )
                            },
                            enabled = !isAnalyzing && !isCapturing,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF8B5CF6))
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.DocumentScanner,
                                    contentDescription = "Analyze Scene",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                // Permission Request Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Snaper AI needs camera access to capture photos, preview camera feed, and analyze objects and text.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Grant Camera Permission")
                    }
                }
            }
        }
    }

    // Photo Preview Dialog Modal
    if (showPhotoPreviewDialog && previewBitmap != null && lastCapturedFile != null) {
        val currentPhoto = lastCapturedFile!!
        LaunchedEffect(currentPhoto.absolutePath) {
            val tag = getPhotoTag(currentPhoto)
            currentPhotoTag = tag
            if (tag.isBlank()) {
                isTaggingPhoto = true
                val newTag = autoClassifyPhoto(context, currentPhoto, aiRepo, userPrefs)
                currentPhotoTag = newTag
                isTaggingPhoto = false
                refreshCapturedPhotos()
            }
        }

        Dialog(
            onDismissRequest = { showPhotoPreviewDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                color = Color.Black
            ) {
                var isAnimatedIn by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    isAnimatedIn = true
                }

                val scaleAnim by animateFloatAsState(
                    targetValue = if (isAnimatedIn) 1f else 0.65f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                    label = "photoZoomScale"
                )
                val alphaAnim by animateFloatAsState(
                    targetValue = if (isAnimatedIn) 1f else 0f,
                    animationSpec = tween(durationMillis = 280),
                    label = "photoZoomAlpha"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scaleAnim
                            scaleY = scaleAnim
                            alpha = alphaAnim
                        }
                ) {
                    Image(
                        bitmap = previewBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Photo Preview",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showPhotoPreviewDialog = false },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }

                        // AI Category Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1E1B2E).copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sell,
                                    contentDescription = null,
                                    tint = Color(0xFFA78BFA),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isTaggingPhoto) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        color = Color.White,
                                        strokeWidth = 1.5.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("AI Auto-Tagging...", color = Color.White, fontSize = 11.sp)
                                } else {
                                    Text(
                                        text = if (currentPhotoTag.isNotBlank()) "Category: $currentPhotoTag" else "Category: Uncategorized",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Delete Photo Option
                            IconButton(
                                onClick = {
                                    photoToDelete = currentPhoto
                                    showDeleteConfirmDialog = true
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete photo", tint = Color(0xFFEF4444))
                            }

                            // Download / Save to System Gallery
                            IconButton(
                                onClick = {
                                    val uri = savePhotoToSystemGallery(context, currentPhoto)
                                    if (uri != null) {
                                        Toast.makeText(context, "Saved to Pictures/SnaperAI gallery!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Saved in local app storage", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Save to device", tint = Color.White)
                            }
                        }
                    }

                    // Bottom Bar Options
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Analyze Button
                            Button(
                                onClick = {
                                    showPhotoPreviewDialog = false
                                    isAnalyzing = true
                                    visionResultText = "Analyzing photo..."

                                    coroutineScope.launch {
                                        try {
                                            val settings = userPrefs.userSettingsFlow.firstOrNull() ?: UserSettings()
                                            val prompt = "Describe what you see in this photo in full detail. Include text, objects, and key visual features."
                                            val byteArrayOutputStream = ByteArrayOutputStream()
                                            previewBitmap?.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
                                            val base64Image = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)

                                            val response = aiRepo.generateAssistantResponse(
                                                prompt = prompt,
                                                history = emptyList(),
                                                memories = emptyList(),
                                                userSettings = settings,
                                                attachedImageBase64 = base64Image
                                            )
                                            visionResultText = response.ifBlank { "Could not analyze photo." }
                                        } catch (e: Exception) {
                                            visionResultText = "Analysis error: ${e.localizedMessage}"
                                        } finally {
                                            isAnalyzing = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Analyze", fontSize = 12.sp)
                            }

                            // 2. Auto-Tag Button
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isTaggingPhoto = true
                                        // Delete cached tag to re-classify
                                        File(currentPhoto.parentFile, currentPhoto.nameWithoutExtension + ".tag").delete()
                                        val newTag = autoClassifyPhoto(context, currentPhoto, aiRepo, userPrefs)
                                        withContext(Dispatchers.Main) {
                                            currentPhotoTag = newTag
                                            isTaggingPhoto = false
                                            Toast.makeText(context, "Tagged as: $newTag", Toast.LENGTH_SHORT).show()
                                            refreshCapturedPhotos()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2DD4BF)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto-Tag", fontSize = 12.sp)
                            }

                            // 3. Share Button
                            OutlinedButton(
                                onClick = {
                                    shareCapturedPhoto(context, currentPhoto)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Local Captured Photo Gallery View Modal Sheet
    if (showLocalGallerySheet) {
        Dialog(
            onDismissRequest = { showLocalGallerySheet = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0E17)),
                color = Color(0xFF0F0E17)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Captured Photo Gallery",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${capturedPhotosList.size} photos saved locally • Auto-tagged by AI",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        Toast.makeText(context, "Auto-tagging all untagged photos...", Toast.LENGTH_SHORT).show()
                                        capturedPhotosList.forEach { file ->
                                            if (getPhotoTag(file).isBlank()) {
                                                autoClassifyPhoto(context, file, aiRepo, userPrefs)
                                            }
                                        }
                                        refreshCapturedPhotos()
                                        Toast.makeText(context, "Finished AI auto-tagging!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Sell, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto-Tag All", fontSize = 11.sp)
                            }

                            IconButton(
                                onClick = { showLocalGallerySheet = false },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF27272A))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }

                    // Category Filter Chips Row
                    val categories = listOf("All", "Nature", "Document", "Person", "Food", "Pet", "Text", "Receipt", "Other")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) Color(0xFF8B5CF6) else Color(0xFF27272A))
                                    .border(1.dp, if (isSelected) Color(0xFFA78BFA) else Color.Transparent, RoundedCornerShape(16.dp))
                                    .clickable { selectedCategoryFilter = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (cat == "All") "All Photos" else "🏷️ $cat",
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    val filteredPhotos = remember(capturedPhotosList, selectedCategoryFilter) {
                        if (selectedCategoryFilter == "All") {
                            capturedPhotosList
                        } else {
                            capturedPhotosList.filter { file ->
                                val tag = getPhotoTag(file)
                                tag.contains(selectedCategoryFilter, ignoreCase = true)
                            }
                        }
                    }

                    if (filteredPhotos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Camera,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (selectedCategoryFilter == "All") "No captured photos yet" else "No photos under category '$selectedCategoryFilter'",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Take photos or tap 'Auto-Tag All' to categorize existing photos!",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredPhotos, key = { it.absolutePath }) { photoFile ->
                                var thumbnailBitmap by remember(photoFile.absolutePath) { mutableStateOf<Bitmap?>(null) }
                                val photoTag = remember(photoFile.absolutePath, capturedPhotosList) { getPhotoTag(photoFile) }

                                LaunchedEffect(photoFile.absolutePath) {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            val options = BitmapFactory.Options().apply {
                                                inSampleSize = 4 // downsample for performance
                                            }
                                            thumbnailBitmap = BitmapFactory.decodeFile(photoFile.absolutePath, options)
                                        } catch (e: Exception) {
                                            Log.e("CameraVision", "Failed to load thumbnail", e)
                                        }
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                        .combinedClickable(
                                            onClick = {
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    val fullBmp = BitmapFactory.decodeFile(photoFile.absolutePath)
                                                    withContext(Dispatchers.Main) {
                                                        lastCapturedFile = photoFile
                                                        previewBitmap = fullBmp
                                                        showPhotoPreviewDialog = true
                                                    }
                                                }
                                            },
                                            onLongClick = {
                                                photoToDelete = photoFile
                                                showDeleteConfirmDialog = true
                                            }
                                        ),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B2E))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        if (thumbnailBitmap != null) {
                                            Image(
                                                bitmap = thumbnailBitmap!!.asImageBitmap(),
                                                contentDescription = photoFile.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp).align(Alignment.Center),
                                                color = Color(0xFF8B5CF6),
                                                strokeWidth = 2.dp
                                            )
                                        }

                                        // Category Tag Badge at Top Left
                                        if (photoTag.isNotBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(4.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF8B5CF6).copy(alpha = 0.85f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "🏷️ $photoTag",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        // Delete Icon Overlay at Top Right
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.6f))
                                                .clickable {
                                                    photoToDelete = photoFile
                                                    showDeleteConfirmDialog = true
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete photo",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        // File Size Badge at Bottom Left
                                        val sizeKb = photoFile.length() / 1024
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Black.copy(alpha = 0.7f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (sizeKb > 1024) "${sizeKb / 1024}MB" else "${sizeKb}KB",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog Modal
    if (showDeleteConfirmDialog && photoToDelete != null) {
        val target = photoToDelete!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Photo?", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete '${target.name}' from local storage?",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deleted = target.delete()
                        showDeleteConfirmDialog = false
                        if (deleted) {
                            Toast.makeText(context, "Photo deleted from local storage", Toast.LENGTH_SHORT).show()
                            if (lastCapturedFile?.absolutePath == target.absolutePath) {
                                lastCapturedFile = null
                                previewBitmap = null
                                showPhotoPreviewDialog = false
                            }
                            refreshCapturedPhotos()
                        } else {
                            Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1B2E),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

private fun loadCapturedPhotosList(context: Context): List<File> {
    val resultList = mutableListOf<File>()

    val externalDir = context.getExternalFilesDir("captured_photos")
    if (externalDir != null && externalDir.exists()) {
        externalDir.listFiles()?.filter { it.isFile && (it.extension.equals("jpg", true) || it.extension.equals("jpeg", true) || it.extension.equals("png", true)) }?.let {
            resultList.addAll(it)
        }
    }

    val internalDir = File(context.filesDir, "captured_photos")
    if (internalDir.exists()) {
        internalDir.listFiles()?.filter { it.isFile && (it.extension.equals("jpg", true) || it.extension.equals("jpeg", true) || it.extension.equals("png", true)) }?.let {
            resultList.addAll(it)
        }
    }

    return resultList.distinctBy { it.absolutePath }.sortedByDescending { it.lastModified() }
}

private fun shareCapturedPhoto(context: Context, photoFile: File) {
    try {
        val contentUri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
        } catch (e: Exception) {
            savePhotoToSystemGallery(context, photoFile) ?: Uri.fromFile(photoFile)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Photo via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to share photo: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

private fun savePhotoToSystemGallery(context: Context, photoFile: File): Uri? {
    return try {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Snaper_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/SnaperAI")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                photoFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        }
        uri
    } catch (e: Exception) {
        Log.e("CameraVision", "Failed to save photo to MediaStore", e)
        null
    }
}

fun getPhotoTag(photoFile: File): String {
    return try {
        val tagFile = File(photoFile.parentFile, photoFile.nameWithoutExtension + ".tag")
        if (tagFile.exists()) tagFile.readText().trim() else ""
    } catch (e: Exception) {
        ""
    }
}

fun savePhotoTag(photoFile: File, tag: String) {
    try {
        val tagFile = File(photoFile.parentFile, photoFile.nameWithoutExtension + ".tag")
        tagFile.writeText(tag.trim())
    } catch (e: Exception) {
        Log.e("CameraVision", "Error saving photo tag", e)
    }
}

suspend fun autoClassifyPhoto(
    context: Context,
    photoFile: File,
    aiRepo: AiRepository,
    userPrefs: UserPreferencesRepository
): String = withContext(Dispatchers.IO) {
    val existingTag = getPhotoTag(photoFile)
    if (existingTag.isNotBlank()) return@withContext existingTag

    return@withContext try {
        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath) ?: return@withContext "Other"
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 65, outputStream)
        val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        val settings = userPrefs.userSettingsFlow.firstOrNull() ?: UserSettings()
        val prompt = "Categorize this photo into 1 or 2 category tags from: Nature, Document, Person, Food, Pet, Product, Architecture, Vehicle, Text, Receipt, Landscape, Other. Return ONLY the category tag names separated by commas (e.g. 'Nature, Landscape' or 'Document')."

        val response = aiRepo.generateAssistantResponse(
            prompt = prompt,
            history = emptyList(),
            memories = emptyList(),
            userSettings = settings,
            attachedImageBase64 = base64Image
        )

        val cleanTag = response.lines().firstOrNull()?.replace(".", "")?.trim()?.ifBlank { "Other" } ?: "Other"
        savePhotoTag(photoFile, cleanTag)
        cleanTag
    } catch (e: Exception) {
        "Other"
    }
}
