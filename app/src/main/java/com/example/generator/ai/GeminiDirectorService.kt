package com.example.generator.ai

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

data class StoryboardShot(
    val shotNumber: Int,
    val shotType: String, // e.g., "Wide Establishing", "Medium Action Tracking", "Close-up Climax", "Cinematic Outro"
    val visualDescription: String,
    val cameraAngle: String,
    val lightingMood: String
)

data class ScriptAnalysis(
    val expandedPrompt: String,
    val cinematographyNotes: String,
    val recommendedFps: Int,
    val suggestedAspect: String,
    val storyboard: List<StoryboardShot>
)

object GeminiDirectorService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeAndDirect(
        userPrompt: String,
        styleName: String,
        cameraMovement: String
    ): ScriptAnalysis = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = """
                    You are an award-winning Hollywood AI visual director and cinematographer.
                    Analyze the user's concept for a cinematic video clip.
                    Return a strict JSON object with:
                    {
                      "expandedPrompt": "A lush, high-detail cinematic visual description",
                      "cinematographyNotes": "Lighting, color grade, and lens specifications",
                      "recommendedFps": 30 or 60,
                      "suggestedAspect": "16:9" or "9:16",
                      "storyboard": [
                        {"shotNumber": 1, "shotType": "Wide Establishing", "visualDescription": "...", "cameraAngle": "...", "lightingMood": "..."},
                        {"shotNumber": 2, "shotType": "Medium Tracking", "visualDescription": "...", "cameraAngle": "...", "lightingMood": "..."},
                        {"shotNumber": 3, "shotType": "Close-up Climax", "visualDescription": "...", "cameraAngle": "...", "lightingMood": "..."},
                        {"shotNumber": 4, "shotType": "Cinematic Outro", "visualDescription": "...", "cameraAngle": "...", "lightingMood": "..."}
                      ]
                    }
                """.trimIndent()

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().put(
                        JSONObject().put("parts", JSONArray().put(
                            JSONObject().put("text", "$systemPrompt\n\nConcept: $userPrompt\nStyle: $styleName\nCamera: $cameraMovement")
                        ))
                    ))
                    put("generationConfig", JSONObject().put("responseMimeType", "application/json"))
                }

                val request = Request.Builder()
                    .url(url)
                    .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null) {
                            val json = JSONObject(body)
                            val candidates = json.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val content = candidates.getJSONObject(0).getJSONObject("content")
                                val text = content.getJSONArray("parts").getJSONObject(0).getString("text")
                                val data = JSONObject(text)

                                val shotsList = mutableListOf<StoryboardShot>()
                                val sbArray = data.optJSONArray("storyboard")
                                if (sbArray != null) {
                                    for (i in 0 until sbArray.length()) {
                                        val item = sbArray.getJSONObject(i)
                                        shotsList.add(
                                            StoryboardShot(
                                                shotNumber = item.optInt("shotNumber", i + 1),
                                                shotType = item.optString("shotType", "Shot ${i + 1}"),
                                                visualDescription = item.optString("visualDescription", ""),
                                                cameraAngle = item.optString("cameraAngle", "Eye level"),
                                                lightingMood = item.optString("lightingMood", "Cinematic")
                                            )
                                        )
                                    }
                                }

                                return@withContext ScriptAnalysis(
                                    expandedPrompt = data.optString("expandedPrompt", userPrompt),
                                    cinematographyNotes = data.optString("cinematographyNotes", "35mm anamorphic, volumetric haze"),
                                    recommendedFps = data.optInt("recommendedFps", 30),
                                    suggestedAspect = data.optString("suggestedAspect", "16:9"),
                                    storyboard = shotsList
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Fall back gracefully
            }
        }

        // Offline Director Engine Fallback
        fallbackDirectorAnalysis(userPrompt, styleName, cameraMovement)
    }

    private fun fallbackDirectorAnalysis(
        userPrompt: String,
        styleName: String,
        cameraMovement: String
    ): ScriptAnalysis {
        val expanded = "Breathtaking cinematic masterwork of $userPrompt, styled in $styleName, enhanced with anamorphic depth of field, photorealistic volumetric lighting, ultra-fine 8k render textures, and dynamic camera choreography."
        val notes = "Arri Alexa 65 sensor, Panavision C-Series anamorphic prime lenses, Rembrandt directional chiaroscuro lighting, custom $styleName LUT color grade."

        val storyboard = listOf(
            StoryboardShot(
                shotNumber = 1,
                shotType = "Wide Establishing",
                visualDescription = "Expansive vista establishing the atmospheric scale of $userPrompt with sweeping environmental lighting.",
                cameraAngle = "Low angle crane ascending",
                lightingMood = "High-contrast rim lighting and ambient glow"
            ),
            StoryboardShot(
                shotNumber = 2,
                shotType = "Medium Action Tracking",
                visualDescription = "Dynamic motion tracking following focal elements across the frame using $cameraMovement trajectory.",
                cameraAngle = "Steady tracking lateral shot",
                lightingMood = "Volumetric haze and specular particle reflections"
            ),
            StoryboardShot(
                shotNumber = 3,
                shotType = "Dramatic Climax",
                visualDescription = "Intense focal convergence highlighting micro-details and kinetic energy in $styleName aesthetic.",
                cameraAngle = "Tight 85mm portrait telephoto",
                lightingMood = "Pulsating neon & golden hour accent sparks"
            ),
            StoryboardShot(
                shotNumber = 4,
                shotType = "Cinematic Outro",
                visualDescription = "Gradual resolution as camera pulls away into a harmonious silhouette composition.",
                cameraAngle = "Slow reverse dolly with depth blur",
                lightingMood = "Subtle twilight falloff and deep vignette"
            )
        )

        return ScriptAnalysis(
            expandedPrompt = expanded,
            cinematographyNotes = notes,
            recommendedFps = 30,
            suggestedAspect = "16:9",
            storyboard = storyboard
        )
    }
}
