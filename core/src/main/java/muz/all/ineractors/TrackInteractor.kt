package muz.all.ineractors

import muz.all.data.TrackRepo
import muz.all.domain.Track

class GetPopular(private val trackRepo: TrackRepo) {
    suspend operator fun invoke(offset: Int) = trackRepo.getPopular(offset)
}

class Search(private val trackRepo: TrackRepo) {
    suspend operator fun invoke(q: String, offset: Int) = trackRepo.search(q, offset)
}

class MyTracks(private val trackRepo: TrackRepo) {
    suspend operator fun invoke() = trackRepo.myTracks()
}

class DeleteMyTrack(private val trackRepo: TrackRepo) {
    suspend operator fun invoke(track: Track) = trackRepo.delete(track)
}