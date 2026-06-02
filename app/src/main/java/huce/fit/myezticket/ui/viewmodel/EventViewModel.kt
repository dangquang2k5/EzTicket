package huce.fit.myezticket.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.lifecycle.HiltViewModel
import huce.fit.myezticket.domain.model.Event
import huce.fit.myezticket.domain.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 10

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _bannerEvents = MutableStateFlow<List<Event>>(emptyList())
    val bannerEvents: StateFlow<List<Event>> = _bannerEvents.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // TRẠNG THÁI TÌM KIẾM
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredEvents = MutableStateFlow<List<Event>>(emptyList())
    val filteredEvents: StateFlow<List<Event>> = _filteredEvents.asStateFlow()

    // ── PHÂN TRANG CHO SEARCH SCREEN ─────────────────────────────────────────
    // Số lượng sự kiện đang được hiển thị (tăng dần theo trang)
    private val _searchDisplayCount = MutableStateFlow(PAGE_SIZE)

    // Danh sách sự kiện đã cắt theo trang — UI chỉ render phần này
    val pagedFilteredEvents: StateFlow<List<Event>> = combine(
        _filteredEvents,
        _searchDisplayCount
    ) { events, count ->
        events.take(count)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // True khi còn sự kiện chưa được hiển thị
    val hasMoreSearchResults: StateFlow<Boolean> = combine(
        _filteredEvents,
        _searchDisplayCount
    ) { events, count ->
        events.size > count
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    // LỊCH SỬ TÌM KIẾM ĐỘNG (Persistent)
    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    // DANH SÁCH THỂ LOẠI THỰC TẾ
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tất cả")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // BỘ LỌC KHOẢNG NGÀY
    private val _selectedDateFrom = MutableStateFlow<String?>(null)
    val selectedDateFrom: StateFlow<String?> = _selectedDateFrom.asStateFlow()

    private val _selectedDateTo = MutableStateFlow<String?>(null)
    val selectedDateTo: StateFlow<String?> = _selectedDateTo.asStateFlow()

    // Lưu năm/tháng/ngày cho "từ ngày"
    private var fromYear: Int = -1
    private var fromMonth: Int = -1
    private var fromDay: Int = -1

    // Lưu năm/tháng/ngày cho "đến ngày"
    private var toYear: Int = -1
    private var toMonth: Int = -1
    private var toDay: Int = -1

    init {
        loadEvents()
        loadRecentSearches()
    }

    private fun loadEvents() {
        refreshEvents()
    }

    fun refreshEvents() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val dataFromFirebase = repository.getEvents()
                _events.value = dataFromFirebase
                _filteredEvents.value = dataFromFirebase
                _bannerEvents.value = dataFromFirebase.filter { it.isBanner }
                
                // Lấy danh sách thể loại từ data thực tế
                _categories.value = listOf("Tất cả") + dataFromFirebase.map { it.category }.distinct()
            } catch (e: Exception) {
                android.util.Log.e("EventViewModel", "Lỗi tải lại dữ liệu: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                val orderedSearches = preferences[stringPreferencesKey("recent_searches_list")] ?: ""
                _recentSearches.value = orderedSearches.split("|").filter { it.isNotBlank() }
            }
        }
    }

    private fun saveRecentSearches(list: List<String>) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("recent_searches_list")] = list.joinToString("|")
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        applyFilters()
    }

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
        applyFilters()
    }

    /**
     * Gọi khi người dùng chọn khoảng ngày từ... đến...
     */
    fun onDateRangeSelect(
        fYear: Int, fMonth: Int, fDay: Int, formattedFrom: String,
        tYear: Int, tMonth: Int, tDay: Int, formattedTo: String
    ) {
        fromYear = fYear; fromMonth = fMonth; fromDay = fDay
        toYear = tYear; toMonth = tMonth; toDay = tDay
        _selectedDateFrom.value = formattedFrom
        // Nếu chỉ chọn 1 ngày (from == to) → không lưu dateTO để nhãn chip gọn hơn
        _selectedDateTo.value = if (formattedFrom == formattedTo) null else formattedTo
        applyFilters()
    }

    fun clearDateFilter() {
        _selectedDateFrom.value = null
        _selectedDateTo.value = null
        fromYear = -1; fromMonth = -1; fromDay = -1
        toYear = -1; toMonth = -1; toDay = -1
        applyFilters()
    }

    // Load thêm PAGE_SIZE sự kiện tiếp theo cho SearchScreen
    fun loadMoreSearchResults() {
        if (_filteredEvents.value.size > _searchDisplayCount.value) {
            _searchDisplayCount.value += PAGE_SIZE
        }
    }

    private fun applyFilters() {
        val query = _searchQuery.value
        val category = _selectedCategory.value

        // Guard: nếu events chưa load, không làm gì
        if (_events.value.isEmpty()) return

        // Tính mốc thời gian cho khoảng ngày được chọn
        val hasDateRange = fromYear >= 0 && toYear >= 0
        val rangeStart: Long
        val rangeEnd: Long
        if (hasDateRange) {
            rangeStart = java.util.Calendar.getInstance().apply {
                set(fromYear, fromMonth, fromDay, 0, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            rangeEnd = java.util.Calendar.getInstance().apply {
                set(toYear, toMonth, toDay, 23, 59, 59)
            }.timeInMillis
        } else {
            rangeStart = 0L
            rangeEnd = Long.MAX_VALUE
        }

        _filteredEvents.value = _events.value.filter { event ->
            val matchesQuery = event.name.contains(query, ignoreCase = true) ||
                    event.venueName.contains(query, ignoreCase = true) ||
                    event.address.contains(query, ignoreCase = true)
            val matchesCategory = category == "Tất cả" || event.category == category

            val matchesDate = when {
                // Đã chọn khoảng ngày từ... đến...
                hasDateRange -> {
                    try {
                        event.schedules.any { schedule ->
                            val t = schedule.date?.toDate()?.time ?: return@any false
                            t in rangeStart..rangeEnd
                        }
                    } catch (e: Exception) { true }
                }
                else -> true
            }

            matchesQuery && matchesCategory && matchesDate
        }
        // Reset về trang đầu mỗi khi bộ lọc thay đổi
        _searchDisplayCount.value = PAGE_SIZE
    }

    fun addToRecentSearches(query: String) {
        if (query.isNotBlank()) {
            val currentList = _recentSearches.value.toMutableList()
            currentList.remove(query) // Đưa lên đầu nếu đã có
            currentList.add(0, query)
            if (currentList.size > 5) currentList.removeAt(5)
            _recentSearches.value = currentList
            saveRecentSearches(currentList)
        }
    }

    fun resetSearch() {
        _searchQuery.value = ""
        _selectedCategory.value = "Tất cả"
        _selectedDateFrom.value = null
        _selectedDateTo.value = null
        fromYear = -1; fromMonth = -1; fromDay = -1
        toYear = -1; toMonth = -1; toDay = -1
        _filteredEvents.value = _events.value
    }

    /**
     * Được gọi từ NavGraph khi navigate sang SearchScreen kèm danh mục cụ thể
     * (ví dụ: nhấn "Xem tất cả" từ HomeScreen).
     * Reset mọi bộ lọc, sau đó set sẵn category đã chọn.
     */
    fun setInitialCategory(category: String) {
        _searchQuery.value = ""
        _selectedDateFrom.value = null
        _selectedDateTo.value = null
        fromYear = -1; fromMonth = -1; fromDay = -1
        toYear = -1; toMonth = -1; toDay = -1
        _selectedCategory.value = category
        applyFilters()
    }
}
