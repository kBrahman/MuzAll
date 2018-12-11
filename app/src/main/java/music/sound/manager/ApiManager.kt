package music.sound.manager

import music.sound.model.Track
import retrofit2.Callback

interface ApiManager {

    fun search(q: String, offset: Int, callback: Callback<List<Track>>)
    fun getPopular(offset: Int, callback: Callback<List<Track>>)
}