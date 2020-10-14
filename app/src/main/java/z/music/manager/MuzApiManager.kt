package z.music.manager

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.GET
import retrofit2.http.Query
import z.music.model.CollectionHolder
import z.music.model.Selection
import z.music.model.Token
import z.music.model.Track
import javax.inject.Inject


class MuzApiManager @Inject constructor(private val apiService: APIService) : ApiManager {

    companion object {
        private val TAG = MuzApiManager::class.java.simpleName
        private const val SEARCH = "search?limit=25&client_id="
        private const val SELECTIONS = "mixed-selections?client_id="
        private const val TRACKS = "tracks?client_id="
        private const val HELLO = "hello"
    }

    override fun search(q: String, offset: Int, callback: Callback<CollectionHolder<Track>>) =
        apiService.search(q, offset).enqueue(callback)

    override fun getMixedSelections(callback: Callback<CollectionHolder<Selection>>) =
        apiService.getMixedSelections().enqueue(callback)

    override fun tracksBy(ids: String?, callback: Callback<List<Track>>) =
        apiService.tracksBy(ids).enqueue(callback)

    override fun getToken() =
        apiService.getToken().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())


    interface APIService {

        @GET(SEARCH)
        fun search(
            @Query("q") q: String,
            @Query("offset") offset: Int
        ): Call<CollectionHolder<Track>>

        @GET(SELECTIONS)
        fun getMixedSelections(): Call<CollectionHolder<Selection>>

        @GET(TRACKS)
        fun tracksBy(@Query("ids") ids: String?): Call<List<Track>>

        @GET(HELLO)
        fun getToken(): Single<Token>
    }
}