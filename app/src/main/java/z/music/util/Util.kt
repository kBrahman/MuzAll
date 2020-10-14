package z.music.util

const val TRACK = "track"

fun milliSecondsToTime(ms: Int?): String {
    if (ms == null) return ""
    val secs = ms / 1000
    val h = secs / 3600
    val m = secs % 3600 / 60
    val s = secs % 60
    return if (h > 0) "$h:" else "" + (if (m < 10) "0$m:" else "$m:") + if (s < 10) "0$s" else s
}