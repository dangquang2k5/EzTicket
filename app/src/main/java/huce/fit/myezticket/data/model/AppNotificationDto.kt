package huce.fit.myezticket.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import huce.fit.myezticket.domain.model.AppNotification

data class AppNotificationDto(
    val id: String = "",
    val uid: String = "",
    val eventId: String = "",
    val eventName: String = "",
    val eventImageUrl: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    
    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,
    
    val eventDate: Timestamp? = null,
    val createdAt: Timestamp? = null
) {
    fun toDomainModel(): AppNotification {
        return AppNotification(
            id = id,
            uid = uid,
            eventId = eventId,
            eventName = eventName,
            eventImageUrl = eventImageUrl,
            title = title,
            body = body,
            type = type,
            isRead = isRead,
            eventDateMillis = eventDate?.toDate()?.time,
            createdAtMillis = createdAt?.toDate()?.time
        )
    }
}

fun AppNotification.toDto(): AppNotificationDto {
    return AppNotificationDto(
        id = id,
        uid = uid,
        eventId = eventId,
        eventName = eventName,
        eventImageUrl = eventImageUrl,
        title = title,
        body = body,
        type = type,
        isRead = isRead,
        eventDate = eventDateMillis?.let { Timestamp(java.util.Date(it)) },
        createdAt = createdAtMillis?.let { Timestamp(java.util.Date(it)) }
    )
}
