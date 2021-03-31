package us.spaceclouds42.playtime_tracker.extension

import com.mojang.brigadier.StringReader

const val s = 1000L
const val m = s * 60
const val h = m * 60
const val d = h * 24
const val w = d * 7

val UNITS = mapOf(
    "w" to w,
    "d" to d,
    "h" to h,
    "m" to m,
    "s" to s,
)

fun String.toTime(): Long {
    val reader = StringReader(this)

    var totalTime: Long = 0

    while (reader.canRead()) {
        reader.skipWhitespace()
        val time: Long = reader.readLong()
        val unit: String = reader.readUnquotedString()
        totalTime += time * (UNITS[unit] ?: 0)
    }

    return totalTime
}