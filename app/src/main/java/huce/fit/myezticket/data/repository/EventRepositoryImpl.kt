package huce.fit.myezticket.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.repository.EventRepository
import huce.fit.myezticket.data.local.dao.EventDao
import huce.fit.myezticket.data.local.entity.EventEntity
import huce.fit.myezticket.data.model.EventDto
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val eventDao: EventDao,
    private val dataStore: DataStore<Preferences>
) : EventRepository {

    override fun getEventsFlow(): Flow<List<Event>> {
        return eventDao.getEventsFlow().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun syncEvents(): Result<Unit> {
        return try {
            val snapshot = db.collection("events").get().await()
            val dtos = snapshot.map { document ->
                document.toObject(EventDto::class.java).apply { id = document.id }
            }
            val entities = dtos.map { EventEntity.fromDto(it) }
            eventDao.replaceEvents(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("EventRepositoryImpl", "Lỗi đồng bộ dữ liệu sự kiện: ${e.message}")
            Result.failure(e)
        }
    }

    override fun getRecentSearches(): Flow<List<String>> {
        val key = stringPreferencesKey("recent_searches_list")
        return dataStore.data.map { preferences ->
            val orderedSearches = preferences[key] ?: ""
            orderedSearches.split("|").filter { it.isNotBlank() }
        }
    }

    override suspend fun saveRecentSearches(searches: List<String>) {
        val key = stringPreferencesKey("recent_searches_list")
        dataStore.edit { preferences ->
            preferences[key] = searches.joinToString("|")
        }
    }
}
