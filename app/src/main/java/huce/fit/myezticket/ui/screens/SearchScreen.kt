package huce.fit.myezticket.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import huce.fit.myezticket.ui.components.EventCard
import huce.fit.myezticket.ui.viewmodel.EventViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    eventViewModel: EventViewModel,
    onBackClick: () -> Unit,
    onEventClick: (String) -> Unit
) {
    // 1. Reset tìm kiếm mỗi khi vào màn hình
    LaunchedEffect(Unit) {
        eventViewModel.resetSearch()
    }

    val searchQuery by eventViewModel.searchQuery.collectAsState()
    val filteredEvents by eventViewModel.filteredEvents.collectAsState()
    val recentSearches by eventViewModel.recentSearches.collectAsState()
    val categories by eventViewModel.categories.collectAsState()
    val selectedCategory by eventViewModel.selectedCategory.collectAsState()
    val selectedDateFilter by eventViewModel.selectedDateFilter.collectAsState()
    val dateFilters = eventViewModel.dateFilters

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showDateMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background // Đồng bộ màu nền với HomeScreen
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            item {
                // HEADER (Quay lại + Tiêu đề)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tìm kiếm",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ô NHẬP TÌM KIẾM
                TextField(
                    value = searchQuery,
                    onValueChange = { eventViewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập tên sự kiện, địa điểm...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            eventViewModel.addToRecentSearches(searchQuery)
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Gray,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // HÀNG BỘ LỌC
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Nút "Ngày"
                    Box {
                        SuggestionChip(
                            onClick = { showDateMenu = true },
                            icon = { Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp)) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(selectedDateFilter, color = MaterialTheme.colorScheme.onSurface)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = null,
                            shape = RoundedCornerShape(20.dp)
                        )
                        DropdownMenu(
                            expanded = showDateMenu,
                            onDismissRequest = { showDateMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            dateFilters.forEach { dateFilter ->
                                DropdownMenuItem(
                                    text = { Text(dateFilter, color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        eventViewModel.onDateFilterSelect(dateFilter)
                                        showDateMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Nút "Bộ lọc" (Lọc theo thể loại)
                    Box {
                        SuggestionChip(
                            onClick = { showCategoryMenu = true },
                            icon = { Icon(Icons.Default.FilterList, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp)) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(selectedCategory, color = MaterialTheme.colorScheme.onSurface)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = null,
                            shape = RoundedCornerShape(20.dp)
                        )
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        eventViewModel.onCategorySelect(category)
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // HIỂN THỊ KHI CHƯA NHẬP TÌM KIẾM VÀ CHƯA CÓ LỌC
            if (searchQuery.isEmpty() && selectedCategory == "Tất cả" && selectedDateFilter == "Tất cả các ngày") {
                // Lịch sử "Gần đây"
                if (recentSearches.isNotEmpty()) {
                    item {
                        Text("Gần đây", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(recentSearches, key = { it }) { text ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { 
                                    eventViewModel.onSearchQueryChange(text)
                                    eventViewModel.addToRecentSearches(text)
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = text, color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp)
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }

                item {
                    // Khám phá thể loại (Lấy từ dữ liệu thực tế)
                    Text("Khám phá thể loại", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Chia categories thành từng hàng 2 cột
                    val categoryList = categories.filter { it != "Tất cả" }
                    val chunkedCategories = categoryList.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        chunkedCategories.forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { category ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                eventViewModel.onCategorySelect(category)
                                                eventViewModel.addToRecentSearches(category)
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = category,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                // Nếu hàng chỉ có 1 item, thêm Spacer để cân bằng
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            } else {
                // HIỂN THỊ KẾT QUẢ TÌM KIẾM
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val titleText = if (searchQuery.isNotEmpty()) "Kết quả cho \"$searchQuery\"" else "Kết quả lọc"
                        Text(
                            text = titleText,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${filteredEvents.size} kết quả",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (filteredEvents.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Không tìm thấy sự kiện nào phù hợp.", color = Color.Gray)
                        }
                    }
                } else {
                    items(filteredEvents, key = { it.id }) { event ->
                        EventCard(
                            event = event,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            onEventClick = {
                                if (searchQuery.isNotBlank()) eventViewModel.addToRecentSearches(searchQuery)
                                onEventClick(it)
                            }
                        )
                    }
                }
            }
        }
    }
}
