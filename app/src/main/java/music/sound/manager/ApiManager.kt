package music.sound.manager

import music.sound.model.MuzResponse
import retrofit2.Callback

interface ApiManager {

    fun search(q: String, offset: Int, callback: Callback<MuzResponse>)
    fun getPopular(offset: Int, callback: Callback<MuzResponse>)
}