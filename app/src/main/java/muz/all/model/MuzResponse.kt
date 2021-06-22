package muz.all.model

import androidx.annotation.Keep

@Keep
data class MuzResponse(val results: MutableList<Track>) {
//    constructor() : this(emptyList())
}