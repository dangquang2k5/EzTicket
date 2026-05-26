package huce.fit.myezticket.domain.repository

import com.google.firebase.firestore.ListenerRegistration
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.model.PurchasedTicket

interface TicketRepository {
    fun listenPurchasedTickets(
        onTicketsChanged: (List<PurchasedTicket>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration

    suspend fun createPurchasedTickets(
        event: Event,
        scheduleIndex: Int,
        selectedTickets: Map<String, Int>,
        phoneNumber: String,
        orderCode: String,
        paymentMethod: String
    ): Result<Unit>
}
