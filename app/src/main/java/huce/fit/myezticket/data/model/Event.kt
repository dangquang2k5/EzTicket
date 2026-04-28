package huce.fit.myezticket.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Event(
    // 1. id này dùng để lưu cái mã Document ID loằng ngoằng của Firebase
    var id: String = "",

    // 2. Các trường này PHẢI trùng tên 100% với trên Firebase Console
    val name: String = "",
    val price: Long = 0,         // Trên Firebase là Number thì ở đây là Long
    val location: String = "",
    val image_url: String = "", // Link ảnh poster
    val date: Timestamp? = null, // Kiểu thời gian bạn vừa hỏi lúc nãy
    val description: String = "",
    val category: String = "",
    @get:PropertyName("isBanner")
    @set:PropertyName("isBanner")
    var isBanner: Boolean = false,
    val ticketTypes: List<TicketType> = emptyList() // Danh sách các hạng vé
)

// Thêm class bổ trợ để quản lý loại vé
data class TicketType(
    val name: String = "",
    val price: Long = 0,
    val quantity: Int = 0 // Tự động báo hết vé nếu quantity = 0
)