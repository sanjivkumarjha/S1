package com.example.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.local.entities.FavoriteSongEntity
import kotlinx.coroutines.flow.Flow

class MediaControlManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val favoriteSongDao = db.favoriteSongDao()

    val favoriteSongs: Flow<List<FavoriteSongEntity>> = favoriteSongDao.getAllFavoriteSongs()

    suspend fun addFavoriteSong(songName: String, artist: String = "", source: String = "youtube") {
        favoriteSongDao.insertFavoriteSong(
            FavoriteSongEntity(
                songName = songName,
                artist = artist,
                source = source
            )
        )
    }

    suspend fun removeFavoriteSong(songName: String) {
        favoriteSongDao.deleteFavoriteSongByName(songName)
    }

    fun playOnYouTube(query: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query)))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun playOnLocalMusicPlayer(query: String = ""): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse("content://media/external/audio/media"), "audio/*")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Fallback to general music intent
            try {
                val fallbackIntent = Intent(android.provider.MediaStore.INTENT_ACTION_MUSIC_PLAYER)
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallbackIntent)
                true
            } catch (ex: Exception) {
                false
            }
        }
    }

    fun processMediaCommand(command: String): String {
        val lower = command.lowercase()
        return when {
            lower.contains("youtube") -> {
                val song = command.replace("YouTube पर", "", ignoreCase = true)
                    .replace("youtube पर", "", ignoreCase = true)
                    .replace("चलाओ", "", ignoreCase = true)
                    .replace("song", "", ignoreCase = true)
                    .replace("गाना", "", ignoreCase = true).trim()
                playOnYouTube(if (song.isNotBlank()) song else "Hindi Songs")
                "YouTube पर $song प्ले कर रही हूँ।"
            }
            lower.contains("local music") || lower.contains("phone के music player") || lower.contains("phone वाले music player") -> {
                playOnLocalMusicPlayer()
                "Phone के music player में गाना play कर रही हूँ।"
            }
            else -> {
                // Default smart routing -> YouTube
                val song = command.replace("चलाओ", "", ignoreCase = true)
                    .replace("song", "", ignoreCase = true)
                    .replace("गाना", "", ignoreCase = true).trim()
                playOnYouTube(if (song.isNotBlank()) song else "Trending Songs")
                "आपके लिए $song प्ले कर रही हूँ।"
            }
        }
    }
}
