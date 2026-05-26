package huce.fit.myezticket.domain.repository

import huce.fit.myezticket.domain.model.Event

interface EventRepository {
    suspend fun getEvents(): List<Event>
}
