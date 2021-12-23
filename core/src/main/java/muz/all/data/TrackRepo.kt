package muz.all.data

import muz.all.domain.Track

class TrackRepo(private val ds: TrackDataSource) {
    suspend fun getPopular(offset: Int) = ds.getPopular(offset)
    suspend fun search(q: String, offset: Int) = ds.search(q, offset)
    suspend fun myTracks() = ds.myTracks()
    suspend fun delete(t: Track) = ds.delete(t)
}