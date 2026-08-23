package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.NoteTab
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.stylus.StylusDrawingCanvas

val NOTE_BACKGROUND_COLORS = listOf(
    0xFF1E2235, // Default Dark Indigo
    0xFF2A1C38, // Deep Purple
    0xFF1C2D37, // Teal Dark
    0xFF362020, // Maroon Crimson
    0xFF1B2C24, // Forest Dark
    0xFF2D2A18  // Amber Dark
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentNote by viewModel.currentNote.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeEditorTab.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val aiStatusMessage by viewModel.aiStatusMessage.collectAsStateWithLifecycle()

    val isListening by viewModel.voiceSpeechManager.isListening.collectAsStateWithLifecycle()
    val partialVoiceText by viewModel.voiceSpeechManager.partialResult.collectAsStateWithLifecycle()
    val rmsLevel by viewModel.voiceSpeechManager.rmsDb.collectAsStateWithLifecycle()
    val voiceError by viewModel.voiceSpeechManager.errorMessage.collectAsStateWithLifecycle()

    var showColorPalette by remember { mutableStateOf(false) }
    var askAiPrompt by remember { mutableStateOf("") }
    var aiAnswerText by remember { mutableStateOf("") }
    var isAskingAi by remember { mutableStateOf(false) }

    // Audio permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceDictation()
        }
    }

    val note = currentNote ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isListening) viewModel.stopVoiceDictation()
                            viewModel.navigateTo(AppScreen.HOME)
                        },
                        modifier = Modifier.testTag("note_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = if (note.title.isNotBlank()) note.title else "Editando Nota",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                },
                actions = {
                    // Color Palette Selector
                    IconButton(
                        onClick = { showColorPalette = !showColorPalette },
                        modifier = Modifier.testTag("note_color_palette_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Color de nota",
                            tint = Color(note.colorHex)
                        )
                    }

                    // Save Note (Checkmark Button)
                    Button(
                        onClick = {
                            if (isListening) viewModel.stopVoiceDictation()
                            viewModel.navigateTo(AppScreen.HOME)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 4.dp).testTag("note_save_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Guardar nota",
                            tint = Color(0xFF0F111D),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Listo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F111D)
                        )
                    }

                    // Auto-Summarize with Gemini AI Button
                    Button(
                        onClick = { viewModel.autoSummarizeCurrentNote() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C4DFF)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 4.dp).testTag("note_ai_summarize_button")
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Resumir con IA",
                                tint = Color(0xFFFFD600),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Resumir IA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Delete Note
                    IconButton(
                        onClick = { viewModel.deleteCurrentNote() },
                        modifier = Modifier.testTag("note_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFFF5252)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF131624)
                )
            )
        },
        containerColor = Color(note.colorHex),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Color selection dropdown row
            AnimatedVisibility(visible = showColorPalette) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF131624))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fondo:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9E9E9E)
                    )
                    NOTE_BACKGROUND_COLORS.forEach { colorVal ->
                        val isSelected = note.colorHex == colorVal
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color(0x44FFFFFF),
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.updateCurrentNoteContent(colorHex = colorVal)
                                }
                        )
                    }
                }
            }

            // AI Status notification pill
            AnimatedVisibility(visible = aiStatusMessage != null) {
                Surface(
                    color = Color(0xFF7C4DFF).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFFD600),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = aiStatusMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Tab navigation: Texto & Dictado, Lápiz Inteligente, Resumen IA
            SecondaryTabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = Color(0xFF161929),
                contentColor = Color(0xFF00E5FF)
            ) {
                Tab(
                    selected = activeTab == NoteTab.TEXT,
                    onClick = { viewModel.setEditorTab(NoteTab.TEXT) },
                    text = { Text("Texto & Dictado", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    selectedContentColor = Color(0xFF00E5FF),
                    unselectedContentColor = Color(0xFF8E92A8),
                    modifier = Modifier.testTag("tab_text_editor")
                )
                Tab(
                    selected = activeTab == NoteTab.STYLUS,
                    onClick = { viewModel.setEditorTab(NoteTab.STYLUS) },
                    text = { Text("Lápiz Inteligente", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    selectedContentColor = Color(0xFF00E5FF),
                    unselectedContentColor = Color(0xFF8E92A8),
                    modifier = Modifier.testTag("tab_stylus_editor")
                )
                Tab(
                    selected = activeTab == NoteTab.AI_SUMMARY,
                    onClick = { viewModel.setEditorTab(NoteTab.AI_SUMMARY) },
                    text = { Text("Agente IA", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    selectedContentColor = Color(0xFFFFD600),
                    unselectedContentColor = Color(0xFF8E92A8),
                    modifier = Modifier.testTag("tab_ai_summary")
                )
            }

            // Tab Content
            when (activeTab) {
                NoteTab.TEXT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Title Field
                        TextField(
                            value = note.title,
                            onValueChange = { viewModel.updateCurrentNoteContent(title = it) },
                            placeholder = { Text("Título de la nota...", fontSize = 20.sp, color = Color(0xFF6E738C)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color(0xFF7C4DFF),
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                            modifier = Modifier.fillMaxWidth().testTag("note_title_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Auto-saved indicator & AI Polish / Format Quick Button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Guardado automático",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF8E92A8),
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.polishCurrentNote() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2F45)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("note_polish_ai_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = "Pulir texto",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pulir con IA", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        // Content Field
                        TextField(
                            value = note.content,
                            onValueChange = { viewModel.updateCurrentNoteContent(content = it) },
                            placeholder = {
                                Text(
                                    "Escribe aquí o usa el botón de dictado por voz abajo para transcribir tus ideas rápidamente...",
                                    color = Color(0xFF6E738C)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .testTag("note_content_input")
                        )

                        // Voice Dictation Bar
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, if (isListening) Color(0xFFFF5252) else Color(0xFF2E3349), RoundedCornerShape(16.dp)),
                            color = Color(0xFF141724)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                if (isListening) {
                                    AudioWaveformVisualizer(
                                        isRecording = true,
                                        rmsLevel = rmsLevel,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                    if (partialVoiceText.isNotBlank()) {
                                        Text(
                                            text = "Escuchando: \"$partialVoiceText\"",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF00E5FF),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }
                                }

                                if (voiceError != null) {
                                    Text(
                                        text = voiceError ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFFF5252),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                                            contentDescription = null,
                                            tint = if (isListening) Color(0xFFFF5252) else Color(0xFF8E92A8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isListening) "Grabando dictado en vivo..." else "Dictado por voz",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isListening) Color(0xFFFF5252) else Color(0xFFD4D7E8)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (isListening) {
                                                viewModel.stopVoiceDictation()
                                            } else {
                                                val hasPermission = ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.RECORD_AUDIO
                                                ) == PackageManager.PERMISSION_GRANTED

                                                if (hasPermission) {
                                                    viewModel.startVoiceDictation()
                                                } else {
                                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isListening) Color(0xFFFF5252) else Color(0xFF7C4DFF)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("dictate_toggle_button")
                                    ) {
                                        Text(
                                            text = if (isListening) "Detener" else "Iniciar Dictado",
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                NoteTab.STYLUS -> {
                    // Stylus Drawing Canvas with Smart Pen Tools
                    StylusDrawingCanvas(
                        modifier = Modifier.fillMaxSize(),
                        initialStrokesJson = note.drawingStrokesJson,
                        onStrokesChanged = { updatedJson ->
                            viewModel.updateCurrentNoteDrawing(updatedJson)
                        }
                    )
                }

                NoteTab.AI_SUMMARY -> {
                    // AI Summary & Action Items Hub
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // AI Summary Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E30)),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD600),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Resumen Ejecutivo de IA",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.autoSummarizeCurrentNote() },
                                        modifier = Modifier.size(32.dp).testTag("refresh_ai_summary_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoFixHigh,
                                            contentDescription = "Regenerar resumen",
                                            tint = Color(0xFF00E5FF)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (note.aiSummary.isNotBlank()) {
                                    Text(
                                        text = note.aiSummary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFE2E4F0),
                                        lineHeight = 22.sp
                                    )
                                } else {
                                    Text(
                                        text = "Aún no se ha generado un resumen. Presiona 'Resumir con IA' para que el agente Gemini analice el contenido de texto y lápiz.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF8E92A8)
                                    )
                                }
                            }
                        }

                        // AI Action Items / Tasks Checklist
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E30)),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3349)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FormatListBulleted,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tareas & Acciones Detectadas",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val actionItems = remember(note.aiActionItems) {
                                    note.aiActionItems.lines().filter { it.isNotBlank() }
                                }

                                if (actionItems.isNotEmpty()) {
                                    actionItems.forEach { item ->
                                        var isChecked by remember { mutableStateOf(false) }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { isChecked = !isChecked }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                                contentDescription = null,
                                                tint = if (isChecked) Color(0xFF00E5FF) else Color(0xFF8E92A8),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = item,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isChecked) Color(0xFF8E92A8) else Color.White
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "No se encontraron tareas pendientes automáticas en esta nota.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF8E92A8)
                                    )
                                }
                            }
                        }

                        // Smart Tags generated by AI
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E30)),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E3349)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Categorías & Tags Sugeridos por IA",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    note.tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { tag ->
                                        Surface(
                                            color = Color(0xFF7C4DFF).copy(alpha = 0.25f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "#$tag",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color(0xFF00E5FF),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Ask AI about this note box
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161929)),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Pregúntale al Agente IA sobre esta nota",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD600)
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = askAiPrompt,
                                        onValueChange = { askAiPrompt = it },
                                        placeholder = { Text("Ej: ¿Qué fechas o compromisos hay?", fontSize = 13.sp, color = Color(0xFF6E738C)) },
                                        modifier = Modifier.weight(1f).testTag("ask_ai_prompt_input"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            focusedBorderColor = Color(0xFF7C4DFF),
                                            unfocusedBorderColor = Color(0xFF2E3349)
                                        ),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            if (askAiPrompt.isNotBlank()) {
                                                isAskingAi = true
                                                val contextStr = "Título: ${note.title}\nContenido: ${note.content}\nResumen: ${note.aiSummary}"
                                                kotlinx.coroutines.MainScope().run {
                                                    // Fire query
                                                    viewModel.askAiAssistant("Sobre la nota '${note.title}': $askAiPrompt")
                                                    askAiPrompt = ""
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .background(Color(0xFF7C4DFF), CircleShape)
                                            .size(42.dp)
                                            .testTag("ask_ai_send_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Enviar",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
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
