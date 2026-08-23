package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "notes")
@JsonClass(generateAdapter = true)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val aiSummary: String = "",
    val aiActionItems: String = "", // Comma/newline-separated list or JSON
    val tags: String = "General",
    val drawingStrokesJson: String = "", // Serialized stylus strokes
    val hasVoiceRecording: Boolean = false,
    val colorHex: Long = 0xFF1E2235, // Card background color
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
