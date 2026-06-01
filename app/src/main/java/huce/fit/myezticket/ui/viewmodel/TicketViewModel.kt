package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import huce.fit.myezticket.domain.model.AppNotification
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.model.PurchasedTicket
import huce.fit.myezticket.domain.repository.FavoriteRepository
import huce.fit.myezticket.domain.repository.NotificationRepository
import huce.fit.myezticket.domain.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val repository: TicketRepository,
    private val favoriteRepository: FavoriteRepository,
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    private var listenerRegistration: ListenerRegistration? = null

    private val _purchasedTickets = MutableStateFlow<List<PurchasedTicket>>(emptyList())
    val purchasedTickets: StateFlow<List<PurchasedTicket>> = _purchasedTickets.asStateFlow()

    private val _isLoadingTickets = MutableStateFlow(true)
    val isLoadingTickets: StateFlow<Boolean> = _isLoadingTickets.asStateFlow()

    private val _isSavingPayment = MutableStateFlow(false)
    val isSavingPayment: StateFlow<Boolean> = _isSavingPayment.asStateFlow()

    private val _paymentError = MutableStateFlow<String?>(null)
    val paymentError: StateFlow<String?> = _paymentError.asStateFlow()

    init {
        listenPurchasedTickets()
        viewModelScope.launch {
            repository.cancelExpiredPendingTickets()
            while (true) {
                delay(1000)
                val expiredOrders = _purchasedTickets.value
                    .filter { it.isPaymentExpired }
                    .map { it.orderCode }
                    .distinct()

                if (expiredOrders.isNotEmpty() && !_isSavingPayment.value) {
                    _purchasedTickets.value = _purchasedTickets.value.map { ticket ->
                        if (ticket.orderCode in expiredOrders) {
                            ticket.copy(status = PurchasedTicket.STATUS_CANCELLED)
                        } else {
                            ticket
                        }
                    }
                    expiredOrders.forEach { orderCode ->
                        repository.cancelPendingTickets(orderCode)
                    }
                }
            }
        }
    }

    private fun listenPurchasedTickets() {
        listenerRegistration = repository.listenPurchasedTickets(
            onTicketsChanged = { tickets ->
                _purchasedTickets.value = tickets
                _isLoadingTickets.value = false
            },
            onError = { exception ->
                _paymentError.value = exception.message ?: "Không thể tải vé từ Firebase"
                _isLoadingTickets.value = false
            }
        )
    }

    fun savePendingTickets(
        event: Event,
        scheduleIndex: Int,
        selectedTickets: Map<String, Int>,
        orderCode: String,
        expiresAtMillis: Long,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            _isSavingPayment.value = true
            _paymentError.value = null

            val result = repository.createPendingTickets(
                event = event,
                scheduleIndex = scheduleIndex,
                selectedTickets = selectedTickets,
                orderCode = orderCode,
                expiresAt = Timestamp(Date(expiresAtMillis))
            )

            _isSavingPayment.value = false
            result
                .onSuccess { onSaved() }
                .onFailure { exception ->
                    _paymentError.value = exception.message ?: "Không thể lưu vé đang chờ thanh toán lên Firebase"
                }
        }
    }

    fun savePurchasedTickets(
        event: Event,
        phoneNumber: String,
        orderCode: String,
        paymentMethod: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            _isSavingPayment.value = true
            _paymentError.value = null

            val result = repository.completePendingTickets(
                orderCode = orderCode,
                phoneNumber = phoneNumber,
                paymentMethod = paymentMethod
            )

            _isSavingPayment.value = false
            result
                .onSuccess {
                    // Tự động xóa sự kiện khỏi yêu thích sau khi mua vé thành công
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        runCatching { favoriteRepository.removeFavorite(uid, event.id) }
                        runCatching {
                            notificationRepository.createNotificationIfNotExists(
                                notification = AppNotification(
                                    uid = uid,
                                    eventId = event.id,
                                    eventName = event.name,
                                    eventImageUrl = event.image_url,
                                    title = "Thanh toán thành công",
                                    body = "Đơn $orderCode đã thanh toán thành công. Vé đã được lưu vào Vé của tôi.",
                                    type = "PAYMENT_SUCCESS",
                                    createdAt = Timestamp.now()
                                ),
                                docId = "${uid}_payment_$orderCode"
                            )
                        }
                    }
                    onSaved()
                }
                .onFailure { exception ->
                    _paymentError.value = exception.message ?: "Không thể lưu vé lên Firebase"
                }
        }
    }

    fun cancelPendingTickets(
        orderCode: String,
        onFinished: () -> Unit
    ) {
        viewModelScope.launch {
            if (_isSavingPayment.value) {
                onFinished()
                return@launch
            }
            repository.cancelPendingTickets(orderCode)
                .onFailure { exception ->
                    _paymentError.value = exception.message ?: "Không thể cập nhật vé đã hủy lên Firebase"
                }
            onFinished()
        }
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}
