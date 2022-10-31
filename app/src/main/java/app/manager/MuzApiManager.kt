package app.manager

import core.domain.MuzResponse
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject


class MuzApiManager @Inject constructor(
    private val apiService: APIService,
) : ApiManager {

    companion object {
        private val TAG = MuzApiManager::class.java.simpleName
        private const val PATH = "tracks/?limit=25"
    }

    override suspend fun search(q: String, offset: Int) = apiService.search(q, offset, "")

    override suspend fun getPopular(offset: Int) = apiService.getPopular(offset, "clientId")

    interface APIService {

        @GET(PATH)
        suspend fun search(
            @Query("search") q: String,
            @Query("offset") offset: Int,
            @Query("client_id") id: String
        ): MuzResponse

        @GET("$PATH&order=popularity_month")
        suspend fun getPopular(
            @Query("offset") offset: Int,
            @Query("client_id") id: String
        ): MuzResponse
    }
}