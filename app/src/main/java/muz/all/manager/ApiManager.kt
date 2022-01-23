package muz.all.manager

import muz.all.domain.MuzResponse

interface ApiManager {
    suspend fun search(q: String, offset: Int): MuzResponse
    suspend fun getPopular(offset: Int): MuzResponse
}