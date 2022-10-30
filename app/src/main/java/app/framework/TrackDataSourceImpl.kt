package app.framework

import core.data.TrackDataSource
import core.domain.Track
import app.manager.MuzApiManager
import javax.inject.Inject

class TrackDataSourceImpl @Inject constructor(
    private val apiService: MuzApiManager.APIService,
) : TrackDataSource {

    private val filteredFiles = mutableListOf<Track>()
    override suspend fun getPopular(offset: Int, clientId: String) =
        apiService.getPopular(offset, clientId)

    override suspend fun search(q: String, offset: Int, clientId: String) =
        apiService.search(q, offset, clientId)

    override suspend fun myTracks(): List<Track> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(track: Track) {
        TODO("Not yet implemented")
    }
}