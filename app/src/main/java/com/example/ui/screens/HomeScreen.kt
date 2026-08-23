package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.NoteCard

val FILTER_TAGS = listOf(
    "Todos",
    "Voz",
    "Lápiz",
    "Resumidas IA",
    "Favoritos",
    "Idea",
    "Trabajo",
    "Urgente",
    "Personal"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF7C4DFF).copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "QuickNotes AI",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.padding(7.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "QuickNotes AI",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Notas rápidas • Dictado • Lápiz • IA",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8E92A8),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    // Lock Screen Simulator Shortcut Button
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.LOCKSCREEN_SIMULATOR) },
                        modifier = Modifier.testTag("home_lockscreen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Simulador Bloqueado",
                            tint = Color(0xFF00E5FF)
                        )
                    }

                    // Gesture Trainer Button
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.GESTURE_TRAINER) },
                        modifier = Modifier.testTag("home_gesture_trainer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Entrenador de Gesto",
                            tint = Color(0xFFFFD600)
                        )
                    }

                    // AI Assistant Hub Button
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.AI_ASSISTANT) },
                        modifier = Modifier.testTag("home_ai_assistant_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Agente IA",
                            tint = Color(0xFFB388FF)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F111D)
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Voice Capture FAB
                SmallFloatingActionButton(
                    onClick = { viewModel.createNewNote(startWithVoice = true) },
                    containerColor = Color(0xFFFF5252),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_quick_voice")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Dictar nota rápida",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Quick Stylus / Smart Pen FAB
                SmallFloatingActionButton(
                    onClick = { viewModel.createNewNote(startWithStylus = true) },
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color(0xFF0F111D),
                    modifier = Modifier.testTag("fab_quick_stylus")
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Apunte rápido con lápiz",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Main Add Note FAB
                FloatingActionButton(
                    onClick = { viewModel.createNewNote() },
                    containerColor = Color(0xFF7C4DFF),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_new_note")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva nota",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFF0F111D),
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Lock Screen 3-tap gesture feature banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF))),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        viewModel.navigateTo(AppScreen.LOCKSCREEN_SIMULATOR)
                    }
                    .testTag("home_lockscreen_banner"),
                color = Color(0xFF181B2B)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gesture,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.padding(7.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Atajo de Pantalla Bloqueada",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "3 toques con 2 dedos abren notas al instante",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB0B3C7),
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF7C4DFF).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(
                            text = "Probar",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Buscar en notas, resúmenes IA o tags...", color = Color(0xFF717691)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color(0xFF8E92A8)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar búsqueda",
                                tint = Color(0xFF8E92A8)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("home_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF7C4DFF),
                    unfocusedBorderColor = Color(0xFF282D42),
                    focusedContainerColor = Color(0xFF161927),
                    unfocusedContainerColor = Color(0xFF161927)
                ),
                singleLine = true
            )

            // Category / Filter Tags
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FILTER_TAGS) { tag ->
                    val isSelected = selectedTag == tag
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSelectedTag(tag) },
                        label = {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF7C4DFF),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E2235),
                            labelColor = Color(0xFFB5B8CD)
                        ),
                        modifier = Modifier.testTag("filter_chip_$tag")
                    )
                }
            }

            // Notes List or Empty State
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E2235),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF7C4DFF),
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No se encontraron notas" else "¡Tu bloc de notas está listo!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "Intenta buscar con otros términos"
                            } else {
                                "Pulsa el micrófono para dictar, el lápiz para dibujar o toca el atajo de 3 toques con 2 dedos en la pantalla de bloqueo."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8E92A8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { viewModel.openNote(note) },
                            onTogglePin = { viewModel.togglePin(note) },
                            onToggleFavorite = { viewModel.toggleFavorite(note) }
                        )
                    }
                }
            }
        }
    }
}
