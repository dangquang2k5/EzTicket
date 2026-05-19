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

    // BỘ LỌC NGÀY
    val dateFilters = listOf("Tất cả các ngày", "Hôm nay", "Tuần này", "Tháng này")
    
    private val _selectedDateFilter = MutableStateFlow("Tất cả các ngày")
    val selectedDateFilter: StateFlow<String> = _selectedDateFilter.asStateFlow()

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

    fun onDateFilterSelect(dateFilter: String) {
        _selectedDateFilter.value = dateFilter
        applyFilters()
    }

    private fun applyFilters() {
        val query = _searchQuery.value
        val category = _selectedCategory.value
        val dateFilter = _selectedDateFilter.value

        // Guard: nếu events chưa load, không làm gì
        if (_events.value.isEmpty()) return

        val now = java.util.Calendar.getInstance()
        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEnd = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
        }.timeInMillis

        // Start of week (Monday)
        val weekStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val weekEnd = java.util.Calendar.getInstance().apply {
            timeInMillis = weekStart
            add(java.util.Calendar.DAY_OF_YEAR, 6)
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
        }.timeInMillis

        // Start of month
        val monthStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthEnd = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
        }.timeInMillis

        _filteredEvents.value = _events.value.filter { event ->
            val matchesQuery = event.name.contains(query, ignoreCase = true) ||
                    event.location.contains(query, ignoreCase = true)
            val matchesCategory = category == "Tất cả" || event.category == category
            
            val matchesDate = if (dateFilter == "Tất cả các ngày") {
                true
            } else {
                // Wrap trong try-catch: tránh crash khi Timestamp từ Firebase bị lỗi
                try {
                    event.schedules.any { schedule ->
                        val scheduleTime = schedule.date?.toDate()?.time ?: 0L
                        when (dateFilter) {
                            "Hôm nay" -> scheduleTime in todayStart..todayEnd
                            "Tuần này" -> scheduleTime in weekStart..weekEnd
                            "Tháng này" -> scheduleTime in monthStart..monthEnd
                            else -> true
                        }
                    }
                } catch (e: Exception) {
                    // Nếu Timestamp lỗi, bỏ qua filter ngày
                    true
                }
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
        _selectedDateFilter.value = "Tất cả các ngày"
        _filteredEvents.value = _events.value
    }
}
