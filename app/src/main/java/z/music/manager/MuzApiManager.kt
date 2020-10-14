package z.music.manager

import z.music.model.CollectionHolder
import z.music.model.Selection
import z.music.model.Track
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import z.music.BuildConfig
import java.util.concurrent.TimeUnit


class MuzApiManager : ApiManager {

    companion object {
        private val TAG = MuzApiManager::class.java.simpleName
        private const val SERVER = BuildConfig.SERVER
        private const val SEARCH = "search?limit=25&client_id=${BuildConfig.ClIENT_ID}"
        private const val SELECTIONS = "mixed-selections?client_id=${BuildConfig.ClIENT_ID}"
        private const val TRACKS = "tracks?client_id=${BuildConfig.ClIENT_ID}"
    }

    private var apiService: APIService

    init {
        apiService = Retrofit.Builder()
            .client(OkHttpClient.Builder().readTimeout(14, TimeUnit.SECONDS).build())
            .baseUrl(SERVER)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(APIService::class.java)
    }

    override fun search(q: String, offset: Int, callback: Callback<CollectionHolder<Track>>) =
        apiService.search(q, offset).enqueue(callback)

    override fun getMixedSelections(callback: Callback<CollectionHolder<Selection>>) =
        apiService.getMixedSelections().enqueue(callback)

    override fun tracksBy(ids: String?, callback: Callback<List<Track>>) =
        apiService.tracksBy(ids).enqueue(callback)

    private interface APIService {

        @GET(SEARCH)
        fun search(@Query("q") q: String, @Query("offset") offset: Int): Call<CollectionHolder<Track>>

        @GET(SELECTIONS)
        fun getMixedSelections(): Call<CollectionHolder<Selection>>

        @GET(TRACKS)
        fun tracksBy(@Query("ids") ids: String?): Call<List<Track>>
    }
}