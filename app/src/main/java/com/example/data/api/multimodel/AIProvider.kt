package com.example.data.api.multimodel

enum class TaskType {
    SIMPLE,        // Routine chat, greetings, quick info
    NORMAL,        // General Q&A, summaries
    COMPLEX,       // Deep reasoning, multi-step math/logic
    CODING,        // Architecture, software development, debugging
    VISION,        // Image recognition, visual context
    RESEARCH,      // Deep multi-angle inquiry, literature
    MULTIMODAL,    // Video/Audio + Text combined
    SPECIALIZED    // Security, Smart Home, Custom tool calling
}

enum class ModelCapability {
    FAST_CHAT, REASONING, CODE_GEN, VISION_OCR, LONG_CONTEXT, CREATIVE_PROMPT, RESEARCH_SYNTHESIS
}

data class ModelSpec(
    val id: String,
    val name: String,
    val providerId: String,
    val capabilities: Set<ModelCapability>,
    val contextWindowTokens: Int = 128000,
    val isEconomical: Boolean = true,
    val costMultiplier: Float = 1.0f
)

data class ProviderConfig(
    val providerId: String,
    val displayName: String,
    val baseUrl: String,
    val apiKey: String,
    val isEnabled: Boolean = true,
    val priority: Int = 1,
    val availableModels: List<ModelSpec>
)
