package com.example.hack24

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.compose.ui.tooling.preview.Preview
import com.example.hack24.ui.theme.Hack24Theme
import android.provider.CalendarContract
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import java.util.Calendar
import java.text.SimpleDateFormat
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import java.util.Locale
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch


var date = 0
var enddate = 365

data class Event(
    val id: Long,
    val title: String,
    val startTime: Long,
    val endTime: Long
)

val eventMap = mutableMapOf<Long, MutableList<Event>>()

class MainActivity : ComponentActivity() {
    private val CALENDAR_PERMISSION_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Hack24Theme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                        DailyEventPager(eventMap)
                    } else {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALENDAR), CALENDAR_PERMISSION_CODE)
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_CALENDAR), CALENDAR_PERMISSION_CODE)
                        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) || ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED))) {
                            setContent {
                                Text(
                                    text = "HELLO YOU NEED TO GIVE CALENDAR PERMISSIONS TO USE THIS APP!!!"
                                )
                            }
                        }
                        else {
                            DailyEventPager(eventMap)
                        }
                    }
                }
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALENDAR), CALENDAR_PERMISSION_CODE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_CALENDAR), CALENDAR_PERMISSION_CODE)
        }
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) || ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED))) {
            setContent {
                Text(
                    text = "HELLO YOU NEED TO GIVE CALENDAR PERMISSIONS TO USE THIS APP!!!"
                )
            }
        }
        else {
            queryCalendarEvents()
        }
    }


    // Function to query calendar events
    private fun queryCalendarEvents() {
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND
        )
        var eventDate = 0
        // Specify date range for the targeted day
        val calendarstart = Calendar.getInstance()
        calendarstart.set(Calendar.HOUR_OF_DAY, 0)
        calendarstart.set(Calendar.MINUTE, 0)
        calendarstart.set(Calendar.SECOND, 0)
        calendarstart.set(Calendar.YEAR, 2024)
        calendarstart.set(Calendar.DAY_OF_YEAR, date)
        val calendarend = Calendar.getInstance()
        calendarend.set(Calendar.HOUR_OF_DAY, 0)
        calendarend.set(Calendar.MINUTE, 0)
        calendarend.set(Calendar.SECOND, 0)
        calendarend.set(Calendar.YEAR, 2024)
        calendarend.set(Calendar.DAY_OF_YEAR, enddate)
        val endTime = calendarend.timeInMillis
        val startTime = calendarstart.timeInMillis
        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTEND} <= ?"
        val selectionArgs = arrayOf(startTime.toString(), endTime.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        val cursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )


        cursor?.apply {


            // Get column indices once before the loop to avoid recalculating them repeatedly
            val eventIdIndex = getColumnIndex(CalendarContract.Events._ID)
            val eventTitleIndex = getColumnIndex(CalendarContract.Events.TITLE)
            val eventStartTimeIndex = getColumnIndex(CalendarContract.Events.DTSTART)
            val eventEndTimeIndex = getColumnIndex(CalendarContract.Events.DTEND)

            if (eventIdIndex == -1 || eventTitleIndex == -1 || eventStartTimeIndex == -1 || eventEndTimeIndex == -1) {
                Log.e("CalendarEvent", "One or more essential columns are missing from the cursor result set")
                return@apply
            }

            // Loop through the cursor data
            while (moveToNext()) {
                val eventId = getLong(eventIdIndex)
                val eventTitle = getString(eventTitleIndex)
                val eventStartTime = getLong(eventStartTimeIndex)
                val eventEndTime = getLong(eventEndTimeIndex)

                // Process the event details
                val event = Event(eventId, eventTitle, eventStartTime, eventEndTime)

                // Assuming the date key for grouping is the start date without time
                val eventDate = eventStartTime / (1000 * 60 * 60 * 24) // Convert ms to days

                // Add the Event object to the map, grouping by date
                if (eventMap[eventDate] == null) {
                    eventMap[eventDate] = mutableListOf()
                }
                eventMap[eventDate]?.add(event)
            }

            // Now you can use the eventMap where each key is a day and value is a list of events of that day
        }

    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun DailyEventPager(eventMap: Map<Long, List<Event>>) {
    val pagerState = rememberPagerState()

    LaunchedEffect(pagerState.currentPage) {
        pagerState.animateScrollToPage(pagerState.currentPage)
    }

    val coroutineScope = rememberCoroutineScope()
    val dates = eventMap.keys.sorted() // Ensure the dates are sorted

    HorizontalPager(
        count = dates.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val selectedDate = dates.elementAtOrNull(page) ?: return@HorizontalPager // Get the correct date based on the current page
        val events = eventMap[selectedDate] ?: emptyList()
        DayEventsScreen(selectedDate, events)
    }

    // Navigation Row for previous and next day
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Button(onClick = {
            coroutineScope.launch {
                if (pagerState.currentPage > 0) {
                    pagerState.scrollToPage(pagerState.currentPage - 1)
                }
            }
        }) {
            Text("Previous")
        }

        Button(onClick = {
            coroutineScope.launch {
                if (pagerState.currentPage < dates.size - 1) {
                    pagerState.scrollToPage(pagerState.currentPage + 1)
                }
            }
        }) {
            Text("Next")
        }
    }
}




@Composable
fun DayEventsScreen(day: Long, events: List<Event>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Events on $day", style = MaterialTheme.typography.displayLarge)
        LazyColumn {
            items(events) { event ->
                EventItem(event)
            }
        }
    }
}


@Composable
fun EventItem(event: Event) {
    val simpleDateFormat = SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Event: ${event.title}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Start: ${simpleDateFormat.format(event.startTime)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "End: ${simpleDateFormat.format(event.endTime)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Hack24Theme {
        Greeting("Android")
    }
}