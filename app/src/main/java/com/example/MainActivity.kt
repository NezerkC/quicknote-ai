package com.example

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.GesturePracticeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LockscreenSimulatorScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Enable show when locked capabilities
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F111D)
                ) {
                    QuickNotesApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun QuickNotesApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.HOME -> HomeScreen(viewModel = viewModel)
            AppScreen.NOTE_EDITOR -> NoteEditorScreen(viewModel = viewModel)
            AppScreen.LOCKSCREEN_SIMULATOR -> LockscreenSimulatorScreen(viewModel = viewModel)
            AppScreen.GESTURE_TRAINER -> GesturePracticeScreen(viewModel = viewModel)
            AppScreen.AI_ASSISTANT -> AiAssistantScreen(viewModel = viewModel)
        }
    }
}
