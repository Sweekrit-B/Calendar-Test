package com.example.calendartest

//This creates a data class for the event item. Think of it like a class in Python - I'm
// defining the variables to make them easier to access later on.
data class EventItem(
    val id: Long,
    val title: String?,
    val eventLocation: String?,
    val status: Int?,
    val dtStart: Long?,
    val dtEnd: Long?,
    val duration: String?,
    val allDay: Boolean?,
    val availability: Int?,
    val rRule: String?,
    val displayColor: Int?,
    val visible: Boolean?,
)