package muz.all.manager

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import muz.all.model.MuzResponse
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject


class MuzApiManager @Inject constructor(
    private val apiService: APIService,
    override var clientId: String
) : ApiManager {

    companion object {
        private val TAG = MuzApiManager::class.java.simpleName
        private const val PATH = "tracks/?format=json&limit=25"
    }


    override fun search(q: String, offset: Int) =
        apiService.search(q, offset, clientId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    override fun getPopular(offset: Int) =
        apiService.getPopular(offset, clientId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    interface APIService {

        @GET(PATH)
        fun search(
            @Query("search") q: String,
            @Query("offset") offset: Int,
            @Query("client_id") id: String
        ): Single<MuzResponse>

        @GET("$PATH&order=popularity_month")
        fun getPopular(
            @Query("offset") offset: Int,
            @Query("client_id") id: String
        ): Single<MuzResponse>
    }
}