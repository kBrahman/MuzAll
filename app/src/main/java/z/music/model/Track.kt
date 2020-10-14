package z.music.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Track(
    val track: String,
    val duration: String,
    val bitrate: String,
    val artistImageUrlSquare100: String,
    val stream_url: String,
    val user: User,
    val id: Int,
    val media: Media
) : Serializable
