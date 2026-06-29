package huce.fit.myezticket.domain.repository

import huce.fit.myezticket.domain.model.Event

import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getEventsFlow(): Flow<List<Event>>
    suspend fun syncEvents(): Result<Unit>
    
    fun getRecentSearches(): Flow<List<String>>
    suspend fun saveRecentSearches(searches: List<String>)
}
