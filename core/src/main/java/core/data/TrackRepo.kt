package core.data

import core.domain.Track
import javax.inject.Inject

class TrackRepo @Inject constructor(private val ds: TrackDataSource) {
    suspend fun getPopular(offset: Int, clientId: String) = ds.getPopular(offset, clientId)
    suspend fun search(q: String, offset: Int, clientId: String) = ds.search(q, offset, clientId)
    suspend fun myTracks() = ds.myTracks()
}