package muz.all.data

import muz.all.domain.Track
import javax.inject.Inject

class TrackRepo @Inject constructor(private val ds: TrackDataSource) {
    suspend fun getPopular(offset: Int, clientId: String) = ds.getPopular(offset, clientId)
    suspend fun search(q: String, offset: Int, clientId: String) = ds.search(q, offset, clientId)
    suspend fun myTracks() = ds.myTracks()
    suspend fun delete(t: Track) = ds.delete(t)
}