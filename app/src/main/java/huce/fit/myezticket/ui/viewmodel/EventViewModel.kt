package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import huce.fit.myezticket.data.model.Event
import huce.fit.myezticket.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {

    // 1. Khai báo Quản lý kho (Repository)
    private val repository = EventRepository()

    // 2. Dòng chảy dữ liệu (StateFlow)
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _bannerEvents = MutableStateFlow<List<Event>>(emptyList())
    val bannerEvents: StateFlow<List<Event>> = _bannerEvents.asStateFlow()

    init {
        loadEvents()
    }

    // 3. Hàm gọi dữ liệu giờ đây cực kỳ ngắn gọn và sạch sẽ
    private fun loadEvents() {
        // viewModelScope.launch giúp chạy tác vụ ngầm mà không làm đơ màn hình
        viewModelScope.launch {
            // "Order" dữ liệu từ Repository và cập nhật lên UI
            val dataFromFirebase = repository.getEvents()
            _events.value = dataFromFirebase

            // Lọc ra các sự kiện có isBanner == true
            _bannerEvents.value = dataFromFirebase.filter { it.isBanner }
        }
    }
}