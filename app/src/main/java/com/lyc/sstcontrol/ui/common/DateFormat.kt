package com.lyc.sstcontrol.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.firebase.Timestamp
import com.lyc.sstcontrol.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun Timestamp.toLocalDateTime(): LocalDateTime =
    Instant.ofEpochSecond(seconds).atZone(ZoneId.systemDefault()).toLocalDateTime()

@Composable
fun formatAssignedAt(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val dateTime = timestamp.toLocalDateTime()
    val today = LocalDate.now()
    val day = when (dateTime.toLocalDate()) {
        today -> stringResource(R.string.date_today)
        today.minusDays(1) -> stringResource(R.string.date_yesterday)
        else -> dateTime.format(dateFormatter)
    }
    return day + ", " + dateTime.format(timeFormatter)
}

@Composable
fun formatDueDate(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val date = timestamp.toLocalDateTime().toLocalDate()
    val today = LocalDate.now()
    return when (date) {
        today -> stringResource(R.string.date_today)
        today.plusDays(1) -> stringResource(R.string.date_tomorrow)
        else -> date.format(dateFormatter)
    }
}