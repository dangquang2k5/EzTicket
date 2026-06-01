package huce.fit.myezticket.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import huce.fit.myezticket.domain.model.AppNotification
import huce.fit.myezticket.domain.repository.NotificationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : NotificationRepository {

    private val col get() = db.collection("notifications")

    override fun getNotifications(uid: String): Flow<List<AppNotification>> = callbackFlow {
        val listener = col
            .whereEqualTo("uid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log lỗi nhưng KHÔNG phát emptyList() — giữ nguyên data cũ
                    // (thường do chưa tạo Firestore composite index)
                    android.util.Log.w(
                        "NotificationRepo",
                        "Query failed (index missing?): ${error.message}"
                    )
                    // Nếu Firestore cần composite index, close flow với lỗi
                    // để ViewModel có thể fallback query đơn giản hơn
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AppNotification::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /** Fallback: ch\u1ec9 query theo uid \u2014 kh\u00f4ng c\u1ea7n composite index, sort trong code */
    override fun getNotificationsSimple(uid: String): Flow<List<AppNotification>> = callbackFlow {
        val listener = col
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("NotificationRepo", "Simple query failed: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AppNotification::class.java)?.copy(id = doc.id)
                }?.sortedByDescending { it.createdAt?.seconds } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun createNotificationIfNotExists(
        notification: AppNotification,
        docId: String
    ) {
        val ref = col.document(docId)
        val snapshot = ref.get().await()
        // Chỉ tạo nếu chưa tồn tại → giữ nguyên isRead nếu user đã đọc rồi
        if (!snapshot.exists()) {
            ref.set(notification).await()
        }
    }

    override suspend fun upsertNotification(
        notification: AppNotification,
        docId: String
    ) {
        val ref = col.document(docId)
        val snapshot = ref.get().await()
        if (!snapshot.exists()) {
            // Chưa có → tạo mới với isRead = false
            ref.set(notification).await()
        } else {
            // Đã có → Chỉ cập nhật nếu title hoặc body thay đổi
            val oldTitle = snapshot.getString("title").orEmpty()
            val oldBody = snapshot.getString("body").orEmpty()
            if (oldTitle != notification.title || oldBody != notification.body) {
                ref.update(
                    mapOf(
                        "title" to notification.title,
                        "body" to notification.body,
                        "createdAt" to notification.createdAt,
                        "eventDate" to notification.eventDate
                    )
                ).await()
            } else {
                // Nếu không đổi nội dung, chỉ cập nhật eventDate nếu có thay đổi
                val oldEventDate = snapshot.getTimestamp("eventDate")
                if (oldEventDate != notification.eventDate) {
                    ref.update("eventDate", notification.eventDate).await()
                }
            }
        }
    }

    override suspend fun markAsRead(notificationId: String) {
        col.document(notificationId).update("isRead", true).await()
    }

    override suspend fun markAllAsRead(uid: String) {
        // Chỉ query theo uid (1 field) → không cần composite index
        // Lọc isRead == false trong code để tránh lỗi FAILED_PRECONDITION
        val snapshot = col
            .whereEqualTo("uid", uid)
            .get()
            .await()
        val unreadDocs = snapshot.documents.filter { doc ->
            doc.getBoolean("isRead") != true
        }
        if (unreadDocs.isEmpty()) return
        val batch = db.batch()
        unreadDocs.forEach { doc ->
            batch.update(doc.reference, "isRead", true)
        }
        batch.commit().await()
    }

    override suspend fun deleteNotificationById(docId: String) {
        col.document(docId).delete().await()
    }
}
