package dev.mus.sound.model

import androidx.annotation.Keep

@Keep
class Selection(val title: String, val items: CollectionHolder<PlayList>)
