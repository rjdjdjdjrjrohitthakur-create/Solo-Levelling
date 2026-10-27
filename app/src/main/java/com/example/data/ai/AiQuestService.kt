package com.example.data.ai

import com.example.data.model.Difficulty
import com.example.data.model.Priority
import com.example.data.model.Quest
import com.example.data.model.QuestCategory
import com.example.data.model.QuestType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiQuestService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    suspend fun testApiKey(apiKey: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("API Key is empty"))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "Respond with 'OK'")
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    val errorBody = response.body?.string() ?: ""
                    Result.failure(Exception("HTTP ${response.code}: $errorBody"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateQuestsFromGoal(
        apiKey: String,
        goal: String,
        timeframe: String,
        intensity: String
    ): Result<List<Quest>> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            // Intelligent fallback generator
            return@withContext Result.success(generateOfflineFallbackQuests(goal, timeframe, intensity))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val promptText = """
                You are an expert RPG Game Master designing a progression questline for a player's real-life goal.
                User Goal: "$goal"
                Timeframe: "$timeframe"
                Intensity: "$intensity"

                Decompose this goal into 4 to 6 structured RPG quests that will take the user from start to victory.
                Categories available:
                MONEY_FINANCE, BUSINESS_CAREER, SKILLS_INTELLIGENCE, MINDSET_DISCIPLINE, BODY_FITNESS, SOCIAL_COMMUNICATION, CREATIVITY, PERSONAL_FREEDOM, CUSTOM.

                Difficulties: EASY (10 XP), MEDIUM (25 XP), HARD (50 XP), EPIC (100 XP), LEGENDARY (250 XP).
                Types: DAILY, WEEKLY, ONE_TIME, LONG_TERM.
                Priorities: LOW, MEDIUM, HIGH, CRITICAL.

                Return ONLY a valid JSON array matching this exact schema:
                [
                  {
                    "title": "Short punchy RPG quest title",
                    "description": "Clear actionable instructions with real life impact",
                    "category": "SKILLS_INTELLIGENCE",
                    "difficulty": "MEDIUM",
                    "xpReward": 25,
                    "type": "DAILY",
                    "priority": "HIGH",
                    "subtasks": ["Milestone 1", "Milestone 2"]
                  }
                ]
            """.trimIndent()

            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", promptText)
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // Fall back gracefully to local generation if quota exceeded or network issue
                    return@withContext Result.success(generateOfflineFallbackQuests(goal, timeframe, intensity))
                }

                val bodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(bodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text") ?: "[]"

                val parsedQuests = parseQuestsJson(text)
                if (parsedQuests.isEmpty()) {
                    Result.success(generateOfflineFallbackQuests(goal, timeframe, intensity))
                } else {
                    Result.success(parsedQuests)
                }
            }
        } catch (e: Exception) {
            // Never leave user empty handed
            Result.success(generateOfflineFallbackQuests(goal, timeframe, intensity))
        }
    }

    private fun parseQuestsJson(jsonText: String): List<Quest> {
        val list = mutableListOf<Quest>()
        try {
            val clean = jsonText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val array = JSONArray(clean)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val title = obj.optString("title", "Goal Quest ${i + 1}")
                val description = obj.optString("description", "")
                val categoryStr = obj.optString("category", "SKILLS_INTELLIGENCE")
                val difficultyStr = obj.optString("difficulty", "MEDIUM")
                val typeStr = obj.optString("type", "DAILY")
                val priorityStr = obj.optString("priority", "HIGH")
                val xp = obj.optInt("xpReward", 25)

                val category = runCatching { QuestCategory.valueOf(categoryStr) }.getOrDefault(QuestCategory.SKILLS_INTELLIGENCE)
                val difficulty = runCatching { Difficulty.valueOf(difficultyStr) }.getOrDefault(Difficulty.MEDIUM)
                val type = runCatching { QuestType.valueOf(typeStr) }.getOrDefault(QuestType.DAILY)
                val priority = runCatching { Priority.valueOf(priorityStr) }.getOrDefault(Priority.HIGH)

                val subtasksArray = obj.optJSONArray("subtasks")
                val subtasksList = mutableListOf<String>()
                if (subtasksArray != null) {
                    for (j in 0 until subtasksArray.length()) {
                        subtasksList.add(subtasksArray.getString(j))
                    }
                }

                val subtasksSerialized = if (subtasksList.isEmpty()) "[]"
                else "[" + subtasksList.mapIndexed { idx, s -> "${System.currentTimeMillis() + idx}::$s::false" }.joinToString(";;") + "]"

                list.add(
                    Quest(
                        title = title,
                        description = description,
                        category = category,
                        difficulty = difficulty,
                        xpReward = if (xp > 0) xp else difficulty.defaultXp,
                        type = type,
                        priority = priority,
                        subtasksJson = subtasksSerialized
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return list
    }

    fun generateOfflineFallbackQuests(
        goal: String,
        timeframe: String,
        intensity: String
    ): List<Quest> {
        val g = goal.lowercase()

        val isFitness = g.contains("fit") || g.contains("gym") || g.contains("muscle") || g.contains("weight") || g.contains("run") || g.contains("health")
        val isFinance = g.contains("money") || g.contains("earn") || g.contains("save") || g.contains("invest") || g.contains("business") || g.contains("wealth")
        val isDiscipline = g.contains("habit") || g.contains("wake") || g.contains("sleep") || g.contains("focus") || g.contains("quit")

        val mainCategory = when {
            isFitness -> QuestCategory.BODY_FITNESS
            isFinance -> QuestCategory.MONEY_FINANCE
            isDiscipline -> QuestCategory.MINDSET_DISCIPLINE
            else -> QuestCategory.SKILLS_INTELLIGENCE
        }

        return listOf(
            Quest(
                title = "Phase 1: Foundation Protocol for \"$goal\"",
                description = "Outline the daily routine, eliminate friction, and set up tracking systems.",
                category = mainCategory,
                difficulty = Difficulty.EASY,
                xpReward = 15,
                type = QuestType.ONE_TIME,
                priority = Priority.HIGH,
                subtasksJson = "[${System.currentTimeMillis()}::Audit current state::false;;${System.currentTimeMillis() + 1}::Set up environment & tools::false]"
            ),
            Quest(
                title = "Daily Focus Drill: Execute 45-Min Block",
                description = "Dedicate at least 45 minutes of undistracted execution directly advancing: $goal.",
                category = mainCategory,
                difficulty = Difficulty.MEDIUM,
                xpReward = 25,
                type = QuestType.DAILY,
                priority = Priority.HIGH,
                subtasksJson = "[${System.currentTimeMillis() + 2}::Zero phone notifications during session::false]"
            ),
            Quest(
                title = "Weekly Review & Performance Metrics",
                description = "Measure actual output, fix bottlenecks, and double down on what worked.",
                category = QuestCategory.MINDSET_DISCIPLINE,
                difficulty = Difficulty.HARD,
                xpReward = 50,
                type = QuestType.WEEKLY,
                priority = Priority.MEDIUM,
                subtasksJson = "[${System.currentTimeMillis() + 3}::Log progress metrics::false;;${System.currentTimeMillis() + 4}::Adjust next week targets::false]"
            ),
            Quest(
                title = "Ascension Milestone: Conquer \"$goal\"",
                description = "Final master achievement: Complete all deliverables and seal your victory in real life.",
                category = mainCategory,
                difficulty = Difficulty.LEGENDARY,
                xpReward = 250,
                type = QuestType.LONG_TERM,
                priority = Priority.CRITICAL,
                subtasksJson = "[${System.currentTimeMillis() + 5}::Verify target criteria met::false;;${System.currentTimeMillis() + 6}::Document lessons learned::false]"
            )
        )
    }
}
