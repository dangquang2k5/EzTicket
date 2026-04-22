package huce.fit.myezticket.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import huce.fit.myezticket.data.model.Event
import kotlinx.coroutines.tasks.await

class EventRepository {
    // Khởi tạo Firestore
    private val db = FirebaseFirestore.getInstance()

    // Hàm suspend chạy ngầm để lấy dữ liệu
    suspend fun getEvents(): List<Event> {
        return try {
            // Lấy toàn bộ collection "events", dùng .await() để chờ dữ liệu về
            val snapshot = db.collection("events").get().await()

            // Chuyển đổi dữ liệu thô thành danh sách các đối tượng Event
            snapshot.map { document ->
                document.toObject(Event::class.java).copy(id = document.id)
            }
        } catch (e: Exception) {
            Log.e("EventRepository", "Lỗi lấy dữ liệu từ Firebase: ${e.message}")
            emptyList() // Nếu lỗi mạng thì trả về danh sách rỗng để app không bị crash
        }
    }
}