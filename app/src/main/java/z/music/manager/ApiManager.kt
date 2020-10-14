package z.music.manager

import io.reactivex.Single
import retrofit2.Callback
import z.music.model.CollectionHolder
import z.music.model.Token
import z.music.model.Track
import z.music.model.TrackList

interface ApiManager {

    fun search(q: String, offset: Int, callback: Callback<CollectionHolder<Track>>)
    fun getTop(token: String, page: Int): Single<TrackList>
    fun tracksBy(
        ids: String?,
        callback: Callback<List<Track>>
    )

    fun getToken(): Single<Token>
    fun getAccessToken(t: String, hash: String): Single<Token>
}