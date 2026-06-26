package com.elima.installment_management.util

import ir.huri.jcal.JalaliCalendar
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

object DateUtils {
    fun toPersianDate(date: LocalDate): String {
        val jalali = JalaliCalendar(GregorianCalendar(date.year, date.monthValue - 1, date.dayOfMonth))
        val format = NumberFormat.getInstance(Locale("fa", "IR"))
        format.isGroupingUsed = false
        val y = format.format(jalali.year)
        val m = String.format(Locale("fa", "IR"), "%02d", jalali.month)
        val d = String.format(Locale("fa", "IR"), "%02d", jalali.day)
        return "$y/$m/$d"
    }

    fun formatNumber(number: Any): String {
        val format = NumberFormat.getInstance(Locale("fa", "IR"))
        return format.format(number)
    }

    fun getRemainingDaysText(dueDate: LocalDate): String {
        val today = LocalDate.now()
        val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate)
        val formattedDays = formatNumber(Math.abs(daysBetween))

        return when {
            daysBetween > 0 -> "($formattedDays روز مانده)"
            daysBetween < 0 -> "($formattedDays روز گذشته)"
            else -> "(امروز)"
        }
    }
}
