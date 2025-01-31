package com.col.eventradar.models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object EventModel {
    /**
     * An array of sample (placeholder) items.
     */
    val EVENTS_DATA: MutableList<Event> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
    private val EVENTS_MAP: MutableMap<String, Event> = HashMap()

    private const val COUNT = 25

    private val usernames =
        listOf("Alice", "Bob", "Charlie", "David", "Emma", "Liam", "Sophia", "Mason")
    private val commentsPool =
        listOf(
            "Amazing event! Loved it.",
            "Looking forward to the next one.",
            "Not what I expected, but still fun.",
            "Great atmosphere and people!",
            "Wish it lasted longer!",
            "The location was perfect.",
            "Had a great time with friends!",
            "Would definitely recommend this event.",
            "Can't wait for next year!",
        )

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createEventItem(i))
        }
    }

    private fun addItem(item: Event) {
        EVENTS_DATA.add(item)
        EVENTS_MAP.put(item.id, item)
    }

    private fun createEventItem(position: Int): Event {
        val isoDate = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) // ISO 8601 format

        return Event(
            id = position.toString(),
            title = "Item $position",
            location = Location(34.2, 34.2),
            locationName = "Tel Aviv",
            time = LocalDateTime.now(), // Now in ISO format
            type = EventType.EarthQuake,
            description = makeDetails(position),
            comments = makeComments(position.toString(), count = (1..5).random()),
        )
    }

    private fun makeDetails(position: Int): String {
        val lorem =
            listOf(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
                "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
            )

        return buildString {
            append("Details about Item: $position\n")
            repeat(position) { append("${lorem[it % lorem.size]}\n") }
        }
    }

    fun makeComments(
        eventId: String,
        count: Int = 5,
    ): List<Comment> {
        val random = Random(eventId.hashCode())
        return List(count) {
            Comment(
                content = commentsPool[random.nextInt(commentsPool.size)],
                username = usernames[random.nextInt(usernames.size)],
                time =
                    LocalDateTime
                        .now()
                        .minusMinutes(random.nextLong(60)), // Random time within the last hour
            )
        }
    }
}
