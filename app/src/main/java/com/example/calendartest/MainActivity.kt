package com.example.calendartest

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import java.util.*
import android.Manifest

//Add to calendar function
fun calendar(context: Context, editEventName: EditText, editTextDate: EditText, editTextTime: EditText, editEmail: EditText) {
    // Retrieve event details from EditText fields
    val eventName = editEventName.text.toString()
    val eventDate = editTextDate.text.toString() // yyyy-mm-dd
    val eventTime = editTextTime.text.toString() // hh:mm
    val eventEmail = editEmail.text.toString()

    // Calculate start time in milliseconds
    //How this works is that the code is scraping specific indeces from the eventDate and
    // eventTime variables, and then converting that with the function timeInMillis
    val startMillis: Long = Calendar.getInstance().apply {
        set(Calendar.YEAR, eventDate.substring(0, 4).toInt())
        set(Calendar.MONTH, eventDate.substring(5, 7).toInt() - 1)
        set(Calendar.DAY_OF_MONTH, eventDate.substring(8).toInt())
        set(Calendar.HOUR_OF_DAY, eventTime.substring(0, 2).toInt())
        set(Calendar.MINUTE, eventTime.substring(3).toInt())
    }.timeInMillis

    // Calculate end time (1 hour after start time)
    val endMillis = startMillis + (60 * 60 * 1000)

    // Query available calendars
    //Here, we are creating a cursor, which uses contentResolver.query to get all the calendars
    // based on their content_uri and creates an array of the calendars and their display name
    val calendarCursor = context.contentResolver.query(
        CalendarContract.Calendars.CONTENT_URI,
        arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME),
        null, null, null
    )

    var calendarID = 4 // Default calendar ID

    //Here, if calendarCursor is not empty, it goes through each of the calendars, gets the ID
    // and display name (think of calendarCursor as a list of lists) and logs the calendar names.
    // This is just a double check to make sure that our calendarID is good to go.
    try {
        if (calendarCursor != null) {
            while (calendarCursor.moveToNext()) {
                val id = calendarCursor.getLong(0)
                val displayName = calendarCursor.getString(1)
                Log.i("CalendarTest", "Calendar ID: $id, Display Name: $displayName")
            }
        }
    } finally {
        calendarCursor?.close()
    }

    // Prepare values for the calendar event using .apply
    val values = ContentValues().apply {
        put(CalendarContract.Events.DTSTART, startMillis)
        put(CalendarContract.Events.DTEND, endMillis)
        put(CalendarContract.Events.TITLE, eventName)
        put(CalendarContract.Events.DESCRIPTION, "Testing app")
        put(CalendarContract.Events.CALENDAR_ID, calendarID)
        put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles")
    }

    // Insert the event into the calendar
    //the first line is just to get the contentResolver function up
    val contentResolver = context.contentResolver

    //creates the uri for the calendar event
    val uri: Uri? = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

    // Retrieve the eventID from the inserted event URI
    val eventID: Long? = uri?.lastPathSegment?.toLong()

    if (eventID != null) {
        //double checks that the event was actually created
        Log.i("CalendarTest", "Event created with eventID $eventID")

        // Verify the event creation
        //eventCursor is very similar to the calendarCursor above
        val eventCursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            arrayOf(CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART, CalendarContract.Events.DTEND),
            "${CalendarContract.Events._ID} = ?",
            arrayOf(eventID.toString()),
            null
        )

        try {
            //verification for the eventCursor and ensuring that the event was created AND found
            if (eventCursor != null) {
                if (eventCursor.moveToFirst()) {
                    val title = eventCursor.getString(0)
                    val startTime = eventCursor.getLong(1)
                    val endTime = eventCursor.getLong(2)
                    Log.i("CalendarTest", "Verified Event - Title: $title, Start: $startTime, End: $endTime")
                } else {
                    Log.i("CalendarTest", "Event not found after insertion")
                }
            }
        } finally {
            eventCursor?.close()
        }

        // Add attendee values
        val attendeeValues = ContentValues().apply {
            put(CalendarContract.Attendees.ATTENDEE_NAME, "Test person")
            put(CalendarContract.Attendees.ATTENDEE_EMAIL, eventEmail)
            put(CalendarContract.Attendees.ATTENDEE_TYPE, CalendarContract.Attendees.TYPE_REQUIRED)
            put(CalendarContract.Attendees.ATTENDEE_STATUS, CalendarContract.Attendees.ATTENDEE_STATUS_INVITED)
            put(CalendarContract.Attendees.EVENT_ID, eventID) // Associate attendee with the event
        }

        // Insert the attendee into the calendar
        contentResolver.insert(CalendarContract.Attendees.CONTENT_URI, attendeeValues)

        Log.i("CalendarTest", "Attendee added")
    } else {
        Log.e("CalendarTest", "Failed to create event")
    }
}


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    //Get all the variables - event name, date, time, and attendee email form the xml
    private lateinit var editEventName: EditText
    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText
    private lateinit var editEmail: EditText

    //main function - on the creation of the app, this is what happens
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //Requesting permissions
        //Request permission needs to be OUTSIDE the button event. This is because we are trying
        // to registerForActivityResult, and it must be called before the 'Activity' or
        // 'Fragment' is fully resumed.

        //when I try to register the activity result inside of View.OnClickListener, the
        // 'Activity' is already in the 'RESUMED' state, leading to an 'IllegalStateException'

        //This is the main function for requesting permission, gets true if permission is granted
        // and false if the permission is denied. Pulled directly from Android Studio's tutorial
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                    isGranted: Boolean ->
                if (isGranted) {
                    Log.i("Permission: ", "Granted")
                } else {
                    Log.i("Permission: ", "Denied")
                }
            }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //request permission function -> first checks if permissions are already granted, and if
        // they are not, asks for permissions
        fun requestPermission() {
            when {
                checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager
                    .PERMISSION_GRANTED && checkSelfPermission(this, Manifest.permission
                        .READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                -> {
                    calendar(this, editEventName, editTextDate, editTextTime, editEmail)
                } else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
                requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
            }
            }
        }

        //this is the button event - on the click of the button, this happens
        //requestPermission() has calendar() in-built
        val buttonCreateEvent: Button = findViewById(R.id.buttonCreateEvent)
        buttonCreateEvent.setOnClickListener {
            requestPermission()
        }

        editEventName = findViewById(R.id.editEventName)
        editTextDate = findViewById(R.id.editTextDate)
        editTextTime = findViewById(R.id.editTextTime)
        editEmail = findViewById(R.id.editEmail)

        editEventName.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                Log.i(TAG, "afterTextChanged $p0")
            }
        })

        editTextDate.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                Log.i(TAG, "afterTextChanged $p0")
            }
        })

        editTextTime.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                Log.i(TAG, "afterTextChanged $p0")
            }
        })

        editEmail.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                Log.i(TAG, "afterTextChanged $p0")
            }
        })

    }
}

