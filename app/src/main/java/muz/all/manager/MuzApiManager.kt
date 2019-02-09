package muz.all.manager

import muz.all.BuildConfig
import muz.all.model.MuzResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MuzApiManager @Inject constructor() : ApiManager {

    companion object {
        private val TAG = MuzApiManager::class.java.simpleName
        private const val SERVER = BuildConfig.SERVER
        private const val PATH = "tracks/?format=json&limit=25"
    }

    private var apiService: APIService
    override var clientId = BuildConfig.CLIENT_ID_1

    init {
        apiService = Retrofit.Builder()
            .client(OkHttpClient.Builder().readTimeout(14, TimeUnit.SECONDS).build())
            .baseUrl(SERVER)
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(APIService::class.java)
    }

    override fun search(q: String, offset: Int, callback: Callback<MuzResponse>) =
        apiService.search(q, offset, clientId).enqueue(callback)

    override fun getPopular(offset: Int, callback: Callback<MuzResponse>) =
        apiService.getPopular(offset, clientId).enqueue(callback)

    private interface APIService {

        @GET(PATH)
        fun search(
            @Query("search") q: String,
            @Query("offset") offset: Int,
            @Query("client_id") id: String
        ): Call<MuzResponse>

        @GET("$PATH&order=popularity_month")
        fun getPopular(
            @Query("offset") offset: Int,
            @Query("client_id") id: String
        ): Call<MuzResponse>
    }
}