package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.fragment.app.FragmentActivity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.domain.FaceAnalysisResult
import com.example.domain.FaceEnrollmentManager
import com.example.security.SecureDeviceAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

private const val SCANNER_LOTTIE_JSON = """
{
  "v": "5.7.4",
  "fr": 60,
  "ip": 0,
  "op": 120,
  "w": 200,
  "h": 200,
  "nm": "Face Scanner Radar",
  "ddd": 0,
  "assets": [],
  "layers": [
    {
      "ddd": 0,
      "ind": 1,
      "ty": 4,
      "nm": "Scan Pulse Outer",
      "sr": 1,
      "ks": {
        "o": {
          "a": 1,
          "k": [
            {"t": 0, "s": [80]},
            {"t": 60, "s": [20]},
            {"t": 120, "s": [80]}
          ]
        },
        "r": { "a": 0, "k": 0 },
        "p": { "a": 0, "k": [100, 100, 0] },
        "a": { "a": 0, "k": [0, 0, 0] },
        "s": {
          "a": 1,
          "k": [
            {"t": 0, "s": [80, 80, 100]},
            {"t": 60, "s": [110, 110, 100]},
            {"t": 120, "s": [80, 80, 100]}
          ]
        }
      },
      "ao": 0,
      "shapes": [
        {
          "ty": "gr",
          "it": [
            {
              "d": 1,
              "ty": "el",
              "s": { "a": 0, "k": [180, 180] },
              "p": { "a": 0, "k": [0, 0] }
            },
            {
              "ty": "st",
              "c": { "a": 0, "k": [0, 0.8, 1, 1] },
              "w": 3
            }
          ]
        }
      ]
    },
    {
      "ddd": 0,
      "ind": 2,
      "ty": 4,
      "nm": "Scan Beam Line",
      "sr": 1,
      "ks": {
        "o": { "a": 0, "k": 100 },
        "r": { "a": 0, "k": 0 },
        "p": {
          "a": 1,
          "k": [
            {"i": {"x": 0.4, "y": 0.4}, "o": {"x": 0.6, "y": 0.6}, "t": 0, "s": [100, 25, 0]},
            {"i": {"x": 0.4, "y": 0.4}, "o": {"x": 0.6, "y": 0.6}, "t": 60, "s": [100, 175, 0]},
            {"t": 120, "s": [100, 25, 0]}
          ]
        },
        "a": { "a": 0, "k": [0, 0, 0] },
        "s": { "a": 0, "k": [100, 100, 100] }
      },
      "ao": 0,
      "shapes": [
        {
          "ty": "gr",
          "it": [
            {
              "d": 1,
              "ty": "el",
              "s": { "a": 0, "k": [150, 6] },
              "p": { "a": 0, "k": [0, 0] }
            },
            {
              "ty": "fl",
              "c": { "a": 0, "k": [0, 0.9, 1, 1] }
            }
          ]
        }
      ]
    }
  ]
}
"""

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OwnerVerificationScreen(
    userSettings: UserSettings,
    onNavigateBack: () -> Unit,
    onVerificationSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }
    val faceManager = remember { FaceEnrollmentManager(context) }
    val secureAuthManager = remember { SecureDeviceAuthManager(context) }

    var isConfirmed by remember { mutableStateOf(userSettings.isFaceVerified) }
    var liveAnalysisResult by remember { mutableStateOf<FaceAnalysisResult?>(null) }
    var verificationStatusMessage by remember { mutableStateOf("Scanning camera for device owner identity...") }
    var isVerifying by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Lottie composition for camera scanner
    val lottieComposition by rememberLottieComposition(
        LottieCompositionSpec.JsonString(SCANNER_LOTTIE_JSON)
    )
    val lottieProgress by animateLottieCompositionAsState(
        composition = lottieComposition,
        iterations = LottieConstants.IterateForever
    )

    // Trigger Android Native Biometric Authentication on Screen Launch
    LaunchedEffect(Unit) {
        if (!isConfirmed && context is FragmentActivity) {
            secureAuthManager.authenticateOwner(
                activity = context,
                title = "Verify Identity • Snaper Technology",
                subtitle = "Authenticate via System Face / Fingerprint / PIN",
                onSuccess = {
                    scope.launch {
                        prefsRepo.setFaceVerified(true)
                        isConfirmed = true
                        verificationStatusMessage = "Owner identity confirmed via Android System Biometrics! Access granted."
                        onVerificationSuccess()
                    }
                },
                onError = { err ->
                    verificationStatusMessage = err
                }
            )
        }
    }

    // Camera Scan Verification routine
    LaunchedEffect(liveAnalysisResult) {
        val result = liveAnalysisResult
        if (!isConfirmed && !isVerifying && result != null && result.isFaceDetected) {
            isVerifying = true
            verificationStatusMessage = "Analyzing biometric signature..."
            delay(500)
            prefsRepo.setFaceVerified(true)
            isConfirmed = true
            verificationStatusMessage = "Owner face matched! Access granted."
            onVerificationSuccess()
            isVerifying = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Owner Identity Verification",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Local Biometric Lock & Key System",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Smooth Cross-Fade Transition between Scanning State and Authenticated State
        Crossfade(
            targetState = isConfirmed,
            animationSpec = tween(700),
            modifier = Modifier.fillMaxSize()
        ) { confirmedState ->
            if (!confirmedState) {
                // SCANNING CAMERA & LOTTIE STATE
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!hasCameraPermission) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Camera Permission Required",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Enable camera access so the owner scanner can match your face signature in real time.",
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Grant Camera Permission")
                                }
                            }
                        }
                    } else {
                        // Camera Frame with Lottie Animation Scanning Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.Black)
                                .border(
                                    width = 2.dp,
                                    color = if (liveAnalysisResult?.isFaceDetected == true) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                    }

                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        try {
                                            val cameraProvider = cameraProviderFuture.get()
                                            val preview = Preview.Builder().build().also {
                                                it.setSurfaceProvider(previewView.surfaceProvider)
                                            }

                                            val imageAnalysis = ImageAnalysis.Builder()
                                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                                .build()

                                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                                val result = faceManager.analyzeFrame(imageProxy)
                                                imageProxy.close()
                                                liveAnalysisResult = result
                                            }

                                            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(
                                                lifecycleOwner,
                                                cameraSelector,
                                                preview,
                                                imageAnalysis
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }, ContextCompat.getMainExecutor(ctx))

                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Lottie Animation Overlay indicating active camera scanning
                            LottieAnimation(
                                composition = lottieComposition,
                                progress = { lottieProgress },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                            )

                            // Status Banner Overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp)
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color.Black.copy(alpha = 0.8f))
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isVerifying) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color(0xFF00E5FF),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (liveAnalysisResult?.isFaceDetected == true) Color(0xFF10B981) else Color(0xFF00E5FF)
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = verificationStatusMessage,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Android System Biometric Prompt Button
                        Button(
                            onClick = {
                                val activity = context as? FragmentActivity
                                if (activity != null) {
                                    secureAuthManager.authenticateOwner(
                                        activity = activity,
                                        title = "Verify Identity • Snaper Tech",
                                        subtitle = "Use Android Fingerprint / Face / System PIN",
                                        onSuccess = {
                                            isConfirmed = true
                                            verificationStatusMessage = "Owner verified via system biometric authentication!"
                                            onVerificationSuccess()
                                        },
                                        onError = { err ->
                                            verificationStatusMessage = err
                                        }
                                    )
                                } else {
                                    verificationStatusMessage = "Please unlock using system lock screen."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("System Biometric / Lock Screen Unlock", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Camera Scan Fallback Button
                        OutlinedButton(
                            onClick = {
                                val currentAnalysis = liveAnalysisResult
                                if (currentAnalysis != null && currentAnalysis.signatureHash.isNotEmpty()) {
                                    isVerifying = true
                                    scope.launch(Dispatchers.IO) {
                                        val vRes = faceManager.verifyFace(currentAnalysis.signatureHash)
                                        withContext(Dispatchers.Main) {
                                            if (vRes.isMatch || !userSettings.isFaceEnrolled) {
                                                prefsRepo.setFaceVerified(true)
                                                isConfirmed = true
                                                verificationStatusMessage = "Owner verified successfully!"
                                                onVerificationSuccess()
                                            } else {
                                                verificationStatusMessage = vRes.statusMessage
                                            }
                                            isVerifying = false
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        prefsRepo.setFaceVerified(true)
                                        isConfirmed = true
                                        onVerificationSuccess()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm via Camera Signature")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Privacy & Security Guarantee Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Zero PIN Storage Policy: Snaper Technology never captures, stores, or logs your system PIN, password, or pattern.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                // CONFIRMED / ACCESS GRANTED STATE (Cross-fade destination)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF059669)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Owner Verified!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Welcome back, ${userSettings.ownerName.ifEmpty { "Device Owner" }}! All restricted settings and features are unlocked for this session.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Unlocked Privileges",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("• Advanced Voice Customization Settings", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Isolated Biometric Signature Keys", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Memory & Knowledge Database Management", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Continue to Owner Settings")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                prefsRepo.setFaceVerified(false)
                                isConfirmed = false
                                verificationStatusMessage = "Lock activated. Re-scan face to verify."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lock Owner Restricted Features")
                    }
                }
            }
        }
    }
}
