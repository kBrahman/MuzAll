package cc.music.manager

import androidx.lifecycle.ViewModel
import cc.music.model.Track
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject


class MuzApiManager @Inject constructor(
    private val apiService: APIService,
) : ApiManager, ViewModel() {

    companion object {
        private val TAG = MuzApiManager::class.java.simpleName
        private const val PATH = "query?f=js&limit=25&search_type=any"
    }

    override fun search(q: String, offset: Int) =
        apiService.search(q, offset)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    override fun getPopular(offset: Int) =
        apiService.getPopular(offset)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    interface APIService {

        @GET(PATH)
        fun search(
            @Query("s") q: String,
            @Query("offset") offset: Int
        ): Single<List<Track>>

        @GET("$PATH&t=links_by")
        fun getPopular(
            @Query("offset") offset: Int
        ): Single<List<Track>>
    }
}