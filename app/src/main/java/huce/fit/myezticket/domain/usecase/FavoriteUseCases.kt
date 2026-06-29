package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.domain.repository.FavoriteRepository
import huce.fit.myezticket.domain.model.Event
import javax.inject.Inject

class GetFavoriteIdsUseCase @Inject constructor(private val repository: FavoriteRepository) {
    operator fun invoke(uid: String) = repository.getFavoriteIds(uid)
}



class RemoveFavoriteUseCase @Inject constructor(private val repository: FavoriteRepository) {
    suspend operator fun invoke(uid: String, eventId: String) = repository.removeFavorite(uid, eventId)
}

class AddFavoriteUseCase @Inject constructor(private val repository: FavoriteRepository) {
    suspend operator fun invoke(uid: String, eventId: String) = repository.addFavorite(uid, eventId)
}
