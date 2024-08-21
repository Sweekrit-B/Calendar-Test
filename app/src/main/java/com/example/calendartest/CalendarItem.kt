package com.example.calendartest

//This creates a data class for the calendar item. Think of it like a class in Python - I'm
// defining the variables to make them easier to access later on.
data class CalendarItem(
    val id: Long,
    val name: String?,
    val displayName: String?,
    val color: Int?,
    val visible: Boolean?,
    val syncEvents: Boolean?,
    val accountName: String?,
    val accountType: String?,
)