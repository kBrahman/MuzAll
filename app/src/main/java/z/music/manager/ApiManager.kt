package z.music.manager

import z.music.model.CollectionHolder
import z.music.model.Selection
import z.music.model.Track
import retrofit2.Callback

interface ApiManager {

    fun search(q: String, offset: Int, callback: Callback<CollectionHolder<Track>>)
    fun getMixedSelections(callback: Callback<CollectionHolder<Selection>>)
    fun tracksBy(
        ids: String?,
        callback: Callback<List<Track>>
    )
}