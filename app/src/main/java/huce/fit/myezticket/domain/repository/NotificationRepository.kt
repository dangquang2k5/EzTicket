package huce.fit.myezticket.domain.repository

import huce.fit.myezticket.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    /** Lắng nghe realtime danh sách thông báo của user */
    fun getNotifications(uid: String): Flow<List<AppNotification>>

    /**
     * Tạo thông báo nếu chưa tồn tại.
     * docId cố định = "{uid}_{eventId}_{type}" để tránh duplicate.
     */
    suspend fun createNotificationIfNotExists(notification: AppNotification, docId: String)

    /** Đánh dấu 1 thông báo đã đọc */
    suspend fun markAsRead(notificationId: String)

    /** Đánh dấu tất cả thông báo của user đã đọc */
    suspend fun markAllAsRead(uid: String)
}
