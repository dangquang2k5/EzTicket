package huce.fit.myezticket.domain.repository

import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.Timestamp
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.model.PurchasedTicket

interface TicketRepository {
    fun listenPurchasedTickets(
        onTicketsChanged: (List<PurchasedTicket>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration

    suspend fun createPendingTickets(
        event: Event,
        scheduleIndex: Int,
        selectedTickets: Map<String, Int>,
        orderCode: String,
        expiresAt: Timestamp
    ): Result<Unit>

    suspend fun completePendingTickets(
        orderCode: String,
        phoneNumber: String,
        paymentMethod: String
    ): Result<Unit>

    suspend fun cancelPendingTickets(orderCode: String): Result<Unit>

    suspend fun cancelExpiredPendingTickets(): Result<Unit>
}
