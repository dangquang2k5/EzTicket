package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import huce.fit.myezticket.domain.model.AppNotification
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.repository.FavoriteRepository
import huce.fit.myezticket.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEVEN_DAYS_MS = 7 * 24 * 60 * 60 * 1000L
private const val THREE_DAYS_MS = 3 * 24 * 60 * 60 * 1000L

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val uid: String get() = auth.currentUser?.uid ?: ""

    // ── Danh sách ID sự kiện đã yêu thích ─────────────────────────────────────
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds

    // ── Danh sách Event đầy đủ (được inject từ bên ngoài khi có data) ─────────
    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())

    // ── Sự kiện yêu thích đầy đủ thông tin ─────────────────────────────────────
    val favoriteEvents: StateFlow<List<Event>> = combine(
        _allEvents, _favoriteIds
    ) { events, ids ->
        events.filter { it.id in ids }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        loadFavoriteIds()
    }

    private fun loadFavoriteIds() {
        val uid = uid
        if (uid.isEmpty()) return
        viewModelScope.launch {
            favoriteRepository.getFavoriteIds(uid).collect { ids ->
                _favoriteIds.value = ids
            }
        }
    }

    /** Gọi từ bên ngoài khi EventViewModel đã load xong events */
    fun updateAllEvents(events: List<Event>) {
        _allEvents.value = events
        // Mỗi lần events cập nhật → re-check thông báo
        if (uid.isNotEmpty() && _favoriteIds.value.isNotEmpty()) {
            checkAndCreateNotifications(events, _favoriteIds.value)
        }
    }

    /** Toggle yêu thích: thêm nếu chưa có, xóa nếu đã có */
    fun toggleFavorite(eventId: String) {
        val uid = uid
        if (uid.isEmpty()) return
        viewModelScope.launch {
            if (eventId in _favoriteIds.value) {
                favoriteRepository.removeFavorite(uid, eventId)
            } else {
                favoriteRepository.addFavorite(uid, eventId)
                // Check ngay khi thêm yêu thích mới
                val event = _allEvents.value.find { it.id == eventId }
                if (event != null) {
                    checkAndCreateNotifications(listOf(event), setOf(eventId))
                }
            }
        }
    }

    /** Gọi sau khi mua vé thành công → xóa khỏi yêu thích */
    fun removeFavoriteOnPurchase(eventId: String) {
        val uid = uid
        if (uid.isEmpty() || eventId !in _favoriteIds.value) return
        viewModelScope.launch {
            favoriteRepository.removeFavorite(uid, eventId)
        }
    }

    fun isFavorite(eventId: String): Boolean = eventId in _favoriteIds.value

    // ── Logic kiểm tra ngày và tạo thông báo ─────────────────────────────────
    private fun checkAndCreateNotifications(events: List<Event>, favoriteIds: Set<String>) {
        val uid = uid
        if (uid.isEmpty()) return
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            events.filter { it.id in favoriteIds }.forEach { event ->
                // Lấy ngày diễn gần nhất trong tương lai
                val nextDateMs = event.schedules
                    .mapNotNull { it.date?.toDate()?.time }
                    .filter { it > now }
                    .minOrNull()

                if (nextDateMs != null) {
                    val diff = nextDateMs - now
                    when {
                        diff <= THREE_DAYS_MS -> {
                            val days = (diff / (24 * 60 * 60 * 1000)).coerceAtLeast(0)
                            createNotif(
                                uid, event,
                                type = "EVENT_3DAYS",
                                title = "⏰ Sự kiện sắp diễn ra!",
                                body = "\"${event.name}\" còn ${days} ngày nữa là diễn ra. Đừng bỏ lỡ!"
                            )
                        }
                        diff <= SEVEN_DAYS_MS -> {
                            val days = (diff / (24 * 60 * 60 * 1000)).coerceAtLeast(0)
                            createNotif(
                                uid, event,
                                type = "EVENT_7DAYS",
                                title = "📅 Sự kiện sắp diễn ra!",
                                body = "\"${event.name}\" còn ${days} ngày nữa. Hãy chuẩn bị sẵn sàng!"
                            )
                        }
                    }
                }

                // Kiểm tra riêng cho sự kiện "COMING_SOON" (sắp mở bán vé)
                if (event.status == "COMING_SOON" && nextDateMs != null) {
                    val diff = nextDateMs - now
                    when {
                        diff <= THREE_DAYS_MS -> createNotif(
                            uid, event,
                            type = "SALE_3DAYS",
                            title = "🎫 Vé sắp mở bán!",
                            body = "Vé sự kiện \"${event.name}\" sẽ mở bán trong vòng 3 ngày!"
                        )
                        diff <= SEVEN_DAYS_MS -> createNotif(
                            uid, event,
                            type = "SALE_7DAYS",
                            title = "🎫 Vé sắp mở bán!",
                            body = "Vé sự kiện \"${event.name}\" sẽ mở bán trong vòng 7 ngày!"
                        )
                    }
                }
            }
        }
    }

    private suspend fun createNotif(
        uid: String,
        event: Event,
        type: String,
        title: String,
        body: String
    ) {
        // docId cố định để tránh tạo trùng: uid_eventId_type
        val docId = "${uid}_${event.id}_$type"
        notificationRepository.createNotificationIfNotExists(
            notification = AppNotification(
                uid = uid,
                eventId = event.id,
                eventName = event.name,
                eventImageUrl = event.image_url,
                title = title,
                body = body,
                type = type,
                isRead = false,
                createdAt = Timestamp.now()
            ),
            docId = docId
        )
    }
}
