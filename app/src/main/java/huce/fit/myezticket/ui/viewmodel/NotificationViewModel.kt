package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import huce.fit.myezticket.domain.model.AppNotification
import huce.fit.myezticket.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val uid: String get() = auth.currentUser?.uid ?: ""

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
            try {
                repository.getNotifications(uid).collect { list ->
                    _notifications.value = list
                }
            } catch (e: Exception) {
                // Primary query th\u1ea5t b\u1ea1i (th\u01b0\u1eddng do thi\u1ebfu Firestore composite index)
                // \u2192 fallback v\u1ec1 query \u0111\u01a1n gi\u1ea3n (ch\u1ec9 l\u1ecdc theo uid, sort trong code)
                android.util.Log.w(
                    "NotificationViewModel",
                    "Primary query failed, using fallback: ${e.message}"
                )
                repository.getNotificationsSimple(uid).collect { list ->
                    _notifications.value = list
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
                repository.markAsRead(notificationId)
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
                repository.markAllAsRead(uid)
            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "markAllAsRead failed: ${e.message}")
            }
        }
    }
}
