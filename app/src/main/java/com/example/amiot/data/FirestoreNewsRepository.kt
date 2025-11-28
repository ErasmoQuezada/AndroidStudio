package com.example.amiot.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.*

data class NewsItemFirestore(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val fullContent: String = "",
    val date: String = "",
    val category: String = "",
    val isUserCreated: Boolean = false,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

class FirestoreNewsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val newsCollection = db.collection("news")
    
    private val _userNews = MutableStateFlow<List<NewsItemFirestore>>(emptyList())
    val userNews: Flow<List<NewsItemFirestore>> = _userNews.asStateFlow()

    suspend fun addNews(
        userId: String,
        title: String,
        shortContent: String,
        fullContent: String,
        category: String,
        date: String
    ): Result<Unit> {
        return try {
            val news = hashMapOf(
                "title" to title,
                "content" to shortContent,
                "fullContent" to fullContent,
                "date" to date,
                "category" to category,
                "isUserCreated" to true,
                "userId" to userId,
                "createdAt" to System.currentTimeMillis()
            )
            
            newsCollection.add(news).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllUserNews(userId: String): List<NewsItemFirestore> {
        return try {
            val snapshot = newsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.map { doc ->
                NewsItemFirestore(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    content = doc.getString("content") ?: "",
                    fullContent = doc.getString("fullContent") ?: "",
                    date = doc.getString("date") ?: "",
                    category = doc.getString("category") ?: "",
                    isUserCreated = doc.getBoolean("isUserCreated") ?: false,
                    userId = doc.getString("userId") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getAllNews(): List<NewsItemFirestore> {
        return try {
            val snapshot = newsCollection
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.map { doc ->
                NewsItemFirestore(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    content = doc.getString("content") ?: "",
                    fullContent = doc.getString("fullContent") ?: "",
                    date = doc.getString("date") ?: "",
                    category = doc.getString("category") ?: "",
                    isUserCreated = doc.getBoolean("isUserCreated") ?: false,
                    userId = doc.getString("userId") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteNews(newsId: String): Result<Unit> {
        return try {
            newsCollection.document(newsId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun listenToUserNews(userId: String): Flow<List<NewsItemFirestore>> {
        val flow = MutableStateFlow<List<NewsItemFirestore>>(emptyList())
        
        newsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    flow.value = emptyList()
                    return@addSnapshotListener
                }
                
                val newsList = snapshot?.documents?.map { doc ->
                    NewsItemFirestore(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        content = doc.getString("content") ?: "",
                        fullContent = doc.getString("fullContent") ?: "",
                        date = doc.getString("date") ?: "",
                        category = doc.getString("category") ?: "",
                        isUserCreated = doc.getBoolean("isUserCreated") ?: false,
                        userId = doc.getString("userId") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                } ?: emptyList()
                
                flow.value = newsList
            }
        
        return flow.asStateFlow()
    }
}

