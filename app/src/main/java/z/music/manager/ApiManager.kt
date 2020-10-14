package z.music.manager

import io.reactivex.Single
import z.music.model.CollectionHolder
import z.music.model.Selection
import z.music.model.Track
import retrofit2.Callback
import z.music.model.Token

interface ApiManager {

    fun search(q: String, offset: Int, callback: Callback<CollectionHolder<Track>>)
    fun getMixedSelections(callback: Callback<CollectionHolder<Selection>>)
    fun tracksBy(
        ids: String?,
        callback: Callback<List<Track>>
    )

    fun getToken(): Single<Token>
}