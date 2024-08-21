package com.example.calendartest

import android.provider.CalendarContract

//this is a helper function that allows us to easily interpret events. Think of it as writing
// functions in Python so that they can be used elsewhere.
class CalendarUtil {
    companion object {
        fun getEventStatus(status: Int?): String {
            return when (status) {
                CalendarContract.Events.STATUS_TENTATIVE -> "Tentative"
                CalendarContract.Events.STATUS_CONFIRMED -> "Confirmed"
                CalendarContract.Events.STATUS_CANCELED -> "Canceled"
                else -> "Unknown"
            }
        }

        fun getEventAvailability(availability: Int?): String {
            return when (availability) {
                CalendarContract.Events.AVAILABILITY_FREE -> "Free"
                CalendarContract.Events.AVAILABILITY_BUSY -> "Busy"
                CalendarContract.Events.AVAILABILITY_TENTATIVE -> "Tentative"
                else -> "Unknown"
            }
        }
    }
}