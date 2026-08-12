package com.example.data.api.multimodel

import android.content.Context
import com.example.data.api.AiRepository
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.preferences.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class MultiModelOrchestrator(private val context: Context) {

    val registry = AIProviderRegistry(context)
    val router = ModelRoutingEngine(registry)
    private val aiRepository = AiRepository(context)

    suspend fun processQuery(
        query: String,
        history: List<ChatMessageEntity> = emptyList(),
        userSettings: UserSettings
    ): String = withContext(Dispatchers.IO) {
        val taskType = router.classifyTask(query)

        // If user explicitly asks for multi-model analysis or task is research
        val isExplicitMultiModel = query.lowercase().contains("all models") ||
                query.contains("सभी models से analyze करो") ||
                taskType == TaskType.RESEARCH

        if (isExplicitMultiModel) {
            val candidateModels = router.selectParallelModels(taskType, count = 3)
            val resultsMap = mutableMapOf<ModelSpec, String>()

            val jobs = candidateModels.map { modelSpec ->
                async<Pair<ModelSpec, String>?> {
                    try {
                        val resp = aiRepository.generateAssistantResponse(
                            prompt = query,
                            history = history,
                            memories = emptyList(),
                            userSettings = userSettings.copy(selectedModel = modelSpec.id)
                        )
                        modelSpec to resp
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            val completed = jobs.awaitAll().filterNotNull()
            completed.forEach { item ->
                if (item.second.isNotBlank()) {
                    resultsMap[item.first] = item.second
                }
            }

            if (resultsMap.isNotEmpty()) {
                return@withContext ResponseSynthesisEngine.synthesize(resultsMap)
            }
        }

        // Single Economical/Specialized Model Execution
        val selectedModel = router.selectPrimaryModel(taskType, userSettings.selectedModel)
        try {
            return@withContext aiRepository.generateAssistantResponse(
                prompt = query,
                history = history,
                memories = emptyList(),
                userSettings = userSettings.copy(selectedModel = selectedModel.id)
            )
        } catch (e: Exception) {
            // Fallback to default
            return@withContext aiRepository.generateAssistantResponse(
                prompt = query,
                history = history,
                memories = emptyList(),
                userSettings = userSettings
            )
        }
    }
}
