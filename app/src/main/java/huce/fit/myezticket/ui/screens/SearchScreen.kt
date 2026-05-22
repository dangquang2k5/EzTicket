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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import huce.fit.myezticket.ui.components.DateRangePickerDialog
import huce.fit.myezticket.ui.components.EventCard
import huce.fit.myezticket.ui.components.PickedDate
import huce.fit.myezticket.ui.viewmodel.EventViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    eventViewModel: EventViewModel,
    onBackClick: () -> Unit,
    onEventClick: (String) -> Unit
) {
    val searchQuery by eventViewModel.searchQuery.collectAsState()
    val filteredEvents by eventViewModel.filteredEvents.collectAsState()
    val recentSearches by eventViewModel.recentSearches.collectAsState()
    val categories by eventViewModel.categories.collectAsState()
    val selectedCategory by eventViewModel.selectedCategory.collectAsState()
    val selectedDateFrom by eventViewModel.selectedDateFrom.collectAsState()
    val selectedDateTo by eventViewModel.selectedDateTo.collectAsState()

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Nhãn hiển thị cho chip ngày
    val dateLabel = when {
        selectedDateFrom != null && selectedDateTo != null -> "${selectedDateFrom} – ${selectedDateTo}"
        selectedDateFrom != null -> selectedDateFrom!!
        else -> "Tất cả các ngày"
    }
    val hasDateFilter = selectedDateFrom != null

    // Khôi phục PickedDate từ ViewModel để truyền vào dialog khi mở lại
    val initialFrom = remember(selectedDateFrom) {
        selectedDateFrom?.let { parsePickedDate(it) }
    }
    val initialTo = remember(selectedDateTo) {
        selectedDateTo?.let { parsePickedDate(it) }
    }

    // ── Custom Date Range Dialog ─────────────────────────────────────────────
    if (showDatePicker) {
        DateRangePickerDialog(
            initialFrom = initialFrom,
            initialTo = initialTo,
            onDismiss = { showDatePicker = false },
            onConfirm = { from, to ->
                showDatePicker = false
                val formattedFrom = from.format()
                if (to != null) {
                    val formattedTo = to.format()
                    eventViewModel.onDateRangeSelect(
                        from.year, from.month, from.day, formattedFrom,
                        to.year, to.month, to.day, formattedTo
                    )
                } else {
                    // Chỉ 1 ngày → lọc ngày đó (from = to)
                    eventViewModel.onDateRangeSelect(
                        from.year, from.month, from.day, formattedFrom,
                        from.year, from.month, from.day, formattedFrom
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tìm kiếm",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            item {
                // ─── Ô NHẬP TÌM KIẾM ────────────────────────────────────────────
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    tonalElevation = 2.dp
                ) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { eventViewModel.onSearchQueryChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Nhập tên sự kiện, địa điểm...",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { eventViewModel.onSearchQueryChange("") }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Xóa",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { eventViewModel.addToRecentSearches(searchQuery) }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ─── HÀNG BỘ LỌC ────────────────────────────────────────────────
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chip bộ lọc ngày → mở custom dialog
                    FilterChip(
                        selected = hasDateFilter,
                        onClick = { showDatePicker = true },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = dateLabel,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = hasDateFilter,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )

                    // Nút xóa khoảng ngày
                    if (hasDateFilter) {
                        InputChip(
                            selected = false,
                            onClick = { eventViewModel.clearDateFilter() },
                            label = { Text("Xóa ngày", fontSize = 13.sp) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Xóa ngày",
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                labelColor = MaterialTheme.colorScheme.error,
                                trailingIconColor = MaterialTheme.colorScheme.error
                            ),
                            border = InputChipDefaults.inputChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    // Chip bộ lọc thể loại
                    Box {
                        FilterChip(
                            selected = selectedCategory != "Tất cả",
                            onClick = { showCategoryMenu = true },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(selectedCategory, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory != "Tất cả",
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            category,
                                            color = if (category == selectedCategory)
                                                MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (category == selectedCategory)
                                                FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
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

            // ─── HIỂN THỊ KHI CHƯA CÓ BỘ LỌC NÀO ─────────────────────────────
            if (searchQuery.isEmpty() && selectedCategory == "Tất cả" && !hasDateFilter) {
                // Lịch sử "Gần đây"
                if (recentSearches.isNotEmpty()) {
                    item {
                        Text(
                            "Gần đây",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
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
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.History,
                                null,
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = text,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 15.sp
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.07f)
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }

                item {
                    Text(
                        "Khám phá thể loại",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val categoryList = categories.filter { it != "Tất cả" }
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        categoryList.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { category ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { eventViewModel.onCategorySelect(category) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        )
                                    ) {
                                        Text(
                                            text = category,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                // ─── HIỂN THỊ KẾT QUẢ TÌM KIẾM ─────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val titleText = when {
                            searchQuery.isNotEmpty() -> "Kết quả cho \"$searchQuery\""
                            hasDateFilter -> "Sự kiện theo ngày"
                            else -> "Kết quả lọc"
                        }
                        Text(
                            text = titleText,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "${filteredEvents.size} kết quả",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("😕", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Không tìm thấy sự kiện nào phù hợp.",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                } else {
                    val eventRows = filteredEvents.chunked(2)
                    items(eventRows) { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { event ->
                                EventCard(
                                    event = event,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = 12.dp),
                                    onEventClick = { id ->
                                        if (searchQuery.isNotBlank()) {
                                            eventViewModel.addToRecentSearches(searchQuery)
                                        }
                                        onEventClick(id)
                                    }
                                )
                            }
                            if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// Helper: parse "dd/MM/yyyy" → PickedDate
private fun parsePickedDate(formatted: String): PickedDate? {
    return try {
        val parts = formatted.split("/")
        if (parts.size != 3) return null
        val day = parts[0].toInt()
        val month = parts[1].toInt() - 1  // 0-based
        val year = parts[2].toInt()
        PickedDate(year, month, day)
    } catch (e: Exception) { null }
}
