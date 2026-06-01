package huce.fit.myezticket.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import huce.fit.myezticket.domain.repository.FavoriteRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : FavoriteRepository {

    override fun getFavoriteIds(uid: String): Flow<Set<String>> = callbackFlow {
        val ref = db.collection("users").document(uid).collection("favorites")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val ids = snapshot?.documents?.map { it.id }?.toSet() ?: emptySet()
            trySend(ids)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun addFavorite(uid: String, eventId: String) {
        db.collection("users")
            .document(uid)
            .collection("favorites")
            .document(eventId)
            .set(mapOf("eventId" to eventId, "addedAt" to com.google.firebase.Timestamp.now()))
            .await()
    }

    override suspend fun removeFavorite(uid: String, eventId: String) {
        db.collection("users")
            .document(uid)
            .collection("favorites")
            .document(eventId)
            .delete()
            .await()
    }
}
