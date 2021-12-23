package muz.all.data

import muz.all.domain.MuzResponse
import muz.all.domain.Track

interface TrackDataSource {
    suspend fun getPopular(offset: Int): MuzResponse
    suspend fun search(q: String, offset: Int): MuzResponse
    suspend fun myTracks(): List<Track>
    suspend fun delete(track: Track)
}