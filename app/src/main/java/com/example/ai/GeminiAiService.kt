package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiSummaryResult(
    val summary: String,
    val keyPoints: List<String>,
    val actionItems: List<String>,
    val suggestedTitle: String,
    val suggestedTags: List<String>,
    val rawResponse: String = ""
)

class GeminiAiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun summarizeAndAnalyze(
        content: String,
        existingTitle: String = ""
    ): AiSummaryResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalSummaryFallback(content, existingTitle)
        }

        val prompt = """
            Eres un asistente inteligente de toma de notas rápidas. Analiza el siguiente contenido de una nota (puede provenir de dictado por voz, escritura rápida o apuntes con lápiz óptico).
            
            Título actual: "$existingTitle"
            Contenido:
            \"\"\"
            $content
            \"\"\"
            
            Devuelve un JSON estrictamente estructurado con las siguientes claves:
            {
              "suggestedTitle": "Título conciso y descriptivo en español (máx 6 palabras)",
              "summary": "Resumen ejecutivo claro y directo en 2-3 frases",
              "keyPoints": ["Punto clave 1", "Punto clave 2", "Punto clave 3"],
              "actionItems": ["Tarea pendiente 1", "Tarea pendiente 2"],
              "suggestedTags": ["Idea", "Trabajo", "Personal", "Urgente", "Proyecto", etc. máx 3 tags]
            }
            
            Responde ÚNICAMENTE con el objeto JSON válido sin bloques markdown adicionales si es posible.
        """.trimIndent()

        try {
            val responseJson = callGeminiRaw(prompt, apiKey)
            parseAiSummaryJson(responseJson, content, existingTitle)
        } catch (e: Exception) {
            Log.e("GeminiAiService", "API error: ${e.localizedMessage}", e)
            generateLocalSummaryFallback(content, existingTitle, errorMsg = e.localizedMessage)
        }
    }

    suspend fun polishAndStructure(content: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext cleanLocalTranscript(content)
        }

        val prompt = """
            Corrige y formatea elegantemente la siguiente nota rápida proveniente de dictado por voz o borrador rápido.
            Mejora la puntuación, ortografía, divide en párrafos limpios o viñetas según convenga, manteniendo el significado original intacto en español.
            
            Nota original:
            \"\"\"
            $content
            \"\"\"
            
            Devuelve únicamente el texto corregido y formateado.
        """.trimIndent()

        try {
            val response = callGeminiRaw(prompt, apiKey)
            extractTextFromGeminiResponse(response).ifBlank { cleanLocalTranscript(content) }
        } catch (e: Exception) {
            Log.e("GeminiAiService", "Error polishing text: ${e.localizedMessage}", e)
            cleanLocalTranscript(content)
        }
    }

    suspend fun askAssistantAboutNotes(contextNotes: String, question: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Para interactuar con el agente Gemini en vivo, configura tu clave GEMINI_API_KEY en los secretos del proyecto. Mientras tanto, tus notas están guardadas de forma segura."
        }

        val prompt = """
            Eres el agente de IA de la app QuickNotes. Tienes acceso al contenido de las notas del usuario.
            
            Contexto de notas del usuario:
            \"\"\"
            $contextNotes
            \"\"\"
            
            Pregunta del usuario:
            "$question"
            
            Responde de manera concisa, útil y estructurada en español.
        """.trimIndent()

        try {
            val response = callGeminiRaw(prompt, apiKey)
            extractTextFromGeminiResponse(response).ifBlank { "No se pudo generar una respuesta." }
        } catch (e: Exception) {
            "Error al consultar el agente: ${e.localizedMessage}"
        }
    }

    private fun callGeminiRaw(prompt: String, apiKey: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val jsonRequest = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObj)
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)
            
            val generationConfig = JSONObject().apply {
                put("temperature", 0.3)
                put("topP", 0.8)
            }
            put("generationConfig", generationConfig)
        }

        val requestBody = jsonRequest.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: $responseBody")
        }

        return responseBody
    }

    private fun extractTextFromGeminiResponse(jsonString: String): String {
        return try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val firstCand = candidates.getJSONObject(0)
            val content = firstCand.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            if (parts.length() == 0) return ""
            parts.getJSONObject(0).optString("text", "")
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseAiSummaryJson(
        geminiResponseJson: String,
        originalContent: String,
        existingTitle: String
    ): AiSummaryResult {
        val text = extractTextFromGeminiResponse(geminiResponseJson).trim()
        
        // Clean markdown blocks if enclosed in ```json ... ```
        val cleanedJson = text
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val obj = JSONObject(cleanedJson)
            val suggestedTitle = obj.optString("suggestedTitle", existingTitle.ifBlank { "Nota rápida" })
            val summary = obj.optString("summary", "")
            
            val keyPointsList = mutableListOf<String>()
            val keyPointsArr = obj.optJSONArray("keyPoints")
            if (keyPointsArr != null) {
                for (i in 0 until keyPointsArr.length()) {
                    val item = keyPointsArr.optString(i)
                    if (item.isNotBlank()) keyPointsList.add(item)
                }
            }

            val actionItemsList = mutableListOf<String>()
            val actionItemsArr = obj.optJSONArray("actionItems")
            if (actionItemsArr != null) {
                for (i in 0 until actionItemsArr.length()) {
                    val item = actionItemsArr.optString(i)
                    if (item.isNotBlank()) actionItemsList.add(item)
                }
            }

            val tagsList = mutableListOf<String>()
            val tagsArr = obj.optJSONArray("suggestedTags")
            if (tagsArr != null) {
                for (i in 0 until tagsArr.length()) {
                    val item = tagsArr.optString(i)
                    if (item.isNotBlank()) tagsList.add(item)
                }
            }

            AiSummaryResult(
                summary = summary,
                keyPoints = keyPointsList,
                actionItems = actionItemsList,
                suggestedTitle = suggestedTitle,
                suggestedTags = if (tagsList.isEmpty()) listOf("Nota", "Rápida") else tagsList,
                rawResponse = text
            )
        } catch (e: Exception) {
            Log.w("GeminiAiService", "Failed to parse JSON response: $text, falling back", e)
            AiSummaryResult(
                summary = text.take(300),
                keyPoints = listOf("Contenido analizado por IA"),
                actionItems = emptyList(),
                suggestedTitle = existingTitle.ifBlank { "Nota rápida IA" },
                suggestedTags = listOf("IA", "Rápido"),
                rawResponse = text
            )
        }
    }

    private fun generateLocalSummaryFallback(
        content: String,
        existingTitle: String,
        errorMsg: String? = null
    ): AiSummaryResult {
        val lines = content.lines().filter { it.isNotBlank() }
        val words = content.split("\\s+".toRegex()).filter { it.isNotBlank() }
        
        val autoTitle = if (existingTitle.isNotBlank()) {
            existingTitle
        } else if (lines.isNotEmpty()) {
            lines.first().take(30)
        } else {
            "Nota rápida"
        }

        val summary = if (words.size > 8) {
            "Resumen: ${words.take(24).joinToString(" ")}..."
        } else {
            content.take(150)
        }

        val keyPoints = lines.take(3).map { "• ${it.take(80)}" }
        val actionItems = lines.filter { line ->
            line.contains("hacer", ignoreCase = true) ||
            line.contains("llamar", ignoreCase = true) ||
            line.contains("comprar", ignoreCase = true) ||
            line.contains("revisar", ignoreCase = true) ||
            line.contains("enviar", ignoreCase = true) ||
            line.startsWith("-") || line.startsWith("*")
        }.map { it.removePrefix("-").removePrefix("*").trim() }

        val tags = mutableListOf("Rápido")
        if (content.contains("urgente", ignoreCase = true) || content.contains("hoy", ignoreCase = true)) {
            tags.add("Urgente")
        }
        if (content.contains("trabajo", ignoreCase = true) || content.contains("reunión", ignoreCase = true)) {
            tags.add("Trabajo")
        }
        if (tags.size == 1) tags.add("Idea")

        return AiSummaryResult(
            summary = summary,
            keyPoints = if (keyPoints.isNotEmpty()) keyPoints else listOf("Nota registrada"),
            actionItems = actionItems,
            suggestedTitle = autoTitle,
            suggestedTags = tags,
            rawResponse = errorMsg ?: "Resumen local generado"
        )
    }

    private fun cleanLocalTranscript(content: String): String {
        return content
            .replace("\\s+".toRegex(), " ")
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
