package huce.fit.myezticket.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.repository.EventRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : EventRepository {

    override suspend fun getEvents(): List<Event> {
        return try {
            val snapshot = db.collection("events").get().await()
            snapshot.map { document ->
                document.toObject(Event::class.java).copy(id = document.id)
            }
        } catch (e: Exception) {
            Log.e("EventRepositoryImpl", "Lỗi lấy dữ liệu từ Firebase: ${e.message}")
            emptyList()
        }
    }
}
