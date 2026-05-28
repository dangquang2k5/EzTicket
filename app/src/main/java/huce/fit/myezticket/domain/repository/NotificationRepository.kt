package huce.fit.myezticket.domain.repository

import huce.fit.myezticket.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    /** Lắng nghe realtime danh sách thông báo của user (cần composite index uid+createdAt) */
    fun getNotifications(uid: String): Flow<List<AppNotification>>

    /** Fallback: query đơn giản không cần index, sắp xếp trong code */
    fun getNotificationsSimple(uid: String): Flow<List<AppNotification>>

    /**
     * Tạo thông báo nếu chưa tồn tại.
     * docId cố định = "{uid}_{eventId}_{type}" để tránh duplicate.
     */
    suspend fun createNotificationIfNotExists(notification: AppNotification, docId: String)

    /**
     * Upsert thông báo:
     * - Nếu chưa tồn tại → tạo mới (isRead = false)
     * - Nếu đã tồn tại → chỉ cập nhật body, title, createdAt (GIỮ NGUYÊN isRead)
     */
    suspend fun upsertNotification(notification: AppNotification, docId: String)

    /** Đánh dấu 1 thông báo đã đọc */
    suspend fun markAsRead(notificationId: String)

    /** Đánh dấu tất cả thông báo của user đã đọc */
    suspend fun markAllAsRead(uid: String)

    /** Xóa thông báo theo docId (dùng để dọn type cũ khi chuyển sang type mới) */
    suspend fun deleteNotificationById(docId: String)
}
