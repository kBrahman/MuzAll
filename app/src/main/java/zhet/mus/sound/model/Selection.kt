package zhet.mus.sound.model

import androidx.annotation.Keep

@Keep
class Selection(val urn: String, val items: CollectionHolder<TrackList>)
