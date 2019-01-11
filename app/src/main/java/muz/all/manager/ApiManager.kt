package muz.all.manager

import muz.all.model.MuzResponse
import retrofit2.Callback

interface ApiManager {
    var clientId: String
    fun search(q: String, offset: Int, callback: Callback<MuzResponse>)
    fun getPopular(offset: Int, callback: Callback<MuzResponse>)
}