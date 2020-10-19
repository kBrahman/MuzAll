package z.music.manager

import io.reactivex.Single
import retrofit2.Callback
import z.music.model.Token
import z.music.model.Track
import z.music.model.TrackList

interface ApiManager {

    fun getTop(token: String, page: Int): Single<TrackList>
    fun tracksBy(
        ids: String?,
        callback: Callback<List<Track>>
    )

    fun getToken(): Single<Token>
    fun getAccessToken(t: String, hash: String): Single<Token>
    fun search(q: String, token: String, page: Int): Single<TrackList>?
}