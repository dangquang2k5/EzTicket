package huce.fit.myezticket.domain.model



data class AppNotification(
    val id: String = "",
    val uid: String = "",
    val eventId: String = "",
    val eventName: String = "",
    val eventImageUrl: String = "",
    val title: String = "",
    val body: String = "",
    // "EVENT_3DAYS" | "EVENT_7DAYS" | "SALE_3DAYS" | "SALE_7DAYS" | "PAYMENT_SUCCESS"
    val type: String = "",
    val isRead: Boolean = false,
    
    val eventDateMillis: Long? = null,
    val createdAtMillis: Long? = null
)
