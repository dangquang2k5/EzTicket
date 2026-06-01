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
            val firstSchedule = schedules.minByOrNull { it.date?.toDate()?.time ?: Long.MAX_VALUE }
            val startDate = firstSchedule?.date?.toDate()
            val endDate = firstSchedule?.endDate?.toDate()
            
            if (startDate == null) return "Đang cập nhật..."
            
            val timeFmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale("vi", "VN"))
            val dateFmt = java.text.SimpleDateFormat("dd 'Tháng' MM, yyyy", java.util.Locale("vi", "VN"))
            
            val startStr = timeFmt.format(startDate)
            val endStr = endDate?.let { timeFmt.format(it) }
            val dateStr = dateFmt.format(startDate)
            
            val firstDisplay = if (endStr != null) {
                "$startStr - $endStr, $dateStr"
            } else {
                "$startStr, $dateStr"
            }
            
            return if (schedules.size > 1) "$firstDisplay và khác" else firstDisplay
        }
}

// Class chứa ngày giờ của suất diễn và kho vé của ngày hôm đó
data class EventSchedule(
    val date: Timestamp? = null,
    val endDate: Timestamp? = null,
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
