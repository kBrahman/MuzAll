package dev.mus.sound.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Track(
    val title: String,
    val duration: Int,
    val created_at: String,
    val artwork_url: String?,
    val stream_url: String,
    val user: User,
    val id: Int,
    val media: Media
) : Serializable
