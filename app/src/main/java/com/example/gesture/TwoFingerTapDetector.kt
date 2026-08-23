package com.example.gesture

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

data class TouchPointerInfo(
    val id: Long,
    val position: Offset,
    val isDown: Boolean
)

data class GestureState(
    val tapCount: Int = 0,
    val maxTaps: Int = 3,
    val lastTapTime: Long = 0,
    val activePointers: List<TouchPointerInfo> = emptyList(),
    val statusMessage: String = "Da 3 toques con 2 dedos en la pantalla",
    val isTriggered: Boolean = false
)

class TwoFingerTapTracker(private val context: Context? = null) {
    private val _gestureState = MutableStateFlow(GestureState())
    val gestureState: StateFlow<GestureState> = _gestureState.asStateFlow()

    private var tapCount = 0
    private var lastTapTimestamp = 0L
    private val maxIntervalBetweenTapsMs = 900L
    private var twoFingerDownStartTimestamp = 0L
    private var hadTwoFingersSimultaneously = false
    private val activePointerMap = mutableMapOf<PointerId, Offset>()

    fun processPointerEvent(
        event: PointerEvent,
        onTriggerSuccess: () -> Unit
    ) {
        val changes = event.changes
        val currentTime = System.currentTimeMillis()

        // Update active pointer positions
        changes.forEach { change ->
            if (change.pressed) {
                activePointerMap[change.id] = change.position
            } else {
                activePointerMap.remove(change.id)
            }
        }

        val currentPointerCount = activePointerMap.size
        val pointerList = activePointerMap.map { (id, offset) ->
            TouchPointerInfo(id.value, offset, true)
        }

        // Reset if interval since last tap exceeded
        if (tapCount > 0 && (currentTime - lastTapTimestamp > maxIntervalBetweenTapsMs)) {
            tapCount = 0
            _gestureState.value = GestureState(
                tapCount = 0,
                activePointers = pointerList,
                statusMessage = "Tiempo expirado. Intenta 3 toques rápidos con 2 dedos."
            )
        }

        // Detect 2 fingers down
        if (currentPointerCount >= 2) {
            if (!hadTwoFingersSimultaneously) {
                twoFingerDownStartTimestamp = currentTime
                hadTwoFingersSimultaneously = true
            }
        }

        // Check if all fingers just lifted after having 2 fingers
        val allReleased = changes.all { !it.pressed }
        if (allReleased && hadTwoFingersSimultaneously) {
            val tapDuration = currentTime - twoFingerDownStartTimestamp
            hadTwoFingersSimultaneously = false

            // Valid tap duration (between 30ms and 450ms)
            if (tapDuration in 30..450) {
                tapCount++
                lastTapTimestamp = currentTime
                vibrateTap(tapCount)

                if (tapCount >= 3) {
                    _gestureState.value = GestureState(
                        tapCount = 3,
                        activePointers = emptyList(),
                        statusMessage = "¡Atajo detectado! Abriendo captura rápida...",
                        isTriggered = true
                    )
                    vibrateSuccess()
                    tapCount = 0
                    onTriggerSuccess()
                } else {
                    _gestureState.value = GestureState(
                        tapCount = tapCount,
                        lastTapTime = currentTime,
                        activePointers = emptyList(),
                        statusMessage = "Toque $tapCount de 3 registrado... ¡rápido, continúa!"
                    )
                }
            } else {
                // Was a hold or too slow
                _gestureState.value = GestureState(
                    tapCount = tapCount,
                    activePointers = emptyList(),
                    statusMessage = "Toque demasiado prolongado. Haz toques rápidos."
                )
            }
        } else {
            _gestureState.value = _gestureState.value.copy(
                activePointers = pointerList
            )
        }
    }

    fun reset() {
        tapCount = 0
        lastTapTimestamp = 0L
        hadTwoFingersSimultaneously = false
        activePointerMap.clear()
        _gestureState.value = GestureState()
    }

    private fun vibrateTap(step: Int) {
        if (context == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(30L * step, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(30L * step, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(30L * step)
                }
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    private fun vibrateSuccess() {
        if (context == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val pattern = longArrayOf(0, 80, 50, 150)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }
}
