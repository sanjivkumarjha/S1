package com.example.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.ui.preview.DeviceDetectionManager
import com.example.ui.preview.PremiumDevicePreviewFrame
import com.example.ui.preview.PreviewDeviceType
import com.example.ui.screens.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun DevicePreviewSettingsCard(
    userSettings: UserSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }

    val activeDevice = DeviceDetectionManager.getEffectivePreviewDevice(userSettings.selectedPreviewDevice)

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color(0x258B5CF6),
        borderColor = Color(0x508B5CF6)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Flagship Device Preview & Auto-Detection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Real-time live device rendering based on detected OS platform",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Device Selection Selector Pills
            Text(
                text = "Reference Frame Device",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "AUTO" to "Auto (${if (DeviceDetectionManager.detectOperatingSystem() == "IOS") "iPhone 17" else "S26 Ultra"})",
                    "SAMSUNG_S26_ULTRA" to "S26 Ultra",
                    "IPHONE_17_PRO_MAX" to "iPhone 17 Pro",
                    "PIXEL_9_PRO" to "Pixel 9 Pro"
                ).forEach { (devId, label) ->
                    val isSelected = userSettings.selectedPreviewDevice == devId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color(0x20FFFFFF),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color(0x30FFFFFF),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                scope.launch {
                                    prefsRepo.updateSelectedPreviewDevice(devId)
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Live Interactive Preview Inside Flagship Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                PremiumDevicePreviewFrame(
                    deviceType = activeDevice,
                    frameWidth = 230.dp,
                    showDeviceBadge = true
                ) {
                    // Render real splash / avatar app state inside device preview
                    SplashScreen(
                        userSettings = userSettings,
                        onSplashFinished = {}
                    )
                }
            }
        }
    }
}
