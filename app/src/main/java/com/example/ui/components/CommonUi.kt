package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Note
import com.example.ui.stylus.StylusDrawingThumbnail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(note.updatedAt) {
        val sdf = SimpleDateFormat("d MMM, HH:mm", Locale("es", "ES"))
        sdf.format(Date(note.updatedAt))
    }

    val cardColor = Color(note.colorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (note.isPinned) 1.5.dp else 1.dp,
                color = if (note.isPinned) Color(0xFF00E5FF) else Color(0x33FFFFFF),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("note_card_${note.id}"),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Pinned status, Title, Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Fijada",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (note.title.isNotBlank()) note.title else "Nota sin título",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(28.dp).testTag("note_pin_button_${note.id}")
                    ) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Fijar",
                            tint = if (note.isPinned) Color(0xFF00E5FF) else Color(0xFF8E92A8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(28.dp).testTag("note_fav_button_${note.id}")
                    ) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (note.isFavorite) Color(0xFFFF4081) else Color(0xFF8E92A8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Stylus drawing preview if exists
            if (note.drawingStrokesJson.isNotBlank()) {
                StylusDrawingThumbnail(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .padding(vertical = 4.dp),
                    strokesJson = note.drawingStrokesJson
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Note text content snippet
            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD4D7E8),
                    maxLines = if (note.drawingStrokesJson.isNotBlank()) 2 else 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // AI Summary badge preview
            if (note.aiSummary.isNotBlank()) {
                Surface(
                    color = Color(0x337C4DFF),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x667C4DFF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Resumen IA",
                            tint = Color(0xFFB388FF),
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = note.aiSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE0D4FF),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 11.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Footer: Badges (Voice, Stylus, AI) & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (note.hasVoiceRecording) {
                        BadgePill(
                            icon = Icons.Default.Mic,
                            text = "Voz",
                            color = Color(0xFFFF5252)
                        )
                    }
                    if (note.drawingStrokesJson.isNotBlank()) {
                        BadgePill(
                            icon = Icons.Default.Create,
                            text = "Lápiz",
                            color = Color(0xFF00E5FF)
                        )
                    }
                    if (note.aiSummary.isNotBlank()) {
                        BadgePill(
                            icon = Icons.Default.AutoAwesome,
                            text = "IA Resumida",
                            color = Color(0xFFFFD600)
                        )
                    }
                }

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8E92A8),
                    fontSize = 10.5.sp
                )
            }
        }
    }
}

@Composable
fun BadgePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    isRecording: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animFactor by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveAnim"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barCount = 14
        for (i in 0 until barCount) {
            val offsetVariance = (i % 4) * 0.2f
            val heightFraction = if (isRecording) {
                ((rmsLevel / 10f) * 0.7f + animFactor * 0.3f + offsetVariance * 0.2f).coerceIn(0.15f, 1f)
            } else {
                0.15f
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(3.5.dp)
                    .height((24.dp * heightFraction))
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isRecording) {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF5252), Color(0xFFFF8A80))
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF5A5E78), Color(0xFF383B52))
                            )
                        }
                    )
            )
        }
    }
}

@Composable
fun QuickCaptureModal(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSaveAndSummarize: (String, String) -> Unit,
    onStartVoiceDictation: () -> Unit,
    onStopVoiceDictation: () -> Unit,
    isListening: Boolean,
    partialVoiceText: String,
    rmsLevel: Float
) {
    if (!visible) return

    var noteContent by remember { mutableStateOf("") }
    var noteTitle by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, Color(0xFF00E5FF), RoundedCornerShape(24.dp)),
            color = Color(0xFF141726)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Captura Rápida",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Captura Rápida Lockscreen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Atajo: 3 toques con 2 dedos activado",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF00E5FF)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("quick_capture_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF9E9E9E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    placeholder = { Text("Título rápido (opcional)", color = Color(0xFF6C718B)) },
                    modifier = Modifier.fillMaxWidth().testTag("quick_capture_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color(0xFF2E3349)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Content Input (Text + Voice transcript)
                OutlinedTextField(
                    value = if (partialVoiceText.isNotBlank()) "$noteContent $partialVoiceText" else noteContent,
                    onValueChange = { noteContent = it },
                    placeholder = { Text("Escribe o mantén presionado el micrófono para dictar...", color = Color(0xFF6C718B)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("quick_capture_content_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF7C4DFF),
                        unfocusedBorderColor = Color(0xFF2E3349)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Waveform visualizer if listening
                if (isListening) {
                    AudioWaveformVisualizer(
                        isRecording = true,
                        rmsLevel = rmsLevel,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Action Bar: Voice Dictation & AI Summarize & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice button
                    Button(
                        onClick = {
                            if (isListening) onStopVoiceDictation() else onStartVoiceDictation()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListening) Color(0xFFFF5252) else Color(0xFF25293E)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("quick_capture_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Dictar",
                            tint = if (isListening) Color.White else Color(0xFFFF5252),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isListening) "Detener" else "Dictar",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    // AI Summarize & Save
                    Button(
                        onClick = {
                            val finalContent = if (partialVoiceText.isNotBlank()) "$noteContent $partialVoiceText" else noteContent
                            onSaveAndSummarize(noteTitle, finalContent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C4DFF)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("quick_capture_save_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Resumir y Guardar",
                            tint = Color(0xFFFFD600),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "IA Resumir y Guardar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
