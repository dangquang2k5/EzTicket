package huce.fit.myezticket.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import huce.fit.myezticket.domain.model.PurchasedTicket


@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey
    val id: String,
    val uid: String,
    val eventId: String,
    val eventName: String,
    val imageUrl: String,
    val orderCode: String,
    val ticketCode: String,
    val eventDateMillis: Long?,
    val location: String,
    val ticketTypeName: String,
    val quantity: Int,
    val unitPrice: Long,
    val totalPrice: Long,
    val status: String,
    val customerPhone: String,
    val paymentMethod: String,
    val scheduleIndex: Int,
    val createdAtMillis: Long?,
    val expiresAtMillis: Long?,
    val eventEndDateMillis: Long?
) {
    fun toPurchasedTicket(): PurchasedTicket {
        return PurchasedTicket(
            id = id,
            eventId = eventId,
            eventName = eventName,
            imageUrl = imageUrl,
            orderCode = orderCode,
            ticketCode = ticketCode,
            eventDateMillis = eventDateMillis,
            location = location,
            ticketTypeName = ticketTypeName,
            quantity = quantity,
            unitPrice = unitPrice,
            totalPrice = totalPrice,
            status = status,
            customerPhone = customerPhone,
            paymentMethod = paymentMethod,
            scheduleIndex = scheduleIndex,
            createdAtMillis = createdAtMillis,
            expiresAtMillis = expiresAtMillis,
            eventEndDateMillis = eventEndDateMillis
        )
    }

    companion object {
        fun fromPurchasedTicket(ticket: PurchasedTicket, uid: String): TicketEntity {
            return TicketEntity(
                id = ticket.id,
                uid = uid,
                eventId = ticket.eventId,
                eventName = ticket.eventName,
                imageUrl = ticket.imageUrl,
                orderCode = ticket.orderCode,
                ticketCode = ticket.ticketCode,
                eventDateMillis = ticket.eventDateMillis,
                location = ticket.location,
                ticketTypeName = ticket.ticketTypeName,
                quantity = ticket.quantity,
                unitPrice = ticket.unitPrice,
                totalPrice = ticket.totalPrice,
                status = ticket.status,
                customerPhone = ticket.customerPhone,
                paymentMethod = ticket.paymentMethod,
                scheduleIndex = ticket.scheduleIndex,
                createdAtMillis = ticket.createdAtMillis,
                expiresAtMillis = ticket.expiresAtMillis,
                eventEndDateMillis = ticket.eventEndDateMillis
            )
        }
    }
}
