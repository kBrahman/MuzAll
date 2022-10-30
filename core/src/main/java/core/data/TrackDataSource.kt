package core.data

import core.domain.MuzResponse
import core.domain.Track

interface TrackDataSource {
    suspend fun getPopular(offset: Int, clientId: String): MuzResponse
    suspend fun search(q: String, offset: Int, clientId: String): MuzResponse
    suspend fun myTracks(): List<Track>
    suspend fun delete(track: Track)
}