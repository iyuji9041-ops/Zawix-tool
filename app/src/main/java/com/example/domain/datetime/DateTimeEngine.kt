package com.example.domain.datetime

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class DateDifferenceResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val totalWeeks: Long
)

data class AgeResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val nextBirthdayDays: Long,
    val nextBirthdayDayOfWeek: String
)

data class EventCountdownResult(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val isPast: Boolean
)

object DateTimeEngine {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    fun calculateDateDifference(start: LocalDate, end: LocalDate): DateDifferenceResult {
        val (earlier, later) = if (start.isAfter(end)) end to start else start to end
        val period = Period.between(earlier, later)
        val totalDays = ChronoUnit.DAYS.between(earlier, later)
        val totalWeeks = totalDays / 7

        return DateDifferenceResult(
            years = period.years,
            months = period.months,
            days = period.days,
            totalDays = totalDays,
            totalWeeks = totalWeeks
        )
    }

    fun calculateAge(birthDate: LocalDate, today: LocalDate = LocalDate.now()): AgeResult {
        require(!birthDate.isAfter(today)) { "Birth date cannot be in the future" }
        val period = Period.between(birthDate, today)
        val totalDays = ChronoUnit.DAYS.between(birthDate, today)

        var nextBirthday = birthDate.withYear(today.year)
        if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            nextBirthday = nextBirthday.plusYears(1)
        }
        val nextBirthdayDays = ChronoUnit.DAYS.between(today, nextBirthday)
        val dayOfWeek = nextBirthday.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

        return AgeResult(
            years = period.years,
            months = period.months,
            days = period.days,
            totalDays = totalDays,
            nextBirthdayDays = nextBirthdayDays,
            nextBirthdayDayOfWeek = dayOfWeek
        )
    }

    fun addOrSubtract(
        baseDate: LocalDate,
        amount: Long,
        unit: ChronoUnit,
        isAdd: Boolean
    ): LocalDate {
        val delta = if (isAdd) amount else -amount
        return baseDate.plus(delta, unit)
    }

    fun calculateCountdown(target: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): EventCountdownResult {
        val isPast = target.isBefore(now)
        val (from, to) = if (isPast) target to now else now to target

        var temp = from
        val days = ChronoUnit.DAYS.between(temp, to)
        temp = temp.plusDays(days)

        val hours = ChronoUnit.HOURS.between(temp, to)
        temp = temp.plusHours(hours)

        val minutes = ChronoUnit.MINUTES.between(temp, to)
        temp = temp.plusMinutes(minutes)

        val seconds = ChronoUnit.SECONDS.between(temp, to)

        return EventCountdownResult(days, hours, minutes, seconds, isPast)
    }

    fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}
