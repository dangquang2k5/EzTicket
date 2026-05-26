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
                    // Không crash app — phát list rỗng và log lỗi
                    // (thường do chưa tạo Firestore composite index)
                    android.util.Log.w(
                        "NotificationRepo",
                        "Query failed (index missing?): ${error.message}"
                    )
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(AppNotification::class.java)?.copy(id = doc.id)
                } ?: emptyList()
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

    override suspend fun markAsRead(notificationId: String) {
        col.document(notificationId).update("isRead", true).await()
    }

    override suspend fun markAllAsRead(uid: String) {
        val snapshot = col
            .whereEqualTo("uid", uid)
            .whereEqualTo("isRead", false)
            .get()
            .await()
        if (snapshot.isEmpty) return
        val batch = db.batch()
        snapshot.documents.forEach { doc ->
            batch.update(doc.reference, "isRead", true)
        }
        batch.commit().await()
    }
}
