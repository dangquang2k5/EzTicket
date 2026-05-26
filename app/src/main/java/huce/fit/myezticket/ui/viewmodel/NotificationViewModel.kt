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
            repository.getNotifications(uid).collect { list ->
                _notifications.value = list
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        val uid = uid
        if (uid.isEmpty()) return
        viewModelScope.launch {
            repository.markAllAsRead(uid)
        }
    }
}
