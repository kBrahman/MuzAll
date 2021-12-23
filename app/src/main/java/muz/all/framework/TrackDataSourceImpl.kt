package muz.all.framework

import muz.all.data.TrackDataSource
import muz.all.domain.Track
import muz.all.manager.MuzApiManager
import javax.inject.Inject

class TrackDataSourceImpl @Inject constructor(
    private val apiService: MuzApiManager.APIService,
    var clientId: String
) : TrackDataSource {
    private val filteredFiles = mutableListOf<Track>()
    override suspend fun getPopular(offset: Int) = apiService.getPopular(offset, clientId)

    override suspend fun search(q: String, offset: Int) = apiService.search(q, offset, clientId)

    override suspend fun myTracks(): List<Track> {
        TODO("Not yet implemented")
    }

    override suspend fun delete(track: Track) {
        TODO("Not yet implemented")
    }

}