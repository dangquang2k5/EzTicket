package huce.fit.myezticket.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import huce.fit.myezticket.data.model.Event
import huce.fit.myezticket.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EventRepository()
    private val sharedPreferences = application.getSharedPreferences("ezticket_prefs", Context.MODE_PRIVATE)

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    private val _bannerEvents = MutableStateFlow<List<Event>>(emptyList())
    val bannerEvents: StateFlow<List<Event>> = _bannerEvents.asStateFlow()

    // TRẠNG THÁI TÌM KIẾM
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredEvents = MutableStateFlow<List<Event>>(emptyList())
    val filteredEvents: StateFlow<List<Event>> = _filteredEvents.asStateFlow()

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
        viewModelScope.launch {
            val dataFromFirebase = repository.getEvents()
            _events.value = dataFromFirebase
            _filteredEvents.value = dataFromFirebase
            _bannerEvents.value = dataFromFirebase.filter { it.isBanner }
            
            // Lấy danh sách thể loại từ data thực tế
            _categories.value = listOf("Tất cả") + dataFromFirebase.map { it.category }.distinct()
        }
    }

    private fun loadRecentSearches() {
        val savedSearches = sharedPreferences.getStringSet("recent_searches", emptySet())?.toList() ?: emptyList()
        // SharedPreferences doesn't guarantee order for StringSet, so we might want a different approach 
        // if order is critical, but for now we'll just load it. 
        // A better way is a delimited string.
        val orderedSearches = sharedPreferences.getString("recent_searches_list", "")
            ?.split("|")
            ?.filter { it.isNotBlank() } ?: emptyList()
        _recentSearches.value = orderedSearches
    }

    private fun saveRecentSearches(list: List<String>) {
        sharedPreferences.edit()
            .putString("recent_searches_list", list.joinToString("|"))
            .apply()
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
                    event.location.contains(query, ignoreCase = true)
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
