package huce.fit.myezticket.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.model.PurchasedTicket
import huce.fit.myezticket.domain.repository.TicketRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : TicketRepository {
    private val purchasedTicketsCollection = db.collection(COLLECTION_PURCHASED_TICKETS)

    override fun listenPurchasedTickets(
        onTicketsChanged: (List<PurchasedTicket>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return purchasedTicketsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }

            val tickets = snapshot
                ?.documents
                ?.mapNotNull { document ->
                    document.toObject(PurchasedTicket::class.java)?.copy(id = document.id)
                }
                ?.sortedByDescending { it.createdAt?.seconds ?: 0L }
                ?: emptyList()

            onTicketsChanged(tickets)
        }
    }

    override suspend fun createPurchasedTickets(
        event: Event,
        scheduleIndex: Int,
        selectedTickets: Map<String, Int>,
        phoneNumber: String,
        orderCode: String,
        paymentMethod: String
    ): Result<Unit> {
        return try {
            val schedule = event.schedules.getOrNull(scheduleIndex)
            val batch = db.batch()

            selectedTickets.forEach { (ticketName, quantity) ->
                val ticketType = schedule?.ticketTypes?.find { it.name == ticketName }
                val unitPrice = ticketType?.price ?: 0L
                val document = purchasedTicketsCollection.document()
                val data = hashMapOf(
                    "eventId" to event.id,
                    "eventName" to event.name,
                    "imageUrl" to event.image_url,
                    "orderCode" to orderCode,
                    "eventDate" to schedule?.date,
                    "location" to "${event.venueName}, ${event.address}",
                    "ticketTypeName" to ticketName,
                    "quantity" to quantity,
                    "unitPrice" to unitPrice,
                    "totalPrice" to unitPrice * quantity,
                    "status" to "Thành công",
                    "customerPhone" to phoneNumber,
                    "paymentMethod" to paymentMethod,
                    "scheduleIndex" to scheduleIndex,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                batch.set(document, data)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    companion object {
        const val COLLECTION_PURCHASED_TICKETS = "purchasedTickets"
    }
}
