package dev.mus.sound.model

import androidx.annotation.Keep

@Keep
class PlayList(val calculated_artwork_url: String, val short_title: String, val tracks: List<Track>)
