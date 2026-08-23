package com.example.ui.stylus

import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

enum class StylusToolType(val displayName: String, val defaultWidth: Float, val defaultAlpha: Float) {
    FOUNTAIN_PEN("Pluma Fuente", 6f, 1.0f),
    BALLPOINT("Bolígrafo", 4f, 1.0f),
    HIGHLIGHTER("Resaltador", 26f, 0.4f),
    PENCIL("Lápiz Grafito", 3f, 0.75f),
    ERASER("Borrador", 28f, 1.0f)
}

data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f
)

data class DrawingStroke(
    val points: List<StrokePoint>,
    val colorHex: Long,
    val width: Float,
    val toolType: StylusToolType,
    val isEraser: Boolean = (toolType == StylusToolType.ERASER)
)

object StrokeSerializer {
    fun serializeStrokes(strokes: List<DrawingStroke>): String {
        val root = JSONArray()
        for (stroke in strokes) {
            val strokeObj = JSONObject()
            strokeObj.put("color", stroke.colorHex)
            strokeObj.put("width", stroke.width.toDouble())
            strokeObj.put("tool", stroke.toolType.name)
            strokeObj.put("eraser", stroke.isEraser)

            val pointsArr = JSONArray()
            for (p in stroke.points) {
                val ptObj = JSONObject()
                ptObj.put("x", p.x.toDouble())
                ptObj.put("y", p.y.toDouble())
                ptObj.put("p", p.pressure.toDouble())
                pointsArr.put(ptObj)
            }
            strokeObj.put("points", pointsArr)
            root.put(strokeObj)
        }
        return root.toString()
    }

    fun deserializeStrokes(json: String): List<DrawingStroke> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<DrawingStroke>()
        try {
            val root = JSONArray(json)
            for (i in 0 until root.length()) {
                val strokeObj = root.getJSONObject(i)
                val colorHex = strokeObj.optLong("color", 0xFFFFFFFF)
                val width = strokeObj.optDouble("width", 4.0).toFloat()
                val toolName = strokeObj.optString("tool", StylusToolType.BALLPOINT.name)
                val toolType = try {
                    StylusToolType.valueOf(toolName)
                } catch (e: Exception) {
                    StylusToolType.BALLPOINT
                }
                val isEraser = strokeObj.optBoolean("eraser", false)

                val pointsArr = strokeObj.optJSONArray("points") ?: JSONArray()
                val points = mutableListOf<StrokePoint>()
                for (j in 0 until pointsArr.length()) {
                    val ptObj = pointsArr.getJSONObject(j)
                    val x = ptObj.optDouble("x", 0.0).toFloat()
                    val y = ptObj.optDouble("y", 0.0).toFloat()
                    val p = ptObj.optDouble("p", 1.0).toFloat()
                    points.add(StrokePoint(x, y, p))
                }
                if (points.isNotEmpty()) {
                    list.add(
                        DrawingStroke(
                            points = points,
                            colorHex = colorHex,
                            width = width,
                            toolType = toolType,
                            isEraser = isEraser
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Return empty list on parse error
        }
        return list
    }
}
