package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.glass.*

import com.example.ui.components.AppLogo

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    DynamicLiquidGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GlassTopBar(
                title = "About Snaper Tech",
                subtitle = "Official Company Profile",
                navigationIcon = {
                    GlassIconButton(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        onClick = onNavigateBack
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppLogo(size = 110.dp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SNAPER TECHNOLOGY",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "राधे राधे ✨",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LocalGlassAccent.current.color
            )

            Text(
                text = "Version 2.0.0 • Liquid Glass Intelligence Upgrade",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Official Identity & Architecture",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = LocalGlassAccent.current.color
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Engineered with Jetpack Compose Liquid Glass System, Multi-Model Intelligence Router (100+ AI Providers), Global Live Seconds Clock, Memory Import SAF Engine, and On-Device Privacy Architecture.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // MANDATORY Official Company Branding
            GlassFooter()
        }
    }
}
