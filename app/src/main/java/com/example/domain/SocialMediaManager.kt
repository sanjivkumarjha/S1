package com.example.domain

import android.content.Context
import com.example.data.api.AiRepository
import com.example.data.local.AppDatabase
import com.example.data.local.entities.SocialMediaPostEntity
import com.example.data.preferences.UserSettings
import kotlinx.coroutines.flow.Flow

class SocialMediaManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val postDao = db.socialMediaPostDao()
    private val aiRepo = AiRepository(context)

    val posts: Flow<List<SocialMediaPostEntity>> = postDao.getAllPosts()

    suspend fun generateCaptionAndHashtags(
        platform: String,
        topic: String,
        userSettings: UserSettings
    ): String {
        val prompt = "Generate an engaging $platform caption and 8 trending hashtags for topic: '$topic'. Language: ${if (userSettings.languageCode == "hi") "Hindi/Hinglish" else "English"}."
        return aiRepo.generateAssistantResponse(
            prompt = prompt,
            history = emptyList(),
            memories = emptyList(),
            userSettings = userSettings
        )
    }

    suspend fun savePostDraft(platform: String, caption: String, hashtags: String): Long {
        return postDao.insertPost(
            SocialMediaPostEntity(
                platform = platform,
                caption = caption,
                hashtags = hashtags,
                scheduledTimeMillis = System.currentTimeMillis() + 86400000L
            )
        )
    }

    suspend fun deletePost(id: Long) {
        postDao.deletePostById(id)
    }
}
