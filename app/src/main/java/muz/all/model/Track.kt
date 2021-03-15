package muz.all.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Track(
        val name: String,
        val duration: String = "",
        val artist_name: String = "",
        val releasedate: String = "",
        val audio: String,
        val image: String = ""
) : Serializable
