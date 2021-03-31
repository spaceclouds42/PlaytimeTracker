package us.spaceclouds42.playtime_tracker.extension

fun Long.prettyPrint(): String {
    val seconds = this / 1000L

    var minutes = seconds / 60L

    var hours = minutes / 60L
    minutes -= (hours * 60L)

    val days = hours / 24L
    hours -= (days * 24L)


    var prettyString = ""

    if (days == 1L) {
        prettyString += " 1 day"
    } else if (days > 0) {
        prettyString += " $days days"
    }

    if (hours == 1L) {
        prettyString += " 1 hour"
    } else if (hours > 0) {
        prettyString += " $hours hours"
    }

    if (minutes == 1L) {
        prettyString += " 1 minute"
    } else if (minutes > 0) {
        prettyString += " $minutes minutes"
    }

    return if (prettyString.isNotEmpty()) {
        prettyString.substring(1)
    } else {
        "less than 1 minute"
    }
}