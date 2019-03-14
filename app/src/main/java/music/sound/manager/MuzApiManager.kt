package music.sound.manager

import music.sound.BuildConfig
import music.sound.model.Track
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


class MuzApiManager : ApiManager {

    companion object {
        private val TAG = MuzApiManager::class.java.simpleName
        private const val SERVER = BuildConfig.SERVER

        private const val PATH = "tracks/?limit=25&client_id=${BuildConfig.ClIENT_ID}"
    }

    private var apiService: APIService

    init {
        apiService = Retrofit.Builder()
            .client(OkHttpClient.Builder().readTimeout(14, TimeUnit.SECONDS).build())
            .baseUrl(SERVER)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(APIService::class.java)
    }

    override fun search(q: String, offset: Int, callback: Callback<List<Track>>) =
        apiService.search(q, offset).enqueue(callback)

    override fun getPopular(offset: Int, callback: Callback<List<Track>>) =
        apiService.getPopular(offset).enqueue(callback)

    private interface APIService {

        @GET(PATH)
        fun search(@Query("q") q: String, @Query("offset") offset: Int): Call<List<Track>>

        @GET(PATH)
        fun getPopular(@Query("offset") offset: Int): Call<List<Track>>
    }
}