package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.glass.LocalGlassAccent

object AppLogoConstants {
    const val DRIVE_VIEW_URL = "https://drive.google.com/file/d/1ZAw0JiWqbLa2M9qnfO8lb_tlWhpqnOgB/view?usp=drivesdk"
    const val DIRECT_LOGO_URL = "https://lh3.googleusercontent.com/d/1ZAw0JiWqbLa2M9qnfO8lb_tlWhpqnOgB"
    const val DIRECT_UC_URL = "https://drive.google.com/uc?export=view&id=1ZAw0JiWqbLa2M9qnfO8lb_tlWhpqnOgB"
}

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    shape: Shape = CircleShape,
    borderWidth: Dp = 2.dp,
    borderColor: Color? = null,
    showGlow: Boolean = true
) {
    val context = LocalContext.current
    val accentColor = LocalGlassAccent.current.color
    val activeBorderColor = borderColor ?: accentColor

    Surface(
        shape = shape,
        color = Color(0xFF1E1B2E),
        shadowElevation = if (showGlow) 12.dp else 4.dp,
        modifier = modifier
            .size(size)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, activeBorderColor, shape)
                } else Modifier
            )
            .clip(shape)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(AppLogoConstants.DIRECT_LOGO_URL)
                    .crossfade(true)
                    .build(),
                contentDescription = "Snaper Official Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1E1B2E))
                    ) {
                        CircularProgressIndicator(
                            color = accentColor,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(size / 3)
                        )
                    }
                },
                error = {
                    Image(
                        painter = painterResource(id = R.drawable.snaper_official_logo),
                        contentDescription = "Snaper Official Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        }
    }
}
