package muz.all.model

import android.support.annotation.Keep

@Keep
data class MuzResponse(val results: List<Track>)