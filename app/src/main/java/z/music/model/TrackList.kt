package z.music.model

import androidx.annotation.Keep

@Keep
data class TrackList(val tracks: List<Track>, val page: Int)
