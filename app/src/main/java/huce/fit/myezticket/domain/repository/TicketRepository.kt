package huce.fit.myezticket.domain.repository


import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.model.PurchasedTicket
import kotlinx.coroutines.flow.Flow

interface TicketRepository {
    fun getPurchasedTickets(): Flow<List<PurchasedTicket>>
    
    fun startTicketSync(onError: (Exception) -> Unit): () -> Unit

    suspend fun createPendingTickets(
        event: Event,
        scheduleIndex: Int,
        selectedTickets: Map<String, Int>,
        orderCode: String,
        expiresAtMillis: Long
    ): Result<Unit>

    suspend fun completePendingTickets(
        orderCode: String,
        phoneNumber: String,
        paymentMethod: String
    ): Result<Unit>

    suspend fun cancelPendingTickets(orderCode: String): Result<Unit>

    suspend fun cancelExpiredPendingTickets(): Result<Unit>

    suspend fun saveUserOrderToRealtimeDb(orderData: Map<String, Any>): Result<Unit>
}
