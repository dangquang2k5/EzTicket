package huce.fit.myezticket

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// BẮT BUỘC phải có dòng "class EventViewModel" này thì MainActivity mới tìm thấy
class EventViewModel : ViewModel() {

    // Đây là nơi chứa danh sách sự kiện để báo cho giao diện vẽ lại
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    // Hàm này tự chạy ngay khi ViewModel được tạo ra
    init {
        loadEvents()
    }

    // Hàm để gọi dữ liệu
    fun loadEvents() {
        val db = Firebase.firestore
        db.collection("events")
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { document ->
                    document.toObject(Event::class.java).copy(id = document.id)
                }
                _events.value = list // Đẩy dữ liệu vào State để MainActivity nhận được
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE_ERROR", "Lỗi lấy dữ liệu: ${e.message}")
            }
    }
}