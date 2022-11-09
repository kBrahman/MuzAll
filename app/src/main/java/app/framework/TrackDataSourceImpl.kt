package app.framework

import app.manager.MuzApiManager
import core.data.TrackDataSource
import core.domain.Track
import java.io.File
import javax.inject.Inject

class TrackDataSourceImpl @Inject constructor(
    private val apiService: MuzApiManager.APIService,
    private val dir: File?
) : TrackDataSource {

    override suspend fun getPopular(offset: Int, clientId: String) =
        apiService.getPopular(offset, clientId)

    override suspend fun search(q: String, offset: Int, clientId: String) =
        apiService.search(q, offset, clientId)

    override suspend fun myTracks() =
        dir?.listFiles()?.filter { it.extension == "mp3" || it.extension == "flac" }?.toMutableList() ?: mutableListOf()
}