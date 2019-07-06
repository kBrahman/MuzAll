package music.sound.model

import androidx.annotation.Keep

@Keep
data class MuzResponse(val results: List<Track>)