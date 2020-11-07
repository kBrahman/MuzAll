package cc.music.manager

import io.reactivex.Single
import cc.music.model.Track

interface ApiManager {
    fun search(q: String, offset: Int): Single<List<Track>>
    fun getPopular(offset: Int): Single<List<Track>>
}