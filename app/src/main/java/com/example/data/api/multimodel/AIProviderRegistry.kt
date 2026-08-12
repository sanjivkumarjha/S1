package com.example.data.api.multimodel

import android.content.Context
import java.util.concurrent.ConcurrentHashMap

/**
 * Registry holding up to 100+ dynamic AI Providers and Models.
 */
class AIProviderRegistry(private val context: Context) {

    private val providers = ConcurrentHashMap<String, ProviderConfig>()

    init {
        // Register default built-in providers
        registerDefaultProviders()
    }

    private fun registerDefaultProviders() {
        // Built-in Gemini
        providers["gemini"] = ProviderConfig(
            providerId = "gemini",
            displayName = "Google Gemini AI",
            baseUrl = "https://generativelanguage.googleapis.com/",
            apiKey = "",
            priority = 1,
            availableModels = listOf(
                ModelSpec("gemini-1.5-flash", "Gemini 1.5 Flash (Economical Default)", "gemini", setOf(ModelCapability.FAST_CHAT, ModelCapability.VISION_OCR), isEconomical = true),
                ModelSpec("gemini-1.5-pro", "Gemini 1.5 Pro (Reasoning & Coding)", "gemini", setOf(ModelCapability.REASONING, ModelCapability.CODE_GEN, ModelCapability.LONG_CONTEXT), isEconomical = false),
                ModelSpec("gemini-2.0-flash", "Gemini 2.0 Flash (Fast Reasoning)", "gemini", setOf(ModelCapability.FAST_CHAT, ModelCapability.REASONING), isEconomical = true)
            )
        )

        // OpenRouter
        providers["openrouter"] = ProviderConfig(
            providerId = "openrouter",
            displayName = "OpenRouter Multi-Model Hub",
            baseUrl = "https://openrouter.ai/api/v1/",
            apiKey = "",
            priority = 2,
            availableModels = listOf(
                ModelSpec("meta-llama/llama-3.3-70b-instruct", "Llama 3.3 70B", "openrouter", setOf(ModelCapability.REASONING, ModelCapability.CODE_GEN), isEconomical = true),
                ModelSpec("deepseek/deepseek-r1", "DeepSeek R1 Reasoning", "openrouter", setOf(ModelCapability.REASONING, ModelCapability.RESEARCH_SYNTHESIS), isEconomical = false),
                ModelSpec("anthropic/claude-3.5-sonnet", "Claude 3.5 Sonnet", "openrouter", setOf(ModelCapability.CODE_GEN, ModelCapability.REASONING), isEconomical = false)
            )
        )

        // Grok
        providers["grok"] = ProviderConfig(
            providerId = "grok",
            displayName = "xAI Grok",
            baseUrl = "https://api.x.ai/v1/",
            apiKey = "",
            priority = 3,
            availableModels = listOf(
                ModelSpec("grok-2-latest", "Grok 2", "grok", setOf(ModelCapability.FAST_CHAT, ModelCapability.REASONING), isEconomical = true)
            )
        )

        // NVIDIA
        providers["nvidia"] = ProviderConfig(
            providerId = "nvidia",
            displayName = "NVIDIA Nim AI",
            baseUrl = "https://integrate.api.nvidia.com/v1/",
            apiKey = "",
            priority = 4,
            availableModels = listOf(
                ModelSpec("nvidia/llama-3.1-nemotron-70b", "Nemotron 70B", "nvidia", setOf(ModelCapability.REASONING), isEconomical = true)
            )
        )
    }

    fun registerProvider(config: ProviderConfig) {
        providers[config.providerId] = config
    }

    fun getProvider(providerId: String): ProviderConfig? = providers[providerId]

    fun getAllProviders(): List<ProviderConfig> = providers.values.toList().sortedBy { it.priority }

    fun getEnabledProviders(): List<ProviderConfig> = providers.values.filter { it.isEnabled }.sortedBy { it.priority }

    fun findModel(modelId: String): ModelSpec? {
        providers.values.forEach { provider ->
            provider.availableModels.find { it.id == modelId }?.let { return it }
        }
        return null
    }
}
