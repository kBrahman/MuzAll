package app.manager

import core.domain.MuzResponse

interface ApiManager {
    suspend fun search(q: String, offset: Int): MuzResponse
    suspend fun getPopular(offset: Int): MuzResponse
}