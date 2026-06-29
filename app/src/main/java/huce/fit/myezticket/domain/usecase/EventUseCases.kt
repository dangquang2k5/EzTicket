package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.domain.repository.EventRepository
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(private val repository: EventRepository) {
    operator fun invoke() = repository.getEventsFlow()
}

class RefreshEventsUseCase @Inject constructor(private val repository: EventRepository) {
    suspend operator fun invoke() = repository.syncEvents()
}

class GetRecentSearchesUseCase @Inject constructor(private val repository: EventRepository) {
    operator fun invoke() = repository.getRecentSearches()
}

class SaveRecentSearchesUseCase @Inject constructor(private val repository: EventRepository) {
    suspend operator fun invoke(searches: List<String>) = repository.saveRecentSearches(searches)
}
