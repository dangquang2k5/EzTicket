package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.model.PurchasedTicket
import huce.fit.myezticket.domain.repository.FavoriteRepository
import huce.fit.myezticket.domain.repository.TicketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketViewModel @Inject constructor(
    private val repository: TicketRepository,
    private val favoriteRepository: FavoriteRepository,
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

    fun savePurchasedTickets(
        event: Event,
        scheduleIndex: Int,
        selectedTickets: Map<String, Int>,
        phoneNumber: String,
        orderCode: String,
        paymentMethod: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            _isSavingPayment.value = true
            _paymentError.value = null

            val result = repository.createPurchasedTickets(
                event = event,
                scheduleIndex = scheduleIndex,
                selectedTickets = selectedTickets,
                phoneNumber = phoneNumber,
                orderCode = orderCode,
                paymentMethod = paymentMethod
            )

            _isSavingPayment.value = false

            result
                .onSuccess {
                    // Tự động xóa sự kiện khỏi yêu thích sau khi mua vé thành công
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        runCatching { favoriteRepository.removeFavorite(uid, event.id) }
                    }
                    onSaved()
                }
                .onFailure { exception ->
                    _paymentError.value = exception.message ?: "Không thể lưu vé lên Firebase"
                }
        }
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}
