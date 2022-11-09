package core.data

import core.domain.MuzResponse
import core.domain.Track
import java.io.File

interface TrackDataSource {
    suspend fun getPopular(offset: Int, clientId: String): MuzResponse
    suspend fun search(q: String, offset: Int, clientId: String): MuzResponse
    suspend fun myTracks(): MutableList<File>
}