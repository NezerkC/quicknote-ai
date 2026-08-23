package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.QuickCaptureModal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LockscreenSimulatorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val gestureState by viewModel.gestureTracker.gestureState.collectAsStateWithLifecycle()
    val isQuickCaptureVisible by viewModel.isQuickCaptureVisible.collectAsStateWithLifecycle()

    val isListening by viewModel.voiceSpeechManager.isListening.collectAsStateWithLifecycle()
    val partialVoiceText by viewModel.voiceSpeechManager.partialResult.collectAsStateWithLifecycle()
    val rmsLevel by viewModel.voiceSpeechManager.rmsDb.collectAsStateWithLifecycle()

    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
        val now = Date()
        currentTimeString = timeFormat.format(now)
        currentDateString = dateFormat.format(now).replaceFirstChar { it.uppercase() }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ambientGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06070D))
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        viewModel.gestureTracker.processPointerEvent(event) {
                            viewModel.triggerQuickCaptureFromLockscreen()
                        }
                    }
                }
            }
            .testTag("lockscreen_touch_surface")
    ) {
        // Ambient background glow circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF7C4DFF).copy(alpha = 0.15f * ambientGlow), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.3f),
                    radius = size.width * 0.6f
                ),
                center = Offset(size.width * 0.5f, size.height * 0.3f),
                radius = size.width * 0.6f
            )

            // Draw active touch pointer circles (fingers currently on screen)
            gestureState.activePointers.forEachIndexed { index, pointer ->
                val pointerColor = if (index == 0) Color(0xFF00E5FF) else Color(0xFFFF4081)
                drawCircle(
                    color = pointerColor.copy(alpha = 0.35f),
                    radius = 48.dp.toPx(),
                    center = pointer.position
                )
                drawCircle(
                    color = pointerColor,
                    radius = 24.dp.toPx(),
                    center = pointer.position,
                    style = Stroke(width = 3.dp.toPx())
                )
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = pointer.position
                )
            }
        }

        // Top Status Bar (Lock icon, Battery, Exit button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier
                    .background(Color(0x33FFFFFF), CircleShape)
                    .size(38.dp)
                    .testTag("lockscreen_exit_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Salir de pantalla de bloqueo",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Bloqueado",
                    tint = Color(0xFF8E92A8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Dispositivo Protegido",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E92A8)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = "Batería",
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "98%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }

        // Center Content: Glowing Clock, Date, and Gesture Tracker Visualizer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Digital Clock
            Text(
                text = currentTimeString.ifBlank { "21:55" },
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )

            // Date
            Text(
                text = currentDateString.ifBlank { "Sábado, 22 de Agosto" },
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFB0B3C7)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 2-Finger 3-Tap Gesture Indicator Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFF00E5FF).copy(alpha = 0.8f), Color(0xFF7C4DFF).copy(alpha = 0.8f))
                        ),
                        RoundedCornerShape(24.dp)
                    ),
                color = Color(0xFF101322).copy(alpha = 0.92f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Acceso Rápido Bloqueado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Da 3 toques rápidos con 2 dedos en la pantalla para abrir tus Notas al instante.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD0D3E5),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Tap Counter Dots (1, 2, 3)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (step in 1..3) {
                            val isCompleted = gestureState.tapCount >= step
                            val isCurrent = gestureState.tapCount == step - 1

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCompleted) Color(0xFF00E5FF)
                                            else if (isCurrent) Color(0xFF7C4DFF).copy(alpha = 0.4f)
                                            else Color(0xFF1E2235)
                                        )
                                        .border(
                                            width = if (isCompleted || isCurrent) 2.dp else 1.dp,
                                            color = if (isCompleted) Color(0xFF00E5FF) else if (isCurrent) Color(0xFF7C4DFF) else Color(0x33FFFFFF),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$step",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) Color(0xFF0F111D) else Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Toque $step",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (isCompleted) Color(0xFF00E5FF) else Color(0xFF8E92A8)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = gestureState.statusMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (gestureState.tapCount > 0) Color(0xFF00E5FF) else Color(0xFF8E92A8),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom Simulation & Test Action Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // One-click Simulator button for mouse/single touch testing
            Button(
                onClick = {
                    viewModel.triggerQuickCaptureFromLockscreen()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C4DFF)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simulate_3taps_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFFFD600),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Probar Atajo Instantáneo (3 toques 2 dedos)",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Toca la pantalla con 2 dedos 3 veces seguidas o presiona el botón",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF6E738C)
            )
        }

        // Quick Capture Dialog Modal (Pops up directly from locked screen)
        QuickCaptureModal(
            visible = isQuickCaptureVisible,
            onDismiss = { viewModel.dismissQuickCapture() },
            onSaveAndSummarize = { title, content ->
                viewModel.updateCurrentNoteContent(title = title, content = content)
                viewModel.dismissQuickCapture()
                viewModel.autoSummarizeCurrentNote()
                viewModel.navigateTo(AppScreen.NOTE_EDITOR)
            },
            onStartVoiceDictation = { viewModel.startVoiceDictation() },
            onStopVoiceDictation = { viewModel.stopVoiceDictation() },
            isListening = isListening,
            partialVoiceText = partialVoiceText,
            rmsLevel = rmsLevel
        )
    }
}
