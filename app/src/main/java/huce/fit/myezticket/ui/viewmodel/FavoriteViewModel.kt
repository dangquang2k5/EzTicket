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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val FAV_PAGE_SIZE = 10
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

    // ── Phân trang cho màn hình yêu thích ────────────────────────────────────
    private val _favDisplayCount = MutableStateFlow(FAV_PAGE_SIZE)

    val pagedFavoriteEvents: StateFlow<List<Event>> = combine(
        favoriteEvents, _favDisplayCount
    ) { events, count ->
        events.take(count)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val hasMoreFavorites: StateFlow<Boolean> = combine(
        favoriteEvents, _favDisplayCount
    ) { events, count ->
        events.size > count
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun loadMoreFavorites() {
        _favDisplayCount.value += FAV_PAGE_SIZE
    }

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
                    val days = (diff / (24 * 60 * 60 * 1000)).coerceAtLeast(0)

                    when {
                        diff <= THREE_DAYS_MS -> {
                            // Xóa thông báo 7 ngày cũ
                            notificationRepository.deleteNotificationById("${uid}_${event.id}_EVENT_7DAYS")

                            // Body thay đổi theo ngày: 3→2→1→hôm nay
                            val (title, body) = if (days == 0L) {
                                "🔥 Sự kiện diễn ra hôm nay!" to
                                        "\"${event.name}\" diễn ra hôm nay. Đừng bỏ lỡ!"
                            } else {
                                "⏰ Sự kiện sắp diễn ra!" to
                                        "\"${event.name}\" còn $days ngày nữa là diễn ra. Đừng bỏ lỡ!"
                            }

                            // Upsert: cập nhật body mỗi ngày (3→2→1→hôm nay)
                            upsertNotif(uid, event, "EVENT_3DAYS", title, body)
                        }
                        diff <= SEVEN_DAYS_MS -> {
                            // Xóa thông báo 3 ngày cũ (nếu còn từ ngày diễn trước)
                            notificationRepository.deleteNotificationById("${uid}_${event.id}_EVENT_3DAYS")
                            // Tạo 1 lần duy nhất khi vào vùng 7 ngày
                            createNotifOnce(
                                uid, event,
                                type = "EVENT_7DAYS",
                                title = "📅 Sự kiện sắp diễn ra!",
                                body = "\"${event.name}\" còn $days ngày nữa. Hãy chuẩn bị sẵn sàng!"
                            )
                        }
                        else -> {
                            // Ngày kế tiếp > 7 ngày → dọn dẹp thông báo cũ từ ngày diễn trước
                            notificationRepository.deleteNotificationById("${uid}_${event.id}_EVENT_3DAYS")
                            notificationRepository.deleteNotificationById("${uid}_${event.id}_EVENT_7DAYS")
                        }
                    }
                } else {
                    // Không còn ngày nào trong tương lai → dọn dẹp hết thông báo cũ
                    notificationRepository.deleteNotificationById("${uid}_${event.id}_EVENT_3DAYS")
                    notificationRepository.deleteNotificationById("${uid}_${event.id}_EVENT_7DAYS")
                    notificationRepository.deleteNotificationById("${uid}_${event.id}_SALE_3DAYS")
                    notificationRepository.deleteNotificationById("${uid}_${event.id}_SALE_7DAYS")
                }

                // Kiểm tra riêng cho sự kiện "COMING_SOON" (sắp mở bán vé)
                if (event.status == "COMING_SOON" && nextDateMs != null) {
                    val diff = nextDateMs - now
                    val days = (diff / (24 * 60 * 60 * 1000)).coerceAtLeast(0)

                    when {
                        diff <= THREE_DAYS_MS -> {
                            // Xóa thông báo 7 ngày cũ
                            notificationRepository.deleteNotificationById("${uid}_${event.id}_SALE_7DAYS")

                            val (title, body) = if (days == 0L) {
                                "🎫 Vé mở bán hôm nay!" to
                                        "Vé sự kiện \"${event.name}\" mở bán hôm nay!"
                            } else {
                                "🎫 Vé sắp mở bán!" to
                                        "Vé sự kiện \"${event.name}\" sẽ mở bán trong $days ngày nữa!"
                            }

                            upsertNotif(uid, event, "SALE_3DAYS", title, body)
                        }
                        diff <= SEVEN_DAYS_MS -> {
                            // Xóa thông báo 3 ngày cũ (nếu còn từ ngày diễn trước)
                            notificationRepository.deleteNotificationById("${uid}_${event.id}_SALE_3DAYS")
                            createNotifOnce(
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
    }

    /** Tạo thông báo 1 lần — dùng cho mốc 7 ngày (không update nếu đã tồn tại) */
    private suspend fun createNotifOnce(
        uid: String,
        event: Event,
        type: String,
        title: String,
        body: String
    ) {
        val docId = "${uid}_${event.id}_$type"
        val now = System.currentTimeMillis()
        val nextDateMs = event.schedules
            .mapNotNull { it.date?.toDate()?.time }
            .filter { it > now }
            .minOrNull()
        val eventDate = nextDateMs?.let { Timestamp(java.util.Date(it)) } ?: event.schedules.firstOrNull()?.date

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
                eventDate = eventDate,
                createdAt = Timestamp.now()
            ),
            docId = docId
        )
    }

    /** Upsert thông báo — dùng cho countdown 3→2→1→hôm nay (cập nhật body nhưng giữ isRead) */
    private suspend fun upsertNotif(
        uid: String,
        event: Event,
        type: String,
        title: String,
        body: String
    ) {
        val docId = "${uid}_${event.id}_$type"
        val now = System.currentTimeMillis()
        val nextDateMs = event.schedules
            .mapNotNull { it.date?.toDate()?.time }
            .filter { it > now }
            .minOrNull()
        val eventDate = nextDateMs?.let { Timestamp(java.util.Date(it)) } ?: event.schedules.firstOrNull()?.date

        notificationRepository.upsertNotification(
            notification = AppNotification(
                uid = uid,
                eventId = event.id,
                eventName = event.name,
                eventImageUrl = event.image_url,
                title = title,
                body = body,
                type = type,
                isRead = false,
                eventDate = eventDate,
                createdAt = Timestamp.now()
            ),
            docId = docId
        )
    }
}
