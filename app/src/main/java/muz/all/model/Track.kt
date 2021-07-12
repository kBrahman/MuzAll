package muz.all.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
open class Track(
    val name: String = "",
    val duration: String = "",
    val artist_name: String = "",
    val releasedate: String = "",
    val audio: String = "",
    val image: String = ""
) : Serializable{
    override fun toString(): String {
        return "Track(name='$name', duration='$duration', artist_name='$artist_name', releasedate='$releasedate', audio='$audio', image='$image')"
    }
}
