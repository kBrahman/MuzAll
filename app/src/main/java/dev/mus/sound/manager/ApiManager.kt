package dev.mus.sound.manager

import dev.mus.sound.model.CollectionHolder
import dev.mus.sound.model.Selection
import dev.mus.sound.model.Track
import retrofit2.Callback

interface ApiManager {

    fun search(q: String, offset: Int, callback: Callback<CollectionHolder<Track>>)
    fun getMixedSelections(callback: Callback<CollectionHolder<Selection>>)
    fun tracksBy(
        ids: String?,
        callback: Callback<List<Track>>
    )
}