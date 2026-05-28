package huce.fit.myezticket.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Event(
    var id: String = "",
    val name: String = "",
    val venueName: String = "",
    val address: String = "",
    val image_url: String = "",
    val description: String = "",
    val category: String = "",
    
    @get:PropertyName("isBanner")
    @set:PropertyName("isBanner")
    var isBanner: Boolean = false,

    @get:PropertyName("isHot")
    @set:PropertyName("isHot")
    var isHot: Boolean = false,

    @get:PropertyName("isVisible")
    @set:PropertyName("isVisible")
    var isVisible: Boolean = true,
    val status: String = "AVAILABLE",
    val organizerName: String = "",
    val organizerLogo: String = "",
    val schedules: List<EventSchedule> = emptyList()
){
    // TỰ ĐỘNG TÍNH GIÁ THẤP NHẤT - LOẠI TRỪ KHỎI FIREBASE ĐỂ TRÁNH CRASH
    @get:Exclude
    val minPrice: Long
        get() = schedules
            .flatMap { it.ticketTypes } // Gom tất cả vé của các ngày lại thành 1 danh sách
            .map { it.price }           // Chỉ lấy danh sách các mức giá
            .minOfOrNull { it } ?: 0    // Tìm mức giá thấp nhất, nếu không có thì trả về 0

    // Tự động lấy ngày hiển thị - LOẠI TRỪ KHỎI FIREBASE ĐỂ TRÁNH CRASH
    @get:Exclude
    val displayDate: String
        get() {
            val sortedDates = schedules.mapNotNull { it.date?.toDate() }.sorted()
            if (sortedDates.isEmpty()) return "Đang cập nhật..."

            val formatter = java.text.SimpleDateFormat("HH:mm, dd/MM/yyyy", java.util.Locale("vi", "VN"))
            val firstDate = formatter.format(sortedDates[0])

            return if (sortedDates.size > 1) "$firstDate và khác" else firstDate
        }
}

// Class chứa ngày giờ của suất diễn và kho vé của ngày hôm đó
data class EventSchedule(
    val date: Timestamp? = null,
    val ticketTypes: List<TicketType> = emptyList()
)

data class TicketType(
    val name: String = "",
    val price: Long = 0,
    val originalPrice: Long = 0,
    @get:PropertyName("isVisible")
    @set:PropertyName("isVisible")
    var isVisible: Boolean = true,
    val quantity: Int = 0
)
