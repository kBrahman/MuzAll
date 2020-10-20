package zhet.mus.sound.manager

import zhet.mus.sound.model.CollectionHolder
import zhet.mus.sound.model.Selection
import zhet.mus.sound.model.Track
import retrofit2.Callback

interface ApiManager {

    fun search(q: String, offset: Int, callback: Callback<CollectionHolder<Track>>)
    fun getMixedSelections(callback: Callback<CollectionHolder<Selection>>)
    fun tracksBy(
        ids: String?,
        callback: Callback<List<Track>>
    )
}