package com.malinskiy.marathon.report.trace.chrome

import com.google.gson.annotations.SerializedName

interface TraceEvent {
    /**
     * The name of the event, as displayed in Trace Viewer
     */
    val eventName: String
    /**
     * The event categories.
     * This is a comma separated list of categories for the event.
     * The categories can be used to hide events in the Trace Viewer UI.
     */
    val categories: String?
    /**
     * The event type.
     * This is a single character which changes depending on the type of event being output
     */
    val phase: Char
    val processId: String
    val threadId: String?
    /**
     * The tracing clock timestamp of the event.
     * Somehow `displayTimeUnit` doesn't affect this time unit.
     */
    val timestampMicroseconds: Long
    /**
     * A fixed color name: https://github.com/catapult-project/catapult/blob/master/tracing/tracing/base/color_scheme.html
     */
    val color: String?
    /**
     * Any arguments provided for the event.
     * Some of the event types have required argument fields, otherwise, you can put any information you wish in here.
     * The arguments are displayed in Trace Viewer when you view an event in the analysis section.
     */
    val args: Map<String, Any>?

    companion object {
        const val COLOR_GOOD = "good"
        const val COLOR_BAD = "bad"
        const val COLOR_TERRIBLE = "terrible"

        const val COLOR_BLACK = "black"
        const val COLOR_GREY = "grey"
        const val COLOR_WHITE = "white"
        const val COLOR_YELLOW = "yellow"
    }
}

data class DurationEvent(
    @SerializedName("ph") override val phase: Char,
    @SerializedName("ts") override val timestampMicroseconds: Long,
    @SerializedName("pid") override val processId: String,
    @SerializedName("tid") override val threadId: String? = null,
    @SerializedName("name") override val eventName: String,
    @SerializedName("cat") override val categories: String? = null,
    @SerializedName("cname") override val color: String? = null,
    // If you provide args to both the Begin and End events then the arguments will be merged.
    // If there is a duplicate argument value provided the E event argument will be taken and the B event argument will be discarded.
    @SerializedName("args") override val args: Map<String, Any>? = null
): TraceEvent {
    companion object {
        const val PHASE_BEGIN = 'B'
        const val PHASE_END = 'E'
    }
}

/**
 * Each complete event logically combines a pair of duration events.
 * Unlike duration events, the timestamps of complete events can be in any order.
 */
data class CompleteEvent(
    @SerializedName("ph") override val phase: Char = PHASE,
    /**
     * The time of the start of the complete event
     */
    @SerializedName("ts") override val timestampMicroseconds: Long,
    /**
     * The tracing clock duration of complete events in microseconds
     */
    @SerializedName("dur") val durationMicroseconds: Long,
    @SerializedName("pid") override val processId: String,
    @SerializedName("tid") override val threadId: String? = null,
    @SerializedName("name") override val eventName: String,
    @SerializedName("cat") override val categories: String? = null,
    @SerializedName("cname") override val color: String? = null,
    // If you provide args to both the Begin and End events then the arguments will be merged.
    // If there is a duplicate argument value provided the E event argument will be taken and the B event argument will be discarded.
    @SerializedName("args") override val args: Map<String, Any>? = null
): TraceEvent {
    companion object {
        const val PHASE = 'X'
    }
}

/**
 * Something that happens but has no duration associated with it
 */
data class InstantEvent(
    @SerializedName("ph") override val phase: Char = PHASE,
    /**
     * The time of the start of the complete event
     */
    @SerializedName("ts") override val timestampMicroseconds: Long,
    /**
     * The scope of the event designates how tall to draw the instant event in Trace Viewer.
     * - A thread scoped event will draw the height of a single thread.
     * - A process scoped event will draw through all threads of a given process.
     * - A global scoped event will draw a time from the top to the bottom of the timeline.
     */
    @SerializedName("s") val scope: String,
    @SerializedName("pid") override val processId: String,
    @SerializedName("tid") override val threadId: String? = null,
    @SerializedName("name") override val eventName: String,
    @SerializedName("cat") override val categories: String? = null,
    @SerializedName("cname") override val color: String? = null,
    // If you provide args to both the Begin and End events then the arguments will be merged.
    // If there is a duplicate argument value provided the E event argument will be taken and the B event argument will be discarded.
    @SerializedName("args") override val args: Map<String, Any>? = null
): TraceEvent {
    companion object {
        const val PHASE = 'i'
        const val SCOPE_THREAD = "t"
        const val SCOPE_PROCESS = "p"
        const val SCOPE_GLOBAL = "g"
    }
}