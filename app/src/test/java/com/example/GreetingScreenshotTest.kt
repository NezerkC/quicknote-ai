package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.Note
import com.example.ui.components.NoteCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleNote = Note(
      id = 1,
      title = "Reunión de Diseño",
      content = "Definir arquitectura de la app de notas rápidas.",
      aiSummary = "Reunión enfocada en diseño y arquitectura rápida.",
      tags = "Trabajo, Diseño"
    )
    composeTestRule.setContent {
      MyApplicationTheme(darkTheme = true) {
        NoteCard(
          note = sampleNote,
          onClick = {},
          onTogglePin = {},
          onToggleFavorite = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
