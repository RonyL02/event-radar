package com.col.eventradar.model

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

    private fun createEventItem(position: Int): Event =
        Event(
            position.toString(),
            "Item $position",
            "Item $position",
            Location(34.2, 34.2),
            city = "Tel Aviv",
            date = "10/10/2024",
            type = "Wow",
            makeDetails(position),
        )

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }
}
