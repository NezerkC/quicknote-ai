package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AiSummaryResult
import com.example.ai.GeminiAiService
import com.example.data.database.AppDatabase
import com.example.data.model.Note
import com.example.data.repository.NoteRepository
import com.example.gesture.TwoFingerTapTracker
import com.example.voice.VoiceSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    NOTE_EDITOR,
    LOCKSCREEN_SIMULATOR,
    GESTURE_TRAINER,
    AI_ASSISTANT
}

enum class NoteTab {
    TEXT,
    STYLUS,
    AI_SUMMARY
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val geminiAiService = GeminiAiService()
    val voiceSpeechManager = VoiceSpeechManager(application.applicationContext)
    val gestureTracker = TwoFingerTapTracker(application.applicationContext)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = NoteRepository(db.noteDao())
    }

    // Navigation & Screen State
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Notes lists & search
    val rawNotes = repository.activeNotes
    val archivedNotes = repository.archivedNotes
    val favoriteNotes = repository.favoriteNotes

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow("Todos")
    val selectedTag: StateFlow<String> = _selectedTag.asStateFlow()

    // Filtered Notes
    val notes: StateFlow<List<Note>> = combine(rawNotes, _searchQuery, _selectedTag) { list, query, tag ->
        var result = list
        if (query.isNotBlank()) {
            result = result.filter { note ->
                note.title.contains(query, ignoreCase = true) ||
                note.content.contains(query, ignoreCase = true) ||
                note.aiSummary.contains(query, ignoreCase = true) ||
                note.tags.contains(query, ignoreCase = true)
            }
        }
        if (tag != "Todos") {
            result = when (tag) {
                "Voz" -> result.filter { it.hasVoiceRecording }
                "Lápiz" -> result.filter { it.drawingStrokesJson.isNotBlank() }
                "Resumidas IA" -> result.filter { it.aiSummary.isNotBlank() }
                "Favoritos" -> result.filter { it.isFavorite }
                else -> result.filter { it.tags.contains(tag, ignoreCase = true) }
            }
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Note Editor
    private val _currentNote = MutableStateFlow<Note?>(null)
    val currentNote: StateFlow<Note?> = _currentNote.asStateFlow()

    private val _activeEditorTab = MutableStateFlow(NoteTab.TEXT)
    val activeEditorTab: StateFlow<NoteTab> = _activeEditorTab.asStateFlow()

    // AI Processing State
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _aiStatusMessage = MutableStateFlow<String?>(null)
    val aiStatusMessage: StateFlow<String?> = _aiStatusMessage.asStateFlow()

    // AI Chat History in Assistant tab
    private val _aiChatMessages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val aiChatMessages: StateFlow<List<Pair<String, String>>> = _aiChatMessages.asStateFlow()

    // Quick Capture Dialog State (from Lock Screen or 3-tap gesture)
    private val _isQuickCaptureVisible = MutableStateFlow(false)
    val isQuickCaptureVisible: StateFlow<Boolean> = _isQuickCaptureVisible.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedTag(tag: String) {
        _selectedTag.value = tag
    }

    fun setEditorTab(tab: NoteTab) {
        _activeEditorTab.value = tab
    }

    fun openNote(note: Note) {
        _currentNote.value = note
        _activeEditorTab.value = if (note.drawingStrokesJson.isNotBlank() && note.content.isBlank()) {
            NoteTab.STYLUS
        } else {
            NoteTab.TEXT
        }
        _currentScreen.value = AppScreen.NOTE_EDITOR
    }

    fun createNewNote(startWithVoice: Boolean = false, startWithStylus: Boolean = false) {
        val newNote = Note(
            title = "",
            content = "",
            tags = if (startWithVoice) "Voz" else if (startWithStylus) "Lápiz" else "General",
            hasVoiceRecording = startWithVoice
        )
        viewModelScope.launch {
            val insertedId = repository.insert(newNote)
            val createdNote = newNote.copy(id = insertedId)
            _currentNote.value = createdNote
            _activeEditorTab.value = if (startWithStylus) NoteTab.STYLUS else NoteTab.TEXT
            _currentScreen.value = AppScreen.NOTE_EDITOR

            if (startWithVoice) {
                startVoiceDictation()
            }
        }
    }

    fun triggerQuickCaptureFromLockscreen() {
        val quickNote = Note(
            title = "Nota Rápida Lockscreen",
            content = "",
            tags = "Lockscreen, Rápida"
        )
        viewModelScope.launch {
            val insertedId = repository.insert(quickNote)
            val noteWithId = quickNote.copy(id = insertedId)
            _currentNote.value = noteWithId
            _isQuickCaptureVisible.value = true
        }
    }

    fun dismissQuickCapture() {
        _isQuickCaptureVisible.value = false
    }

    fun updateCurrentNoteContent(title: String? = null, content: String? = null, colorHex: Long? = null) {
        val note = _currentNote.value ?: return
        val updated = note.copy(
            title = title ?: note.title,
            content = content ?: note.content,
            colorHex = colorHex ?: note.colorHex,
            updatedAt = System.currentTimeMillis()
        )
        _currentNote.value = updated
        viewModelScope.launch {
            repository.update(updated)
        }
    }

    fun updateCurrentNoteDrawing(strokesJson: String) {
        val note = _currentNote.value ?: return
        val updated = note.copy(
            drawingStrokesJson = strokesJson,
            updatedAt = System.currentTimeMillis()
        )
        _currentNote.value = updated
        viewModelScope.launch {
            repository.update(updated)
        }
    }

    // Voice Dictation
    fun startVoiceDictation() {
        voiceSpeechManager.startListening { transcribedText ->
            val note = _currentNote.value ?: return@startListening
            val newContent = if (note.content.isBlank()) {
                transcribedText
            } else {
                "${note.content} $transcribedText"
            }
            val updated = note.copy(
                content = newContent,
                hasVoiceRecording = true,
                updatedAt = System.currentTimeMillis()
            )
            _currentNote.value = updated
            viewModelScope.launch {
                repository.update(updated)
            }
        }
    }

    fun stopVoiceDictation() {
        voiceSpeechManager.stopListening()
    }

    // AI Functions
    fun autoSummarizeCurrentNote() {
        val note = _currentNote.value ?: return
        val fullContent = buildString {
            if (note.content.isNotBlank()) append(note.content)
            if (note.drawingStrokesJson.isNotBlank()) {
                append("\n[Nota contiene dibujo/apunte manuscrito con lápiz inteligente]")
            }
        }

        if (fullContent.isBlank()) {
            _aiStatusMessage.value = "Ingresa texto o dicta algo para que la IA lo resuma."
            return
        }

        _isAiLoading.value = true
        _aiStatusMessage.value = "Agente Gemini analizando y generando resumen..."

        viewModelScope.launch {
            try {
                val result: AiSummaryResult = geminiAiService.summarizeAndAnalyze(
                    content = fullContent,
                    existingTitle = note.title
                )

                val updatedTitle = if (note.title.isBlank()) result.suggestedTitle else note.title
                val actionItemsStr = result.actionItems.joinToString("\n")
                val tagsStr = result.suggestedTags.joinToString(", ")

                val updatedNote = note.copy(
                    title = updatedTitle,
                    aiSummary = result.summary,
                    aiActionItems = actionItemsStr,
                    tags = tagsStr,
                    updatedAt = System.currentTimeMillis()
                )

                _currentNote.value = updatedNote
                repository.update(updatedNote)
                _aiStatusMessage.value = "¡Resumen inteligente generado con éxito!"
                _activeEditorTab.value = NoteTab.AI_SUMMARY
            } catch (e: Exception) {
                _aiStatusMessage.value = "Error al resumir: ${e.localizedMessage}"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun polishCurrentNote() {
        val note = _currentNote.value ?: return
        if (note.content.isBlank()) return

        _isAiLoading.value = true
        _aiStatusMessage.value = "Agente Gemini estructurando y puliendo nota..."

        viewModelScope.launch {
            try {
                val polished = geminiAiService.polishAndStructure(note.content)
                val updatedNote = note.copy(
                    content = polished,
                    updatedAt = System.currentTimeMillis()
                )
                _currentNote.value = updatedNote
                repository.update(updatedNote)
                _aiStatusMessage.value = "Nota formateada y pulida con éxito."
            } catch (e: Exception) {
                _aiStatusMessage.value = "Error al pulir nota: ${e.localizedMessage}"
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun askAiAssistant(question: String) {
        if (question.isBlank()) return
        val currentNotesList = notes.value
        val context = currentNotesList.take(15).joinToString("\n---\n") {
            "Título: ${it.title}\nContenido: ${it.content}\nResumen: ${it.aiSummary}\nTags: ${it.tags}"
        }

        val messages = _aiChatMessages.value.toMutableList()
        messages.add(Pair("user", question))
        _aiChatMessages.value = messages
        _isAiLoading.value = true

        viewModelScope.launch {
            try {
                val answer = geminiAiService.askAssistantAboutNotes(context, question)
                val updated = _aiChatMessages.value.toMutableList()
                updated.add(Pair("model", answer))
                _aiChatMessages.value = updated
            } catch (e: Exception) {
                val updated = _aiChatMessages.value.toMutableList()
                updated.add(Pair("model", "Error: ${e.localizedMessage}"))
                _aiChatMessages.value = updated
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun togglePin(note: Note) {
        val updated = note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis())
        if (_currentNote.value?.id == note.id) {
            _currentNote.value = updated
        }
        viewModelScope.launch {
            repository.update(updated)
        }
    }

    fun toggleFavorite(note: Note) {
        val updated = note.copy(isFavorite = !note.isFavorite, updatedAt = System.currentTimeMillis())
        if (_currentNote.value?.id == note.id) {
            _currentNote.value = updated
        }
        viewModelScope.launch {
            repository.update(updated)
        }
    }

    fun deleteCurrentNote() {
        val note = _currentNote.value ?: return
        viewModelScope.launch {
            repository.delete(note)
            _currentNote.value = null
            _currentScreen.value = AppScreen.HOME
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
            if (_currentNote.value?.id == note.id) {
                _currentNote.value = null
                _currentScreen.value = AppScreen.HOME
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceSpeechManager.destroy()
    }
}
