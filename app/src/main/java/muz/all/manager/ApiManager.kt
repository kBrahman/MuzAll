package muz.all.manager

import muz.all.domain.MuzResponse

interface ApiManager {
    var clientId: String
    suspend fun search(q: String, offset: Int): MuzResponse
    suspend fun getPopular(offset: Int): MuzResponse
}