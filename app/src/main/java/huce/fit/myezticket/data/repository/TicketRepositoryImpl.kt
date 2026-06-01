package huce.fit.myezticket.data.repository

import com.google.firebase.Timestamp
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
                ?.flatMap { document ->
                    document
                        .toObject(PurchasedTicket::class.java)
                        ?.copy(id = document.id)
                        ?.splitIntoIndividualTickets()
                        .orEmpty()
                }
                ?.sortedByDescending { it.createdAt?.seconds ?: 0L }
                ?: emptyList()

            onTicketsChanged(tickets)
        }
    }

    override suspend fun createPendingTickets(
        event: Event,
        scheduleIndex: Int,
        selectedTickets: Map<String, Int>,
        orderCode: String,
        expiresAt: Timestamp
    ): Result<Unit> = createTickets(
        event = event,
        scheduleIndex = scheduleIndex,
        selectedTickets = selectedTickets,
        orderCode = orderCode,
        expiresAt = expiresAt
    )

    override suspend fun completePendingTickets(
        orderCode: String,
        phoneNumber: String,
        paymentMethod: String
    ): Result<Unit> {
        return try {
            val documents = getOrderDocuments(orderCode)
            if (documents.isEmpty()) {
                return Result.failure(IllegalStateException("Không tìm thấy đơn hàng $orderCode"))
            }

            val isExpired = documents.any { document ->
                (document.getTimestamp("expiresAt")?.toDate()?.time ?: 0L) <= System.currentTimeMillis()
            }
            if (isExpired) {
                cancelPendingTickets(orderCode)
                return Result.failure(IllegalStateException("Đơn hàng đã hết thời gian thanh toán"))
            }

            val pendingDocuments = documents.filter {
                it.getString("status") == PurchasedTicket.STATUS_PENDING
            }
            if (pendingDocuments.isEmpty()) {
                return if (documents.all { it.getString("status") == PurchasedTicket.STATUS_SUCCESS }) {
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalStateException("Đơn hàng không còn chờ thanh toán"))
                }
            }

            val batch = db.batch()
            pendingDocuments.forEach { document ->
                batch.update(
                    document.reference,
                    mapOf(
                        "status" to PurchasedTicket.STATUS_SUCCESS,
                        "customerPhone" to phoneNumber,
                        "paymentMethod" to paymentMethod
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun cancelPendingTickets(orderCode: String): Result<Unit> {
        return try {
            val pendingDocuments = getOrderDocuments(orderCode).filter {
                it.getString("status") == PurchasedTicket.STATUS_PENDING
            }
            if (pendingDocuments.isNotEmpty()) {
                db.runTransaction { transaction ->
                    pendingDocuments.forEach { document ->
                        val latestDocument = transaction.get(document.reference)
                        if (latestDocument.getString("status") == PurchasedTicket.STATUS_PENDING) {
                            transaction.update(document.reference, "status", PurchasedTicket.STATUS_CANCELLED)
                        }
                    }
                }.await()
            }
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun cancelExpiredPendingTickets(): Result<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val expiredDocuments = purchasedTicketsCollection
                .whereEqualTo("status", PurchasedTicket.STATUS_PENDING)
                .get()
                .await()
                .documents
                .filter { document ->
                    (document.getTimestamp("expiresAt")?.toDate()?.time ?: 0L) <= now
                }

            expiredDocuments
                .mapNotNull { it.getString("orderCode") }
                .distinct()
                .forEach { orderCode ->
                    cancelPendingTickets(orderCode).getOrThrow()
                }
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private suspend fun createTickets(
        event: Event,
        scheduleIndex: Int,
        selectedTickets: Map<String, Int>,
        orderCode: String,
        expiresAt: Timestamp
    ): Result<Unit> {
        return try {
            val schedule = event.schedules.getOrNull(scheduleIndex)
            val batch = db.batch()

            selectedTickets.forEach { (ticketName, quantity) ->
                val ticketType = schedule?.ticketTypes?.find { it.name == ticketName }
                val unitPrice = ticketType?.price ?: 0L
                repeat(quantity.coerceAtLeast(0)) {
                    val document = purchasedTicketsCollection.document()
                    val data = hashMapOf(
                        "eventId" to event.id,
                        "eventName" to event.name,
                        "imageUrl" to event.image_url,
                        "orderCode" to orderCode,
                        "ticketCode" to document.id,
                        "eventDate" to schedule?.date,
                        "location" to "${event.venueName}, ${event.address}",
                        "ticketTypeName" to ticketName,
                        "quantity" to 1,
                        "unitPrice" to unitPrice,
                        "totalPrice" to unitPrice,
                        "status" to PurchasedTicket.STATUS_PENDING,
                        "customerPhone" to "",
                        "paymentMethod" to "",
                        "scheduleIndex" to scheduleIndex,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "expiresAt" to expiresAt
                    )

                    batch.set(document, data)
                }
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private suspend fun getOrderDocuments(orderCode: String) =
        purchasedTicketsCollection
            .whereEqualTo("orderCode", orderCode)
            .get()
            .await()
            .documents

    companion object {
        const val COLLECTION_PURCHASED_TICKETS = "purchasedTickets"
    }
}
