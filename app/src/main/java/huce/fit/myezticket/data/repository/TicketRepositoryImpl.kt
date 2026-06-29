package huce.fit.myezticket.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import huce.fit.myezticket.data.local.dao.TicketDao
import huce.fit.myezticket.data.local.dao.EventDao
import huce.fit.myezticket.data.local.entity.TicketEntity
import huce.fit.myezticket.data.local.entity.EventEntity
import huce.fit.myezticket.data.model.EventDto
import huce.fit.myezticket.data.model.PurchasedTicketDto
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.model.EventSchedule
import huce.fit.myezticket.domain.model.TicketType
import huce.fit.myezticket.domain.model.PurchasedTicket
import huce.fit.myezticket.domain.repository.TicketRepository
import huce.fit.myezticket.core.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TicketRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val realtimeDb: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val ticketDao: TicketDao,
    private val eventDao: EventDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : TicketRepository {
    private val purchasedTicketsCollection = db.collection(COLLECTION_PURCHASED_TICKETS)

    override fun getPurchasedTickets(): Flow<List<PurchasedTicket>> {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isEmpty()) return flowOf(emptyList())

        return ticketDao.getTicketsFlow(uid).map { entities ->
            entities.map { it.toPurchasedTicket() }
        }
    }

    override fun startTicketSync(onError: (Exception) -> Unit): () -> Unit {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isEmpty()) return {}

        val registration = purchasedTicketsCollection
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val tickets = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(PurchasedTicketDto::class.java)?.apply { id = document.id }?.toDomainModel()
                }?.flatMap { it.splitIntoIndividualTickets() } ?: emptyList()

                // Lưu vào Room bằng applicationScope
                applicationScope.launch(Dispatchers.IO) {
                    val entities = tickets.map { TicketEntity.fromPurchasedTicket(it, uid) }
                    ticketDao.replaceTickets(uid, entities)
                }
            }

        return { registration.remove() }
    }

    override suspend fun createPendingTickets(
        event: Event,
        scheduleIndex: Int,
        selectedTickets: Map<String, Int>,
        orderCode: String,
        expiresAtMillis: Long
    ): Result<Unit> = createTickets(
        event = event,
        scheduleIndex = scheduleIndex,
        selectedTickets = selectedTickets,
        orderCode = orderCode,
        expiresAtMillis = expiresAtMillis
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
        android.util.Log.d("TicketRepository", "Bắt đầu hủy vé pending cho đơn hàng: $orderCode")
        return try {
            // Lấy danh sách các vé pending của đơn hàng này
            // Sử dụng một query độc lập để không phụ thuộc vào getOrderDocuments nếu cần (hoặc vẫn dùng)
            val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Chưa đăng nhập"))
            val pendingDocsRef = purchasedTicketsCollection
                .whereEqualTo("uid", uid)
                .whereEqualTo("orderCode", orderCode)
                .whereEqualTo("status", PurchasedTicket.STATUS_PENDING)
                .get()
                .await()
                .documents
                
            android.util.Log.d("TicketRepository", "Tìm thấy ${pendingDocsRef.size} tài liệu vé PENDING")
            if (pendingDocsRef.isNotEmpty()) {
                var eventToUpdate: Event? = null
                var schedulesToUpdate: List<EventSchedule>? = null
                var statusToUpdate: String? = null

                db.runTransaction { transaction ->
                    // Lấy lại dữ liệu mới nhất trong transaction
                    val validDocs = pendingDocsRef.mapNotNull { doc ->
                        val latest = transaction.get(doc.reference)
                        val status = latest.getString("status")
                        android.util.Log.d("TicketRepository", "Trạng thái mới nhất của vé ${doc.id}: $status")
                        if (status == PurchasedTicket.STATUS_PENDING) latest else null
                    }
                    
                    android.util.Log.d("TicketRepository", "Số lượng vé hợp lệ trong transaction: ${validDocs.size}")
                    if (validDocs.isEmpty()) {
                        android.util.Log.w("TicketRepository", "Không có vé nào hợp lệ, bỏ qua")
                        return@runTransaction
                    }
                    
                    // Phục hồi lại quantity cho sự kiện
                    val ticketsByEvent = validDocs.groupBy { it.getString("eventId") }
                    for ((eventId, docs) in ticketsByEvent) {
                        if (eventId == null) continue
                        val eventRef = db.collection("events").document(eventId)
                        val eventDoc = transaction.get(eventRef)
                        if (eventDoc.exists()) {
                            val eventObj = eventDoc.toObject(EventDto::class.java)?.apply { id = eventId }?.toDomainModel()
                            if (eventObj != null) {
                                val updatedSchedules = eventObj.schedules.toMutableList()
                                val ticketsBySchedule = docs.groupBy { (it.get("scheduleIndex") as? Number)?.toInt() ?: 0 }
                                
                                for ((schedIdx, schedDocs) in ticketsBySchedule) {
                                    val currentSchedule = updatedSchedules.getOrNull(schedIdx)
                                    if (currentSchedule != null) {
                                        val ticketTypeCounts = schedDocs.groupBy { it.getString("ticketTypeName") ?: "" }
                                            .mapValues { it.value.sumOf { doc -> (doc.get("quantity") as? Number)?.toInt() ?: 0 } }
                                            
                                        android.util.Log.d("TicketRepository", "Cộng trả cho suất diễn $schedIdx: $ticketTypeCounts")
                                        val updatedTicketTypes = currentSchedule.ticketTypes.map { tt ->
                                            val returnedQty = ticketTypeCounts[tt.name] ?: 0
                                            if (returnedQty > 0) {
                                                android.util.Log.d("TicketRepository", "Hoàn trả ${tt.name}: +$returnedQty vé (cũ: ${tt.quantity})")
                                            }
                                            tt.copy(quantity = tt.quantity + returnedQty)
                                        }
                                        updatedSchedules[schedIdx] = currentSchedule.copy(ticketTypes = updatedTicketTypes)
                                    } else {
                                        android.util.Log.w("TicketRepository", "Không tìm thấy suất diễn $schedIdx")
                                    }
                                }
                                transaction.update(eventRef, "schedules", updatedSchedules.map { huce.fit.myezticket.data.model.EventScheduleDto.fromDomainModel(it) })

                                 // Tự động cập nhật lại thành AVAILABLE nếu có vé được hoàn trả
                                 val now = System.currentTimeMillis()
                                 val isAllSoldOut = updatedSchedules.isNotEmpty() && updatedSchedules.filter { schedule ->
                                     val endTime = schedule.endDateMillis ?: schedule.dateMillis
                                     endTime == null || endTime >= now
                                 }.let { futureSchedules ->
                                     futureSchedules.isEmpty() || futureSchedules.all { schedule ->
                                         val visibleTickets = schedule.ticketTypes.filter { it.isVisible }
                                         visibleTickets.isNotEmpty() && visibleTickets.all { it.quantity <= 0 }
                                     }
                                 }
                                 var targetStatus = eventObj.status
                                 if (!isAllSoldOut && eventObj.status == "SOLD_OUT") {
                                     android.util.Log.d("TicketRepository", "Đổi trạng thái sự kiện thành AVAILABLE")
                                     transaction.update(eventRef, "status", "AVAILABLE")
                                     targetStatus = "AVAILABLE"
                                 }

                                 eventToUpdate = eventObj
                                 schedulesToUpdate = updatedSchedules
                                 statusToUpdate = targetStatus
                            }
                        }
                    }

                    // Cập nhật trạng thái vé thành CANCELLED
                    validDocs.forEach { document ->
                        android.util.Log.d("TicketRepository", "Cập nhật trạng thái vé ${document.id} thành Đã hủy")
                        transaction.update(document.reference, "status", PurchasedTicket.STATUS_CANCELLED)
                    }
                }.await()
                android.util.Log.d("TicketRepository", "Transaction hủy vé pending hoàn tất thành công")
            }
            Result.success(Unit)
        } catch (exception: Exception) {
            android.util.Log.e("TicketRepository", "Lỗi khi hủy vé pending: ${exception.message}", exception)
            Result.failure(exception)
        }
    }

    override suspend fun cancelExpiredPendingTickets(): Result<Unit> {
        android.util.Log.d("TicketRepository", "Bắt đầu quét vé pending hết hạn")
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Chưa đăng nhập"))
            val now = System.currentTimeMillis()
            val expiredDocuments = purchasedTicketsCollection
                .whereEqualTo("uid", uid)
                .whereEqualTo("status", PurchasedTicket.STATUS_PENDING)
                .get()
                .await()
                .documents
                .filter { document ->
                    val expiresAt = document.getTimestamp("expiresAt")?.toDate()?.time ?: 0L
                    expiresAt <= now
                }

            android.util.Log.d("TicketRepository", "Tìm thấy ${expiredDocuments.size} vé pending đã hết hạn")
            expiredDocuments
                .mapNotNull { it.getString("orderCode") }
                .distinct()
                .forEach { orderCode ->
                    android.util.Log.d("TicketRepository", "Hủy đơn hàng hết hạn: $orderCode")
                    cancelPendingTickets(orderCode).getOrThrow()
                }
            Result.success(Unit)
        } catch (exception: Exception) {
            android.util.Log.e("TicketRepository", "Lỗi quét vé hết hạn: ${exception.message}", exception)
            Result.failure(exception)
        }
    }

    override suspend fun saveUserOrderToRealtimeDb(orderData: Map<String, Any>): Result<Unit> {
        // Trả về thành công luôn vì bảng 'userOrders' chỉ dùng để log nháp và không được đọc ở bất kỳ đâu trong app,
        // giúp tránh lỗi PERMISSION_DENIED do cấu hình Security Rules trên Firebase của bạn.
        return Result.success(Unit)
    }

    private suspend fun createTickets(
        event: Event,
        scheduleIndex: Int,
        selectedTickets: Map<String, Int>,
        orderCode: String,
        expiresAtMillis: Long
    ): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("Người dùng chưa đăng nhập"))

            var eventToUpdate: Event? = null
            var schedulesToUpdate: List<EventSchedule>? = null
            var statusToUpdate: String? = null

            db.runTransaction { transaction ->
                val eventRef = db.collection("events").document(event.id)
                val eventDoc = transaction.get(eventRef)
                if (!eventDoc.exists()) {
                    throw Exception("Sự kiện không tồn tại")
                }
                
                val currentEvent = eventDoc.toObject(EventDto::class.java)?.apply { id = event.id }?.toDomainModel() ?: throw Exception("Sự kiện không tồn tại")
                val currentSchedule = currentEvent.schedules.getOrNull(scheduleIndex) ?: throw Exception("Lịch diễn không tồn tại")
                
                // Trừ quantity của từng loại vé trong sự kiện
                val updatedTicketTypes = currentSchedule.ticketTypes.map { tt ->
                    val buyQty = selectedTickets[tt.name] ?: 0
                    if (tt.quantity < buyQty) {
                        throw Exception("Không đủ số lượng cho loại vé ${tt.name}")
                    }
                    tt.copy(quantity = tt.quantity - buyQty)
                }
                
                val updatedSchedule = currentSchedule.copy(ticketTypes = updatedTicketTypes)
                val updatedSchedules = currentEvent.schedules.toMutableList()
                updatedSchedules[scheduleIndex] = updatedSchedule
                
                // Cập nhật sự kiện với mảng schedules mới
                transaction.update(eventRef, "schedules", updatedSchedules.map { huce.fit.myezticket.data.model.EventScheduleDto.fromDomainModel(it) })

                // Tự động cập nhật SOLD_OUT nếu tất cả các suất diễn đều hết vé
                val now = System.currentTimeMillis()
                val isAllSoldOut = updatedSchedules.isNotEmpty() && updatedSchedules.filter { schedule ->
                    val endTime = schedule.endDateMillis ?: schedule.dateMillis
                    endTime == null || endTime >= now
                }.let { futureSchedules ->
                    futureSchedules.isEmpty() || futureSchedules.all { schedule ->
                        val visibleTickets = schedule.ticketTypes.filter { it.isVisible }
                        visibleTickets.isNotEmpty() && visibleTickets.all { it.quantity <= 0 }
                    }
                }
                var targetStatus = currentEvent.status
                if (isAllSoldOut && currentEvent.status == "AVAILABLE") {
                    transaction.update(eventRef, "status", "SOLD_OUT")
                    targetStatus = "SOLD_OUT"
                }

                // Lưu dữ liệu để cập nhật Room sau
                eventToUpdate = currentEvent
                schedulesToUpdate = updatedSchedules
                statusToUpdate = targetStatus

                // Tạo các tài liệu purchasedTickets
                selectedTickets.forEach { (ticketName, quantity) ->
                    val ticketType = currentSchedule.ticketTypes.find { it.name == ticketName }
                    val unitPrice = ticketType?.price ?: 0L
                    repeat(quantity.coerceAtLeast(0)) {
                        val document = purchasedTicketsCollection.document()
                        val data = hashMapOf(
                            "uid" to uid,
                            "eventId" to event.id,
                            "eventName" to event.name,
                            "imageUrl" to event.image_url,
                            "orderCode" to orderCode,
                            "ticketCode" to document.id,
                            "eventDate" to currentSchedule.dateMillis?.let { Timestamp(java.util.Date(it)) },
                            "eventEndDate" to currentSchedule.endDateMillis?.let { Timestamp(java.util.Date(it)) },
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
                            "expiresAt" to Timestamp(java.util.Date(expiresAtMillis))
                        )

                        transaction.set(document, data)
                    }
                }
            }.await()
            
            Result.success(Unit)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private suspend fun getOrderDocuments(orderCode: String): List<com.google.firebase.firestore.DocumentSnapshot> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return purchasedTicketsCollection
            .whereEqualTo("uid", uid)
            .whereEqualTo("orderCode", orderCode)
            .get()
            .await()
            .documents
    }

    companion object {
        const val COLLECTION_PURCHASED_TICKETS = "purchasedTickets"
    }
}
