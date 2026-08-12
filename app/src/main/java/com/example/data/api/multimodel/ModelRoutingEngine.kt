package com.example.data.api.multimodel

import android.content.Context

/**
 * Intelligent task classification and model router.
 * Evaluates user input and routes to:
 * - Economical default model for simple tasks/conversations
 * - Specialized coding/reasoning models for complex tasks
 * - Multi-model parallel queries when research or multi-perspective analysis is requested
 */
class ModelRoutingEngine(private val registry: AIProviderRegistry) {

    fun classifyTask(query: String): TaskType {
        val q = query.lowercase().trim()

        if (q.contains(" analyze with all models") || q.contains("सभी models से analyze करो") || q.contains("compare answers")) {
            return TaskType.RESEARCH
        }

        if (q.contains("code") || q.contains("kotlin") || q.contains("architecture") || q.contains("function") || q.contains("debug") || q.contains("screen")) {
            return TaskType.CODING
        }

        if (q.contains("math") || q.contains("logic") || q.contains("calculate") || q.contains("theorem") || q.contains("strategy")) {
            return TaskType.COMPLEX
        }

        if (q.contains("image") || q.contains("photo") || q.contains("read text in picture") || q.contains("describe image")) {
            return TaskType.VISION
        }

        if (q.length < 30 || q.contains("hi") || q.contains("hello") || q.contains("time") || q.contains("weather") || q.contains(" How are you")) {
            return TaskType.SIMPLE
        }

        return TaskType.NORMAL
    }

    fun selectPrimaryModel(taskType: TaskType, defaultModelId: String): ModelSpec {
        val configured = registry.findModel(defaultModelId)
        if (configured != null && taskType == TaskType.SIMPLE) {
            return configured
        }

        // Return economical model for simple/normal tasks
        val enabled = registry.getEnabledProviders().flatMap { it.availableModels }
        return when (taskType) {
            TaskType.SIMPLE -> enabled.firstOrNull { it.isEconomical } ?: configured ?: fallbackModel()
            TaskType.CODING -> enabled.firstOrNull { it.capabilities.contains(ModelCapability.CODE_GEN) } ?: configured ?: fallbackModel()
            TaskType.COMPLEX, TaskType.RESEARCH -> enabled.firstOrNull { it.capabilities.contains(ModelCapability.REASONING) } ?: configured ?: fallbackModel()
            TaskType.VISION -> enabled.firstOrNull { it.capabilities.contains(ModelCapability.VISION_OCR) } ?: configured ?: fallbackModel()
            else -> configured ?: enabled.firstOrNull() ?: fallbackModel()
        }
    }

    fun selectParallelModels(taskType: TaskType, count: Int = 3): List<ModelSpec> {
        val allAvailable = registry.getEnabledProviders().flatMap { it.availableModels }.distinctBy { it.id }
        if (allAvailable.size <= 1) return allAvailable
        return allAvailable.take(count)
    }

    private fun fallbackModel() = ModelSpec(
        id = "gemini-1.5-flash",
        name = "Gemini 1.5 Flash",
        providerId = "gemini",
        capabilities = setOf(ModelCapability.FAST_CHAT),
        isEconomical = true
    )
}
