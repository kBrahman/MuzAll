package z.music.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Keep
@Entity
data class Track(
    val track: String,
    val duration: String,
    val bitrate: String,
    val artistImageUrlSquare100: String?,
    @PrimaryKey
    val id: Int,
    @Ignore
    val playbackEnabled: Boolean
) : Serializable {

    constructor(
        track: String,
        duration: String,
        bitrate: String,
        artistImageUrlSquare100: String?,
        id: Int
    ) : this(track, duration, bitrate, artistImageUrlSquare100, id, true)

    var isAdded = false
}
