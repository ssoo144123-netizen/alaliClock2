package com.jalaliclock

import java.util.Calendar

object Jalali {
    private val WEEKDAYS = arrayOf("یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه")
    private val MONTHS = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    private const val PERSIAN_DIGITS = "۰۱۲۳۴۵۶۷۸۹"

    fun toPersianDigits(s: String): String = buildString {
        for (c in s) append(if (c in '0'..'9') PERSIAN_DIGITS[c - '0'] else c)
    }

    /** Gregorian -> Jalali. Returns [year, month, day]. */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 + gd + gdm[gm - 1]
        var jy = -1595 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + days / 31
            jd = 1 + days % 31
        } else {
            jm = 7 + (days - 186) / 30
            jd = 1 + (days - 186) % 30
        }
        return intArrayOf(jy, jm, jd)
    }

    fun dateString(c: Calendar): String {
        val j = gregorianToJalali(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        val weekday = WEEKDAYS[c.get(Calendar.DAY_OF_WEEK) - 1]
        return "$weekday ${toPersianDigits(j[2].toString())} ${MONTHS[j[1] - 1]} ${toPersianDigits(j[0].toString())}"
    }
}
