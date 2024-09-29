package com.labwhisper.floodview.date

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun convertToDateTimeString(date: LocalDate): String {
    return date.atTime(23, 59, 59)
        .atOffset(ZoneOffset.UTC)
        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
}
