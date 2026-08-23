package com.example.ui.stylus

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val STYLUS_PRESET_COLORS = listOf(
    0xFF00E5FF, // Neon Cyan
    0xFF7C4DFF, // Electric Violet
    0xFFFF4081, // Hot Pink
    0xFFFFD600, // Bright Gold
    0xFF00E676, // Vivid Green
    0xFFFF6E40, // Coral Orange
    0xFFFFFFFF, // Pure White
    0xFF121212  // Charcoal Black
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StylusDrawingCanvas(
    modifier: Modifier = Modifier,
    initialStrokesJson: String = "",
    onStrokesChanged: (String) -> Unit
) {
    val strokes = remember {
        val list = mutableStateListOf<DrawingStroke>()
        list.addAll(StrokeSerializer.deserializeStrokes(initialStrokesJson))
        list
    }
    val undoStack = remember { mutableStateListOf<DrawingStroke>() }

    var selectedTool by remember { mutableStateOf(StylusToolType.FOUNTAIN_PEN) }
    var selectedColorHex by remember { mutableStateOf(0xFF00E5FF) }
    var strokeWidth by remember { mutableFloatStateOf(6f) }
    var isStylusDetected by remember { mutableStateOf(false) }
    var currentPressure by remember { mutableFloatStateOf(1f) }
    var currentDrawingPoints by remember { mutableStateOf<List<StrokePoint>>(emptyList()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF13151F))
    ) {
        // Smart Stylus Status Banner
        Surface(
            color = Color(0xFF1B1E2D),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Smart Stylus",
                        tint = if (isStylusDetected) Color(0xFF00E5FF) else Color(0xFF9E9E9E),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isStylusDetected) "Lápiz inteligente activo" else "Lápiz / Táctil disponible",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isStylusDetected) Color(0xFF00E5FF) else Color(0xFFB0B3C7)
                    )
                    if (isStylusDetected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Presión: ${(currentPressure * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFD600)
                        )
                    }
                }

                // Action controls: Undo, Redo, Clear
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (strokes.isNotEmpty()) {
                                val last = strokes.removeAt(strokes.lastIndex)
                                undoStack.add(last)
                                onStrokesChanged(StrokeSerializer.serializeStrokes(strokes))
                            }
                        },
                        enabled = strokes.isNotEmpty(),
                        modifier = Modifier.size(36.dp).testTag("stylus_undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Deshacer",
                            tint = if (strokes.isNotEmpty()) Color.White else Color(0xFF555566)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (undoStack.isNotEmpty()) {
                                val restored = undoStack.removeAt(undoStack.lastIndex)
                                strokes.add(restored)
                                onStrokesChanged(StrokeSerializer.serializeStrokes(strokes))
                            }
                        },
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier.size(36.dp).testTag("stylus_redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "Rehacer",
                            tint = if (undoStack.isNotEmpty()) Color.White else Color(0xFF555566)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (strokes.isNotEmpty()) {
                                strokes.clear()
                                undoStack.clear()
                                onStrokesChanged("")
                            }
                        },
                        enabled = strokes.isNotEmpty(),
                        modifier = Modifier.size(36.dp).testTag("stylus_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Limpiar lienzo",
                            tint = if (strokes.isNotEmpty()) Color(0xFFFF5252) else Color(0xFF555566)
                        )
                    }
                }
            }
        }

        // Tools Bar (Fountain Pen, Ballpoint, Highlighter, Pencil, Eraser)
        Surface(
            color = Color(0xFF171A27),
            modifier = Modifier.fillMaxWidth()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StylusToolType.values().forEach { tool ->
                    val isSelected = selectedTool == tool
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedTool = tool
                            strokeWidth = tool.defaultWidth
                        },
                        label = {
                            Text(
                                text = tool.displayName,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (tool) {
                                    StylusToolType.FOUNTAIN_PEN -> Icons.Default.Create
                                    StylusToolType.BALLPOINT -> Icons.Default.Edit
                                    StylusToolType.HIGHLIGHTER -> Icons.Default.FormatPaint
                                    StylusToolType.PENCIL -> Icons.Default.Brush
                                    StylusToolType.ERASER -> Icons.Default.Clear
                                },
                                contentDescription = tool.displayName,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF7C4DFF),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF222638),
                            labelColor = Color(0xFFCFD2E6)
                        )
                    )
                }
            }
        }

        // Color palette bar (if not eraser)
        AnimatedVisibility(
            visible = selectedTool != StylusToolType.ERASER,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF171A27))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Color:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E9E9E)
                )

                STYLUS_PRESET_COLORS.forEach { colorVal ->
                    val isCurrentColor = selectedColorHex == colorVal
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .border(
                                width = if (isCurrentColor) 2.5.dp else 1.dp,
                                color = if (isCurrentColor) Color.White else Color(0x44FFFFFF),
                                shape = CircleShape
                            )
                            .clickable {
                                selectedColorHex = colorVal
                            }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Stroke width slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(130.dp)
                ) {
                    Text(
                        text = "${strokeWidth.toInt()}pt",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB0B3C7),
                        modifier = Modifier.width(28.dp)
                    )
                    Slider(
                        value = strokeWidth,
                        onValueChange = { strokeWidth = it },
                        valueRange = 2f..40f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF7C4DFF),
                            activeTrackColor = Color(0xFF7C4DFF)
                        )
                    )
                }
            }
        }

        // Interactive Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1D2B))
                .border(1.dp, Color(0xFF2E3349), RoundedCornerShape(16.dp))
                .clipToBounds()
                .testTag("stylus_drawing_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(selectedTool, selectedColorHex, strokeWidth) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull() ?: continue

                                isStylusDetected = (change.type == PointerType.Stylus)
                                currentPressure = change.pressure.coerceIn(0.2f, 1.5f)

                                if (change.pressed) {
                                    val currentPoints = currentDrawingPoints.toMutableList()
                                    currentPoints.add(
                                        StrokePoint(
                                            x = change.position.x,
                                            y = change.position.y,
                                            pressure = currentPressure
                                        )
                                    )
                                    currentDrawingPoints = currentPoints
                                    change.consume()
                                } else {
                                    // Finger or stylus lifted
                                    if (currentDrawingPoints.isNotEmpty()) {
                                        val newStroke = DrawingStroke(
                                            points = currentDrawingPoints,
                                            colorHex = if (selectedTool == StylusToolType.ERASER) 0xFF1A1D2B else selectedColorHex,
                                            width = strokeWidth,
                                            toolType = selectedTool,
                                            isEraser = (selectedTool == StylusToolType.ERASER)
                                        )
                                        strokes.add(newStroke)
                                        undoStack.clear()
                                        currentDrawingPoints = emptyList()
                                        onStrokesChanged(StrokeSerializer.serializeStrokes(strokes))
                                        change.consume()
                                    }
                                }
                            }
                        }
                    }
            ) {
                // Draw grid lines background for notes paper feel
                val gridSpacing = 28.dp.toPx()
                var y = gridSpacing
                while (y < size.height) {
                    drawLine(
                        color = Color(0x18FFFFFF),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    y += gridSpacing
                }

                // Render completed strokes
                for (stroke in strokes) {
                    drawSingleStroke(stroke)
                }

                // Render currently active stroke in progress
                if (currentDrawingPoints.isNotEmpty()) {
                    drawSingleStroke(
                        DrawingStroke(
                            points = currentDrawingPoints,
                            colorHex = if (selectedTool == StylusToolType.ERASER) 0xFF1A1D2B else selectedColorHex,
                            width = strokeWidth,
                            toolType = selectedTool,
                            isEraser = (selectedTool == StylusToolType.ERASER)
                        )
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawSingleStroke(stroke: DrawingStroke) {
    if (stroke.points.size < 2) {
        val pt = stroke.points.firstOrNull() ?: return
        val baseColor = Color(stroke.colorHex)
        val alpha = stroke.toolType.defaultAlpha
        drawCircle(
            color = baseColor.copy(alpha = alpha),
            radius = stroke.width / 2f * pt.pressure,
            center = Offset(pt.x, pt.y)
        )
        return
    }

    val path = Path()
    val first = stroke.points.first()
    path.moveTo(first.x, first.y)

    for (i in 1 until stroke.points.size) {
        val prev = stroke.points[i - 1]
        val curr = stroke.points[i]
        val midX = (prev.x + curr.x) / 2f
        val midY = (prev.y + curr.y) / 2f
        path.quadraticTo(prev.x, prev.y, midX, midY)
    }
    val last = stroke.points.last()
    path.lineTo(last.x, last.y)

    val baseColor = Color(stroke.colorHex)
    val alpha = stroke.toolType.defaultAlpha
    val avgPressure = stroke.points.map { it.pressure }.average().toFloat().coerceIn(0.4f, 1.4f)
    val dynamicWidth = if (stroke.toolType == StylusToolType.FOUNTAIN_PEN) {
        stroke.width * avgPressure
    } else {
        stroke.width
    }

    drawPath(
        path = path,
        color = baseColor.copy(alpha = alpha),
        style = Stroke(
            width = dynamicWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

@Composable
fun StylusDrawingThumbnail(
    modifier: Modifier = Modifier,
    strokesJson: String
) {
    val strokes = remember(strokesJson) {
        StrokeSerializer.deserializeStrokes(strokesJson)
    }

    if (strokes.isEmpty()) return

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141724))
            .clipToBounds()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (strokes.isEmpty()) return@Canvas

            // Find bounds to scale nicely
            var minX = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var minY = Float.MAX_VALUE
            var maxY = Float.MIN_VALUE

            for (s in strokes) {
                for (p in s.points) {
                    if (p.x < minX) minX = p.x
                    if (p.x > maxX) maxX = p.x
                    if (p.y < minY) minY = p.y
                    if (p.y > maxY) maxY = p.y
                }
            }

            val strokeWidthSpan = (maxX - minX).coerceAtLeast(1f)
            val strokeHeightSpan = (maxY - minY).coerceAtLeast(1f)

            val scaleX = (size.width - 24f) / strokeWidthSpan
            val scaleY = (size.height - 24f) / strokeHeightSpan
            val scale = minOf(scaleX, scaleY).coerceAtMost(1f)

            val offsetX = 12f - minX * scale
            val offsetY = 12f - minY * scale

            for (stroke in strokes) {
                if (stroke.points.size < 2) continue
                val path = Path()
                val first = stroke.points.first()
                path.moveTo(first.x * scale + offsetX, first.y * scale + offsetY)

                for (i in 1 until stroke.points.size) {
                    val p = stroke.points[i]
                    path.lineTo(p.x * scale + offsetX, p.y * scale + offsetY)
                }

                drawPath(
                    path = path,
                    color = Color(stroke.colorHex).copy(alpha = stroke.toolType.defaultAlpha),
                    style = Stroke(
                        width = (stroke.width * scale * 0.8f).coerceAtLeast(1.5f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
