package music.sound.manager

import music.sound.model.CollectionHolder
import music.sound.model.Selection
import music.sound.model.Track
import retrofit2.Callback

interface ApiManager {

    fun search(q: String, offset: Int, callback: Callback<CollectionHolder<Track>>)
    fun getMixedSelections(callback: Callback<CollectionHolder<Selection>>)
    fun tracksBy(
        ids: String?,
        callback: Callback<List<Track>>
    )
}