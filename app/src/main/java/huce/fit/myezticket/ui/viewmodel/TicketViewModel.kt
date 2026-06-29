package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import dagger.hilt.android.lifecycle.HiltViewModel
import huce.fit.myezticket.domain.model.AppNotification
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.model.PurchasedTicket
import huce.fit.myezticket.domain.usecase.GetPurchasedTicketsUseCase
import huce.fit.myezticket.domain.usecase.StartTicketSyncUseCase
import huce.fit.myezticket.domain.usecase.CreatePendingTicketsUseCase
import huce.fit.myezticket.domain.usecase.CompletePendingTicketsUseCase
import huce.fit.myezticket.domain.usecase.CancelPendingTicketsUseCase
import huce.fit.myezticket.domain.usecase.CancelExpiredPendingTicketsUseCase
import huce.fit.myezticket.domain.usecase.SaveUserOrderUseCase
import huce.fit.myezticket.domain.usecase.RemoveFavoriteUseCase
import huce.fit.myezticket.domain.usecase.CreateNotificationUseCase
import huce.fit.myezticket.domain.usecase.GetCurrentUserUidUseCase
import huce.fit.myezticket.domain.usecase.GetCurrentUserDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val getPurchasedTicketsUseCase: GetPurchasedTicketsUseCase,
    private val startTicketSyncUseCase: StartTicketSyncUseCase,
    private val createPendingTicketsUseCase: CreatePendingTicketsUseCase,
    private val completePendingTicketsUseCase: CompletePendingTicketsUseCase,
    private val cancelPendingTicketsUseCase: CancelPendingTicketsUseCase,
    private val cancelExpiredPendingTicketsUseCase: CancelExpiredPendingTicketsUseCase,
    private val saveUserOrderUseCase: SaveUserOrderUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val createNotificationUseCase: CreateNotificationUseCase,
    private val getCurrentUserUidUseCase: GetCurrentUserUidUseCase,
    private val getCurrentUserDetailUseCase: GetCurrentUserDetailUseCase
) : ViewModel() {
    private var syncCleanup: (() -> Unit)? = null
    private var ticketCollectionJob: kotlinx.coroutines.Job? = null

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
            // First time load
            cancelExpiredPendingTicketsUseCase()
            // Loop to monitor expiration locally
            while (isActive) {
                delay(10000) // Check every 10 seconds
                val tickets = _purchasedTickets.value
                val now = System.currentTimeMillis()
                val hasExpired = tickets.any { 
                    it.status == PurchasedTicket.STATUS_PENDING && (it.expiresAtMillis ?: 0L) <= now 
                }
                if (hasExpired) {
                    cancelExpiredPendingTicketsUseCase()
                }
            }
        }
    }

    private fun listenPurchasedTickets() {
        // Hủy listener cũ trước khi tạo mới (quan trọng khi đổi tài khoản)
        syncCleanup?.invoke()
        syncCleanup = null
        ticketCollectionJob?.cancel() // Hủy job Room thu thập vé cũ trước khi tạo mới
        _purchasedTickets.value = emptyList()
        _isLoadingTickets.value = true

        // 1. Observe from Room (Single Source of Truth)
        ticketCollectionJob = viewModelScope.launch {
            getPurchasedTicketsUseCase().collect { tickets ->
                _purchasedTickets.value = tickets
                _isLoadingTickets.value = false
            }
        }

        // 2. Trigger Firebase Sync to update Room in background
        syncCleanup = startTicketSyncUseCase(
            onError = { exception ->
                _paymentError.value = exception.message ?: "Không thể tải vé từ Firebase"
                _isLoadingTickets.value = false
            }
        )
    }

    /** Gọi hàm này sau khi login thành công để reload dữ liệu đúng tài khoản */
    fun reloadForCurrentUser() {
        listenPurchasedTickets()
        viewModelScope.launch {
            // First time load
            cancelExpiredPendingTicketsUseCase()
            // Loop to monitor expiration locally
            while (isActive) {
                delay(10000) // Check every 10 seconds
                val tickets = _purchasedTickets.value
                val now = System.currentTimeMillis()
                val hasExpired = tickets.any { 
                    it.status == PurchasedTicket.STATUS_PENDING && (it.expiresAtMillis ?: 0L) <= now 
                }
                if (hasExpired) {
                    cancelExpiredPendingTicketsUseCase()
                }
            }
        }
    }

    /** Gọi hàm này khi logout để xóa sạch dữ liệu và hủy listener */
    fun clearTickets() {
        syncCleanup?.invoke()
        syncCleanup = null
        ticketCollectionJob?.cancel() // Hủy job thu thập vé Room
        ticketCollectionJob = null
        _purchasedTickets.value = emptyList()
        _isLoadingTickets.value = true
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

            val result = createPendingTicketsUseCase(
                event = event,
                scheduleIndex = scheduleIndex,
                selectedTickets = selectedTickets,
                orderCode = orderCode,
                expiresAtMillis = expiresAtMillis
            )

            _isSavingPayment.value = false
            result
                .onSuccess { onSaved() }
                .onFailure { exception ->
                    _paymentError.value = exception.message ?: "Không thể lưu vé đang chờ thanh toán lên Firebase"
                }
        }
    }

    fun saveUserOrder(orderData: Map<String, Any>, onSaved: () -> Unit) {
        viewModelScope.launch {
            _isSavingPayment.value = true
            _paymentError.value = null
            val result = saveUserOrderUseCase(orderData)
            _isSavingPayment.value = false
            result
                .onSuccess { onSaved() }
                .onFailure { exception ->
                    _paymentError.value = exception.message ?: "Không thể lưu thông tin đơn hàng"
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

            val result = completePendingTicketsUseCase(
                orderCode = orderCode,
                phoneNumber = phoneNumber,
                paymentMethod = paymentMethod
            )

            _isSavingPayment.value = false
            result
                .onSuccess {
                    // Tự động xóa sự kiện khỏi yêu thích và tạo notification sau khi mua vé thành công
                    val uid = getCurrentUserUidUseCase()
                    if (uid.isNotEmpty()) {
                        viewModelScope.launch {
                            runCatching { removeFavoriteUseCase(uid, event.id) }
                            
                            val userDetail = getCurrentUserDetailUseCase()
                            val isBookingNotifEnabled = if (userDetail is huce.fit.myezticket.core.common.Resource.Success) {
                                userDetail.data.bookingNotificationEnabled
                            } else {
                                true
                            }
                            if (isBookingNotifEnabled) {
                                runCatching {
                                    createNotificationUseCase(
                                        notification = AppNotification(
                                            uid = uid,
                                            eventId = event.id,
                                            eventName = event.name,
                                            eventImageUrl = event.image_url,
                                            title = "Thanh toán thành công",
                                            body = "Đơn $orderCode đã thanh toán thành công. Vé đã được lưu vào Vé của tôi.",
                                            type = "PAYMENT_SUCCESS",
                                            createdAtMillis = System.currentTimeMillis()
                                        ),
                                        docId = "${uid}_payment_$orderCode"
                                    )
                                }
                            }
                            
                            // Gọi onSaved sau khi các tiến trình background đã xử lý xong hoặc đưa vào background task
                            kotlinx.coroutines.Dispatchers.Main.dispatch(kotlin.coroutines.EmptyCoroutineContext, Runnable {
                                onSaved()
                            })
                        }.join() // Đảm bảo mọi thứ hoàn thành trước khi gọi onSaved() trong current coroutine.
                    } else {
                        onSaved()
                    }
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
            cancelPendingTicketsUseCase(orderCode)
                .onFailure { exception ->
                    _paymentError.value = exception.message ?: "Không thể cập nhật vé đã hủy lên Firebase"
                }
            onFinished()
        }
    }

    override fun onCleared() {
        syncCleanup?.invoke()
        super.onCleared()
    }
}
