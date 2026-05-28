package huce.fit.myezticket.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class AppNotification(
    val id: String = "",
    val uid: String = "",
    val eventId: String = "",
    val eventName: String = "",
    val eventImageUrl: String = "",
    val title: String = "",
    val body: String = "",
    // "EVENT_3DAYS" | "EVENT_7DAYS" | "SALE_3DAYS" | "SALE_7DAYS"
    val type: String = "",
    
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,
    
    val eventDate: Timestamp? = null,
    val createdAt: Timestamp? = null
)
