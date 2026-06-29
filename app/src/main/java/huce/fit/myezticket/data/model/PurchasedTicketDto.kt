package huce.fit.myezticket.data.model

import com.google.firebase.Timestamp
import huce.fit.myezticket.domain.model.PurchasedTicket

data class PurchasedTicketDto(
    var id: String = "",
    val uid: String = "",
    val eventId: String = "",
    val eventName: String = "",
    val imageUrl: String = "",
    val orderCode: String = "",
    val ticketCode: String = "",
    val eventDate: Timestamp? = null,
    val location: String = "",
    val ticketTypeName: String = "",
    val quantity: Int = 1,
    val unitPrice: Long = 0,
    val totalPrice: Long = 0,
    val status: String = "Thành công",
    val customerPhone: String = "",
    val paymentMethod: String = "",
    val scheduleIndex: Int = 0,
    val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    val eventEndDate: Timestamp? = null
) {
    fun toDomainModel(): PurchasedTicket {
        return PurchasedTicket(
            id = id,
            eventId = eventId,
            eventName = eventName,
            imageUrl = imageUrl,
            orderCode = orderCode,
            ticketCode = ticketCode,
            eventDateMillis = eventDate?.toDate()?.time,
            location = location,
            ticketTypeName = ticketTypeName,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = totalPrice,
            status = status,
            customerPhone = customerPhone,
            paymentMethod = paymentMethod,
            scheduleIndex = scheduleIndex,
            createdAtMillis = createdAt?.toDate()?.time,
            expiresAtMillis = expiresAt?.toDate()?.time,
            eventEndDateMillis = eventEndDate?.toDate()?.time
        )
    }
}
