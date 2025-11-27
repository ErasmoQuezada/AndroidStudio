package com.example.amiot.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.amiot.ui.main.NewsItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

private val Context.newsDataStore: DataStore<Preferences> by preferencesDataStore(name = "news_prefs")

@Serializable
data class NewsItemSerializable(
    val id: String,
    val title: String,
    val content: String,
    val fullContent: String,
    val date: String,
    val category: String,
    val isUserCreated: Boolean = false
)

class NewsRepository(private val context: Context) {
    private val userNewsKey = stringSetPreferencesKey("user_news")
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun addNews(news: NewsItemSerializable): Result<Unit> {
        return try {
            val currentNews = getAllUserNews().toMutableList()
            currentNews.add(news)
            saveUserNews(currentNews)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUserNews(): List<NewsItemSerializable> {
        return try {
            val preferences = context.newsDataStore.data.first()
            val newsSet = preferences[userNewsKey] ?: emptySet()
            newsSet.map { jsonString ->
                json.decodeFromString<NewsItemSerializable>(jsonString)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val userNews: Flow<List<NewsItemSerializable>> = context.newsDataStore.data.map { preferences ->
        val newsSet = preferences[userNewsKey] ?: emptySet()
        newsSet.map { jsonString ->
            try {
                json.decodeFromString<NewsItemSerializable>(jsonString)
            } catch (e: Exception) {
                null
            }
        }.filterNotNull()
    }

    private suspend fun saveUserNews(newsList: List<NewsItemSerializable>) {
        context.newsDataStore.edit { preferences ->
            val newsSet = newsList.map { news ->
                json.encodeToString(news)
            }.toSet()
            preferences[userNewsKey] = newsSet
        }
    }

    suspend fun deleteNews(newsId: String): Result<Unit> {
        return try {
            val currentNews = getAllUserNews().filter { it.id != newsId }
            saveUserNews(currentNews)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

