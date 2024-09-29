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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
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

    val monthListState = rememberLazyListState()
    val dayListState = rememberLazyListState()

    val months = Month.entries.map { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
    val daysInMonth = remember(selectedDate.month, selectedDate.year) {
        (1..YearMonth.of(selectedDate.year, selectedDate.month).lengthOfMonth()).toList()
    }

    val bufferSpace = 3

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(initialDate) {
        coroutineScope.launch {
            val monthScrollIndex = (initialDate.monthValue - 1) + bufferSpace
            val dayScrollIndex = (initialDate.dayOfMonth - 1) + bufferSpace

            monthListState.centerItem(monthScrollIndex)
            dayListState.centerItem(dayScrollIndex)
        }
    }


    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        SelectorRow(
            items = months,
            listState = monthListState,
            selectedIndex = selectedDate.monthValue - 1,
            onItemSelected = { monthIndex ->
                val newDate = selectedDate.withMonth(monthIndex + 1)
                selectedDate = newDate
                onDateChange(newDate)
            },
            coroutineScope = coroutineScope,
            bufferSpace = bufferSpace,
        )

        Spacer(modifier = Modifier.height(16.dp))

        SelectorRow(
            items = daysInMonth.map { it.toString() },
            listState = dayListState,
            selectedIndex = selectedDate.dayOfMonth - 1,
            onItemSelected = { dayIndex ->
                val newDate = selectedDate.withDayOfMonth(dayIndex + 1)
                selectedDate = newDate
                onDateChange(newDate)
            },
            coroutineScope = coroutineScope,
            bufferSpace = bufferSpace,
        )
    }

    LaunchedEffect(monthListState.isScrollInProgress) {
        if (!monthListState.isScrollInProgress) {
            coroutineScope.launch {
                val centeredMonthIndex = getCenteredItemIndex(monthListState) - bufferSpace
                if (centeredMonthIndex in months.indices) {
                    val newDate = selectedDate.withMonth(centeredMonthIndex + 1)
                    if (newDate != selectedDate) {
                        selectedDate = newDate
                        onDateChange(newDate)
                    }
                }
            }
        }
    }

    LaunchedEffect(dayListState.isScrollInProgress) {
        if (!dayListState.isScrollInProgress) {
            coroutineScope.launch {
                val centeredDayIndex = getCenteredItemIndex(dayListState) - bufferSpace
                if (centeredDayIndex in daysInMonth.indices) {
                    val newDate = selectedDate.withDayOfMonth(centeredDayIndex + 1)
                    if (newDate != selectedDate) {
                        selectedDate = newDate
                        onDateChange(newDate)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectorRow(
    items: List<String>,
    listState: LazyListState,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    coroutineScope: CoroutineScope,
    bufferSpace: Int = 0
) {
    Box(
        contentAlignment = Alignment.Center, modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        CenterOverlayBox()
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(bufferSpace) { Spacer(modifier = Modifier.width(40.dp)) }
            items(items.size) { index ->
                val itemText = items[index]
                Text(
                    text = itemText,
                    fontSize = if (index == selectedIndex) 20.sp else 16.sp,
                    color = if (index == selectedIndex) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 16.dp)
                        .clickable {
                            coroutineScope.launch {
                                onItemSelected(index)
                                listState.centerItem(index)
                            }
                        }
                )
            }
            items(bufferSpace) { Spacer(modifier = Modifier.width(40.dp)) }
        }
    }
}

@Composable
private fun CenterOverlayBox() {
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(40.dp)
            .background(
                Color.LightGray.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.small
            )
    )
}

suspend fun LazyListState.centerItem(index: Int) {
    val visibleItems = layoutInfo.visibleItemsInfo

    if (visibleItems.isNotEmpty()) {
        val parentWidth = layoutInfo.viewportEndOffset
        val itemWidth = visibleItems.first().size
        val offset = (parentWidth - itemWidth) / 2
        scrollToItem(index, scrollOffset = -offset)
    }
}


private fun getCenteredItemIndex(listState: LazyListState): Int {
    val visibleItems = listState.layoutInfo.visibleItemsInfo
    return if (visibleItems.isNotEmpty()) {
        val center = listState.layoutInfo.viewportEndOffset / 2
        visibleItems.minByOrNull { kotlin.math.abs((it.offset + it.size / 2) - center) }?.index ?: 0
    } else 0
}

@Composable
@Preview(showBackground = true)
fun DateSelectorWidgetPreview() {
    DateSelectorWidget(
        initialDate = LocalDate.of(2024, 6, 20),
        onDateChange = {}
    )
}
