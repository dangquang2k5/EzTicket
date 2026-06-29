package huce.fit.myezticket.domain.usecase

import huce.fit.myezticket.domain.repository.TicketRepository
import huce.fit.myezticket.domain.model.Event
import javax.inject.Inject

class GetPurchasedTicketsUseCase @Inject constructor(private val repository: TicketRepository) {
    operator fun invoke() = repository.getPurchasedTickets()
}

class StartTicketSyncUseCase @Inject constructor(private val repository: TicketRepository) {
    operator fun invoke(onError: (Exception) -> Unit) = repository.startTicketSync(onError)
}

class CreatePendingTicketsUseCase @Inject constructor(private val repository: TicketRepository) {
    suspend operator fun invoke(event: Event, scheduleIndex: Int, selectedTickets: Map<String, Int>, orderCode: String, expiresAtMillis: Long) = 
        repository.createPendingTickets(event, scheduleIndex, selectedTickets, orderCode, expiresAtMillis)
}

class CompletePendingTicketsUseCase @Inject constructor(private val repository: TicketRepository) {
    suspend operator fun invoke(orderCode: String, phoneNumber: String, paymentMethod: String) = 
        repository.completePendingTickets(orderCode, phoneNumber, paymentMethod)
}

class CancelPendingTicketsUseCase @Inject constructor(private val repository: TicketRepository) {
    suspend operator fun invoke(orderCode: String) = repository.cancelPendingTickets(orderCode)
}

class CancelExpiredPendingTicketsUseCase @Inject constructor(private val repository: TicketRepository) {
    suspend operator fun invoke() = repository.cancelExpiredPendingTickets()
}

class SaveUserOrderUseCase @Inject constructor(private val repository: TicketRepository) {
    suspend operator fun invoke(orderData: Map<String, Any>) = repository.saveUserOrderToRealtimeDb(orderData)
}
