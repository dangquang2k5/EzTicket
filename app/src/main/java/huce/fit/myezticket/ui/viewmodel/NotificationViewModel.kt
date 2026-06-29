package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import huce.fit.myezticket.core.common.Resource
import huce.fit.myezticket.domain.model.AppNotification
import huce.fit.myezticket.domain.usecase.GetCurrentUserUidUseCase
import huce.fit.myezticket.domain.usecase.GetCurrentUserDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotificationsUseCase: huce.fit.myezticket.domain.usecase.GetNotificationsUseCase,
    private val getNotificationsSimpleUseCase: huce.fit.myezticket.domain.usecase.GetNotificationsSimpleUseCase,
    private val markNotificationAsReadUseCase: huce.fit.myezticket.domain.usecase.MarkNotificationAsReadUseCase,
    private val markAllNotificationsAsReadUseCase: huce.fit.myezticket.domain.usecase.MarkAllNotificationsAsReadUseCase,
    private val getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
    private val getCurrentUserDetailUseCase: GetCurrentUserDetailUseCase
) : ViewModel() {

    private val uid: String get() = getCurrentUserUidUseCase()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications

    /** Số thông báo chưa đọc — hiển thị trên badge icon 🔔 */
    val unreadCount: StateFlow<Int> = _notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        val uid = uid
        if (uid.isEmpty()) return
        viewModelScope.launch {
            // Lấy cấu hình thông báo hiện tại của người dùng
            val userRes = getCurrentUserDetailUseCase()
            val user = (userRes as? Resource.Success)?.data
            val bookingEnabled = user?.bookingNotificationEnabled ?: true
            val promoEnabled = user?.promoNotificationEnabled ?: true
            val systemEnabled = user?.systemNotificationEnabled ?: true

            val filterNotif: (List<AppNotification>) -> List<AppNotification> = { list ->
                list.filter { notif ->
                    when (notif.type) {
                        "PAYMENT_SUCCESS" -> bookingEnabled
                        "PROMO" -> promoEnabled
                        "EVENT_3DAYS", "EVENT_7DAYS", "SALE_3DAYS", "SALE_7DAYS" -> systemEnabled
                        else -> true
                    }
                }
            }

            try {
                getNotificationsUseCase(uid).collect { list ->
                    _notifications.value = filterNotif(list)
                }
            } catch (e: Exception) {
                // Primary query thất bại (thường do thiếu Firestore composite index)
                // → fallback về query đơn giản (chỉ lọc theo uid, sort trong code)
                android.util.Log.w(
                    "NotificationViewModel",
                    "Primary query failed, using fallback: ${e.message}"
                )
                try {
                    getNotificationsSimpleUseCase(uid).collect { list ->
                        _notifications.value = filterNotif(list)
                    }
                } catch (fallbackEx: Exception) {
                    android.util.Log.e("NotificationViewModel", "Fallback query failed", fallbackEx)
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        // Cập nhật UI ngay lập tức (optimistic update)
        _notifications.value = _notifications.value.map { notif ->
            if (notif.id == notificationId) notif.copy(isRead = true) else notif
        }
        // Đồng bộ lên Firestore
        viewModelScope.launch {
            try {
                markNotificationAsReadUseCase(notificationId)
            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "markAsRead failed: ${e.message}")
            }
        }
    }

    fun markAllAsRead() {
        val uid = uid
        if (uid.isEmpty()) return
        // Cập nhật UI ngay lập tức (optimistic update)
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        // Đồng bộ lên Firestore
        viewModelScope.launch {
            try {
                markAllNotificationsAsReadUseCase(uid)
            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "markAllAsRead failed: ${e.message}")
            }
        }
    }
}
