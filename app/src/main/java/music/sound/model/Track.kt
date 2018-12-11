package music.sound.model

import android.support.annotation.Keep
import java.io.Serializable

@Keep
data class Track(
    val title: String,
    val duration: Int,
    val created_at: String,
    val artwork_url: String,
    val stream_url: String,
    val user: User
) : Serializable
