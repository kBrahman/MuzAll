package muz.all.model

import androidx.annotation.Keep

@Keep
data class MuzResponse(val results: List<Track>) {
    constructor() : this(emptyList())
}