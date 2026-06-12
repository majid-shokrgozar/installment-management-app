package com.elima.installment_management.util

import ir.huri.jcal.JalaliCalendar
import java.time.LocalDate
import java.util.GregorianCalendar

object DateUtils {
    fun toPersianDate(date: LocalDate): String {
        val jalali = JalaliCalendar(GregorianCalendar(date.year, date.monthValue - 1, date.dayOfMonth))
        return "${jalali.year}/${String.format("%02d", jalali.month)}/${String.format("%02d", jalali.day)}"
    }
}
