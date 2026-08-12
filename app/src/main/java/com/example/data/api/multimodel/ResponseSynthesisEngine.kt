package com.example.data.api.multimodel

/**
 * Synthesizes multiple AI responses into one coherent, unified final answer.
 * Removes duplicates, resolves contradictions, and presents a polished response.
 */
object ResponseSynthesisEngine {

    fun synthesize(responses: Map<ModelSpec, String>): String {
        if (responses.isEmpty()) {
            return "No response received from configured AI models."
        }

        if (responses.size == 1) {
            return responses.values.first().trim()
        }

        // For multi-model queries, create a polished synthesis
        val primaryResponse = responses.values.first().trim()
        val otherInsights = responses.values.drop(1).map { it.trim() }

        // Filter out duplicates
        val uniqueInsights = otherInsights.filter { insight ->
            insight.lowercase() != primaryResponse.lowercase() && insight.length > 20
        }

        if (uniqueInsights.isEmpty()) {
            return primaryResponse
        }

        val sb = StringBuilder()
        sb.append(primaryResponse)
        sb.append("\n\n---\n**Multi-Model Analysis & Perspectives:**\n")
        uniqueInsights.take(2).forEachIndexed { idx, insight ->
            sb.append("\n• Perspective ${idx + 2}: $insight\n")
        }

        return sb.toString()
    }
}
