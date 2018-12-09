package music.sound.model

import android.support.annotation.Keep
import java.io.Serializable

@Keep
data class Track(
    val name: String,
    val duration: String,
    val artist_name: String,
    val releasedate: String,
    val audiodownload: String,
    val image: String
) : Serializable