package muz.all.manager

import io.reactivex.Single
import muz.all.model.MuzResponse

interface ApiManager {
    var clientId: String
    fun search(q: String, offset: Int): Single<MuzResponse>
    fun getPopular(offset: Int): Single<MuzResponse>
}