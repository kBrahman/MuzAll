package core.actor

import core.data.TrackRepo
import javax.inject.Inject

class GetPopular @Inject constructor(private val trackRepo: TrackRepo) {
    suspend operator fun invoke(offset: Int, clientId: String) = trackRepo.getPopular(offset, clientId)
}

class Search @Inject constructor(private val trackRepo: TrackRepo) {
    suspend operator fun invoke(q: String, offset: Int, clientId: String) = trackRepo.search(q, offset, clientId)
}

class MyTracks @Inject constructor(private val trackRepo: TrackRepo) {
    suspend operator fun invoke() = trackRepo.myTracks()
}

data class Actor @Inject constructor(
    val getPopular: GetPopular,
    val search: Search,
    val myTracks: MyTracks
)
