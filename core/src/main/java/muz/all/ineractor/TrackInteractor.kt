package muz.all.ineractor

import muz.all.data.TrackRepo
import muz.all.domain.Track
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

class DeleteMyTrack @Inject constructor(private val trackRepo: TrackRepo) {
    suspend operator fun invoke(track: Track) = trackRepo.delete(track)
}

data class Interactor @Inject constructor(
    val getPopular: GetPopular,
    val search: Search,
    val myTracks: MyTracks,
    val deleteMyTrack: DeleteMyTrack
)
