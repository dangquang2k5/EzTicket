package huce.fit.myezticket.domain.model



data class Event(
    val id: String = "",
    val name: String = "",
    val venueName: String = "",
    val address: String = "",
    val image_url: String = "",
    val description: String = "",
    val category: String = "",
    val isBanner: Boolean = false,
    val isHot: Boolean = false,
    val isVisible: Boolean = true,
    val status: String = "AVAILABLE",
    val organizerName: String = "",
    val organizerLogo: String = "",
    val schedules: List<EventSchedule> = emptyList()
){
    // TỰ ĐỘNG TÍNH GIÁ THẤP NHẤT - LOẠI TRỪ KHỎI FIREBASE ĐỂ TRÁNH CRASH
    val minPrice: Long
        get() = schedules
            .flatMap { it.ticketTypes } // Gom tất cả vé của các ngày lại thành 1 danh sách
            .filter { it.isVisible }    // Lọc chỉ lấy những loại vé đang hiển thị
            .map { it.price }           // Chỉ lấy danh sách các mức giá
            .minOfOrNull { it } ?: 0    // Tìm mức giá thấp nhất, nếu không có thì trả về 0

    // Tự động lấy ngày hiển thị - LOẠI TRỪ KHỎI FIREBASE ĐỂ TRÁNH CRASH
    val displayDate: String
        get() {
            val visibleSchedules = schedules.filter { s -> s.ticketTypes.any { t -> t.isVisible } }
            val firstSchedule = visibleSchedules.minByOrNull { it.dateMillis ?: Long.MAX_VALUE }
            val startDateMillis = firstSchedule?.dateMillis
            val endDateMillis = firstSchedule?.endDateMillis
            
            if (startDateMillis == null) return "Đang cập nhật..."
            
            val zoneId = java.time.ZoneId.of("Asia/Ho_Chi_Minh")
            val viLocale = java.util.Locale("vi", "VN")
            
            val startInstant = java.time.Instant.ofEpochMilli(startDateMillis)
            val startZdt = java.time.ZonedDateTime.ofInstant(startInstant, zoneId)
            
            val timeFmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm", viLocale)
            val dateFmt = java.time.format.DateTimeFormatter.ofPattern("dd 'Tháng' MM, yyyy", viLocale)
            
            val startStr = startZdt.format(timeFmt)
            val endStr = endDateMillis?.let {
                val endInstant = java.time.Instant.ofEpochMilli(it)
                val endZdt = java.time.ZonedDateTime.ofInstant(endInstant, zoneId)
                endZdt.format(timeFmt)
            }
            val dateStr = startZdt.format(dateFmt)
            
            val firstDisplay = if (endStr != null) {
                "$startStr - $endStr, $dateStr"
            } else {
                "$startStr, $dateStr"
            }
            
            return if (visibleSchedules.size > 1) "$firstDisplay và khác" else firstDisplay
        }

    // Tự động kiểm tra xem sự kiện có hết vé không - dựa trên các vé đang hiển thị của các suất diễn chưa diễn ra
    val isSoldOut: Boolean
        get() {
            if (status == "SOLD_OUT") return true
            val visibleSchedules = schedules.filter { s -> s.ticketTypes.any { t -> t.isVisible } }
            if (schedules.isNotEmpty() && visibleSchedules.isEmpty()) return true
            if (visibleSchedules.isNotEmpty()) {
                val now = System.currentTimeMillis()
                // Lọc các lịch diễn chưa diễn ra hoặc chưa kết thúc (ngày kết thúc/ngày bắt đầu >= hiện tại)
                val futureSchedules = visibleSchedules.filter { schedule ->
                    val endTime = schedule.endDateMillis ?: schedule.dateMillis
                    endTime == null || endTime >= now
                }
                // Nếu không còn lịch diễn nào ở tương lai, coi như đã hết vé
                if (futureSchedules.isEmpty()) return true

                // Nếu tất cả lịch diễn ở tương lai đều hết vé
                return futureSchedules.all { schedule ->
                    val visibleTickets = schedule.ticketTypes.filter { it.isVisible }
                    visibleTickets.isNotEmpty() && visibleTickets.all { it.quantity <= 0 }
                }
            }
            return false
        }
}

// Class chứa ngày giờ của suất diễn và kho vé của ngày hôm đó
data class EventSchedule(
    val dateMillis: Long? = null,
    val endDateMillis: Long? = null,
    val ticketTypes: List<TicketType> = emptyList()
)

data class TicketType(
    val name: String = "",
    val price: Long = 0,
    val isVisible: Boolean = true,
    val quantity: Int = 0
)
