package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.local.entities.ChatMessageEntity
import com.example.security.SecureDeviceAuthManager
import com.example.ui.components.MarkdownCodeView
import com.example.ui.components.TypingAnimatedText
import com.example.ui.glass.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessageEntity>,
    isLoading: Boolean,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    modifier: Modifier = Modifier,
    selectedImageBase64: String? = null,
    onAttachImageClick: (() -> Unit)? = null,
    onRemoveImageClick: (() -> Unit)? = null,
    onVoiceInputClick: (() -> Unit)? = null,
    onSpeakMessage: ((String) -> Unit)? = null,
    onBookmarkMessage: ((String) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    assistantName: String = "Snaper AI",
    aiModelInfo: String = "Liquid Glass Multi-Model Engine",
    placeholderHint: String = "Ask anything..."
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    DynamicLiquidGlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = modifier
                .fillMaxSize()
                .testTag("chat_screen"),
            topBar = {
                GlassTopBar(
                    title = assistantName,
                    subtitle = aiModelInfo,
                    navigationIcon = if (onNavigateBack != null) {
                        {
                            GlassIconButton(
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                onClick = onNavigateBack
                            )
                        }
                    } else null
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            // Preview attached image
                            selectedImageBase64?.let { base64 ->
                                val imageBitmap = remember(base64) {
                                    try {
                                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                imageBitmap?.let { bmp ->
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 12.dp, bottom = 6.dp)
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.Black.copy(alpha = 0.5f))
                                    ) {
                                        Image(
                                            bitmap = bmp,
                                            contentDescription = "Preview",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        if (onRemoveImageClick != null) {
                                            IconButton(
                                                onClick = onRemoveImageClick,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (onAttachImageClick != null) {
                                    GlassIconButton(
                                        icon = Icons.Default.Image,
                                        contentDescription = "Attach Image",
                                        onClick = onAttachImageClick
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                if (onVoiceInputClick != null) {
                                    GlassIconButton(
                                        icon = Icons.Default.Mic,
                                        contentDescription = "Voice Input",
                                        onClick = onVoiceInputClick
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                GlassTextField(
                                    value = inputText,
                                    onValueChange = onInputTextChange,
                                    placeholder = placeholderHint,
                                    modifier = Modifier.weight(1f),
                                    testTag = "chat_input_field"
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                GlassIconButton(
                                    icon = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    onClick = onSendMessage,
                                    accentColor = LocalGlassAccent.current.color
                                )
                            }
                        }
                    }

                    // MANDATORY Official Company Branding
                    GlassFooter()
                }
            }
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(messages, key = { it.id }) { msg ->
                    val isUser = msg.sender == "USER"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        GlassSurface(
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (isUser) 18.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 18.dp
                            ),
                            modifier = Modifier.widthIn(max = 310.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (!msg.mediaUri.isNullOrBlank() && msg.mediaType == "image") {
                                    val bitmap = remember(msg.mediaUri) {
                                        try {
                                            val bytes = Base64.decode(msg.mediaUri, Base64.DEFAULT)
                                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                    bitmap?.let { bmp ->
                                        Image(
                                            bitmap = bmp,
                                            contentDescription = "Attached Image",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }

                                if (!msg.codeSnippet.isNullOrBlank()) {
                                    MarkdownCodeView(text = msg.codeSnippet)
                                } else if (isUser) {
                                    Text(
                                        text = msg.content,
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    TypingAnimatedText(
                                        text = msg.content,
                                        isAnimated = true,
                                        isMarkdown = true,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )

                                    if (msg.content.contains("authentication", ignoreCase = true) ||
                                        msg.content.contains("fingerprint", ignoreCase = true) ||
                                        msg.content.contains("dialog", ignoreCase = true) ||
                                        msg.content.contains("अनलॉक", ignoreCase = true)
                                    ) {
                                        val currentContext = LocalContext.current
                                        Spacer(modifier = Modifier.height(8.dp))
                                        GlassButton(
                                            text = "Scan Fingerprint / Face / PIN",
                                            onClick = {
                                                val activity = currentContext as? FragmentActivity
                                                if (activity != null) {
                                                    SecureDeviceAuthManager(currentContext).authenticateOwner(
                                                        activity = activity,
                                                        onSuccess = { },
                                                        onError = { }
                                                    )
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!isUser && onSpeakMessage != null) {
                                        IconButton(
                                            onClick = { onSpeakMessage(msg.content) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VolumeUp,
                                                contentDescription = "Speak",
                                                tint = LocalGlassAccent.current.color,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    if (onBookmarkMessage != null) {
                                        IconButton(
                                            onClick = { onBookmarkMessage(msg.content) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = "Bookmark",
                                                tint = LocalGlassAccent.current.color,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            GlassSurface(shape = RoundedCornerShape(18.dp)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = LocalGlassAccent.current.color
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "$assistantName is thinking...",
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}
