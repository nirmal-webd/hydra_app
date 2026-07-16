package com.hydra.app.utils

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    /**
     * Returns the start and end timestamps (epoch millis) for today.
     */
    fun todayRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    /**
     * Returns the start and end timestamps (epoch millis) for a specific date.
     */
    fun dayRange(date: LocalDate): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }

    /**
     * Formats a timestamp to a human-readable time string (e.g., "2:34 PM").
     */
    fun formatTime(timestampMillis: Long): String {
        val instant = java.time.Instant.ofEpochMilli(timestampMillis)
        val time = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        return time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }

    /**
     * Formats a timestamp to a date string (e.g., "July 16, 2026").
     */
    fun formatDate(timestampMillis: Long): String {
        val instant = java.time.Instant.ofEpochMilli(timestampMillis)
        val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault()))
    }

    /**
     * Returns the LocalDate for a timestamp.
     */
    fun toLocalDate(timestampMillis: Long): LocalDate {
        return java.time.Instant.ofEpochMilli(timestampMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    /**
     * Parses a time string ("HH:mm") to LocalTime.
     */
    fun parseTime(timeString: String): LocalTime {
        return LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm"))
    }

    /**
     * Checks if the current time is within quiet hours.
     */
    fun isInQuietHours(startTime: String, endTime: String): Boolean {
        val now = LocalTime.now()
        val start = parseTime(startTime)
        val end = parseTime(endTime)

        return if (start <= end) {
            // Same day range (e.g., 08:00 to 20:00)
            now in start..end
        } else {
            // Overnight range (e.g., 22:00 to 07:00)
            now >= start || now <= end
        }
    }
}
