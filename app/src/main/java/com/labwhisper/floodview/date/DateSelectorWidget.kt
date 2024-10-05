package com.labwhisper.floodview.date

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DateSelectorWidget(
    initialDate: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    val coroutineScope = rememberCoroutineScope()
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    val startDate = initialDate.minusYears(10)
    val endDate = LocalDate.now()

    val dateRange = generateDateRange(startDate, endDate)
    val dateRangeWithDummies =
        listOf<LocalDate?>(null, null) + dateRange + listOf(null, null)

    val monthRange = generateMonthRange(startDate, endDate)
    val monthRangeWithDummies =
        listOf<MonthWithYear?>(null) + monthRange + listOf(null)

    val dayListState = rememberLazyListState()
    val monthListState = rememberLazyListState()

    val density = LocalDensity.current
    val monthItemWidthDp = 120.dp
    val dayItemWidthDp = 60.dp

    LaunchedEffect(Unit) {
        isProgrammaticScroll = true
        try {
            while (monthListState.layoutInfo.viewportEndOffset == 0) {
                delay(16L)
            }
            while (dayListState.layoutInfo.viewportEndOffset == 0) {
                delay(16L)
            }

            val initialDayIndex =
                dateRangeWithDummies.indexOfFirst { it?.isEqual(initialDate) == true }
            dayListState.centerItem(
                initialDayIndex,
                with(density) { dayItemWidthDp.toPx().toInt() })

            val initialMonthIndex = monthRangeWithDummies.indexOfFirst {
                it == MonthWithYear(
                    initialDate.month,
                    initialDate.year
                )
            }
            monthListState.centerItem(
                initialMonthIndex,
                with(density) { monthItemWidthDp.toPx().toInt() })
        } finally {
            isProgrammaticScroll = false
        }
    }

    LaunchedEffect(dayListState.isScrollInProgress) {
        if (!dayListState.isScrollInProgress && !isProgrammaticScroll) {
            coroutineScope.launch {
                val centeredDayIndex = getCenteredItemIndex(dayListState)
                if (centeredDayIndex in dateRangeWithDummies.indices) {
                    val newDate = dateRangeWithDummies[centeredDayIndex]
                    if (newDate != null && newDate != selectedDate) {
                        selectedDate = newDate
                        onDateChange(newDate)
                        isProgrammaticScroll = true
                        try {
                            val monthIndex = monthRangeWithDummies.indexOfFirst {
                                it == MonthWithYear(
                                    newDate.month,
                                    newDate.year
                                )
                            }
                            monthListState.centerItem(
                                monthIndex,
                                with(density) { monthItemWidthDp.toPx().toInt() })
                        } finally {
                            isProgrammaticScroll = false
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(monthListState.isScrollInProgress) {
        if (!monthListState.isScrollInProgress && !isProgrammaticScroll) {
            coroutineScope.launch {
                val centeredMonthIndex = getCenteredItemIndex(monthListState)
                if (centeredMonthIndex in monthRangeWithDummies.indices) {
                    val newMonthWithYear = monthRangeWithDummies[centeredMonthIndex]
                    if (newMonthWithYear != null) {
                        val newDate = selectedDate.withYear(newMonthWithYear.year)
                            .withMonth(newMonthWithYear.month.value)
                        if (newDate <= endDate) {
                            selectedDate = newDate
                            onDateChange(newDate)
                            isProgrammaticScroll = true
                            try {
                                val dayIndex =
                                    dateRangeWithDummies.indexOfFirst { it?.isEqual(newDate) == true }
                                dayListState.centerItem(
                                    dayIndex,
                                    with(density) { dayItemWidthDp.toPx().toInt() })
                            } finally {
                                isProgrammaticScroll = false
                            }
                        }
                    }
                }
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

        SelectorRow(
            items = monthRangeWithDummies.map { monthWithYear ->
                monthWithYear?.let {
                    "${
                        it.month.getDisplayName(
                            TextStyle.SHORT,
                            Locale.getDefault()
                        )
                    } ${it.year}"
                } ?: " "
            },
            listState = monthListState,
            selectedIndex = monthRangeWithDummies.indexOfFirst {
                it == MonthWithYear(
                    selectedDate.month,
                    selectedDate.year
                )
            },
            onItemSelected = { index ->
                val newMonthWithYear = monthRangeWithDummies[index]
                if (newMonthWithYear != null) {
                    val newDate = selectedDate.withYear(newMonthWithYear.year)
                        .withMonth(newMonthWithYear.month.value)
                    if (newDate <= endDate) {
                        selectedDate = newDate
                        onDateChange(newDate)
                        coroutineScope.launch {
                            isProgrammaticScroll = true
                            try {
                                val dayIndex =
                                    dateRangeWithDummies.indexOfFirst { it?.isEqual(newDate) == true }
                                dayListState.centerItem(
                                    dayIndex,
                                    with(density) { dayItemWidthDp.toPx().toInt() })
                            } finally {
                                isProgrammaticScroll = false
                            }
                        }
                    }
                }
            },
            coroutineScope = coroutineScope,
            overlayWidth = monthItemWidthDp,
            itemWidth = monthItemWidthDp
        )

        Spacer(modifier = Modifier.height(16.dp))

        SelectorRow(
            items = dateRangeWithDummies.map { date -> date?.dayOfMonth?.toString() ?: " " },
            listState = dayListState,
            selectedIndex = dateRangeWithDummies.indexOfFirst { it?.isEqual(selectedDate) == true },
            onItemSelected = { index ->
                val newDate = dateRangeWithDummies[index]
                if (newDate != null && newDate <= endDate) {
                    selectedDate = newDate
                    onDateChange(newDate)
                    coroutineScope.launch {
                        isProgrammaticScroll = true
                        try {
                            val monthIndex = monthRangeWithDummies.indexOfFirst {
                                it == MonthWithYear(
                                    newDate.month,
                                    newDate.year
                                )
                            }
                            monthListState.centerItem(
                                monthIndex,
                                with(density) { monthItemWidthDp.toPx().toInt() })
                        } finally {
                            isProgrammaticScroll = false
                        }
                    }
                }
            },
            coroutineScope = coroutineScope,
            overlayWidth = dayItemWidthDp,
            itemWidth = dayItemWidthDp
        )
    }
}

private data class MonthWithYear(val month: Month, val year: Int)

private fun generateMonthRange(startDate: LocalDate, endDate: LocalDate): List<MonthWithYear> {
    val months = mutableListOf<MonthWithYear>()
    var current = YearMonth.of(startDate.year, startDate.month)
    val end = YearMonth.of(endDate.year, endDate.month)
    while (!current.isAfter(end)) {
        months.add(MonthWithYear(current.month, current.year))
        current = current.plusMonths(1)
    }
    return months
}

private fun generateDateRange(startDate: LocalDate, endDate: LocalDate): List<LocalDate> {
    val dates = mutableListOf<LocalDate>()
    var current = startDate

    while (current <= endDate) {
        dates.add(current)
        current = current.plusDays(1)
    }

    return dates
}

private fun getCenteredItemIndex(listState: LazyListState): Int {
    val visibleItems = listState.layoutInfo.visibleItemsInfo
    return if (visibleItems.isNotEmpty()) {
        val center = listState.layoutInfo.viewportEndOffset / 2
        visibleItems.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - center) }?.index
            ?: 0
    } else 0
}

suspend fun LazyListState.centerItem(index: Int, itemWidth: Int) {
    val parentWidth = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
    if (parentWidth == 0) {
        while (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset == 0) {
            delay(16L)
        }
    }
    val offset = (parentWidth - itemWidth) / 2
    scrollToItem(index, scrollOffset = -offset)
}

@Composable
private fun SelectorRow(
    items: List<String>,
    listState: LazyListState,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    coroutineScope: CoroutineScope,
    overlayWidth: Dp,
    itemWidth: Dp
) {
    val density = LocalDensity.current
    val itemWidthPx = with(density) { itemWidth.toPx().toInt() }

    Box(
        contentAlignment = Alignment.Center, modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        CenterOverlayBox(width = overlayWidth)
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items.size) { index ->
                val itemText = items[index]
                val isSelectable = itemText.trim().isNotEmpty()
                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 16.dp)
                        .clickable(enabled = isSelectable) {
                            coroutineScope.launch {
                                onItemSelected(index)
                                listState.centerItem(index, itemWidthPx)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemText,
                        fontSize = 16.sp,
                        color = if (isSelectable) {
                            if (index == selectedIndex) MaterialTheme.colorScheme.primary else Color.Gray
                        } else {
                            Color.Transparent
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterOverlayBox(width: Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(40.dp)
            .background(
                Color.LightGray.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            )
    )
}

@Composable
@Preview(showBackground = true)
fun DateSelectorWidgetPreview() {
    DateSelectorWidget(
        initialDate = LocalDate.now(),
        onDateChange = {}
    )
}