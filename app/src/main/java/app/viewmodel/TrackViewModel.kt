package app.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.Track
import core.actor.Actor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import muz.all.R
import java.io.File

class TrackViewModel(
    private val actor: Actor,
    private val idIterator: Iterator<String>,
) : ViewModel() {
    internal var myTracks: MutableList<File>? = null

    @Suppress("PrivatePropertyName")
    private val TAG = "ViewModel"
    internal var q = ""
    private var clientId = idIterator.next()
    internal val tracksObservable = MutableLiveData<List<Track>>()
    internal val myTracksObservable = MutableLiveData<List<File>>()
    internal val toastObservable = MutableLiveData<Int>()
    internal val progressBarObservable = MutableLiveData<Boolean>()
    private val popularTracks = mutableListOf<Track>()
    val searchTracks = mutableListOf<Track>()
    internal val myMusicObservable = MutableLiveData<MutableList<File>>()

    fun getPopular(offset: Int): Job = viewModelScope.launch(Dispatchers.IO) {
        if (offset == 0 && popularTracks.isNotEmpty()) {
            progressBarObservable.postValue(false)
            tracksObservable.postValue(popularTracks)
        } else {
            val results = actor.getPopular(offset, clientId).results
            if (results.isEmpty() && popularTracks.isEmpty() && idIterator.hasNext()) {
                clientId = idIterator.next()
                getPopular(0)
            } else if (results.isEmpty()) toastObservable.postValue(R.string.service_unavailable)
            else {
                popularTracks.addAll(results)
                tracksObservable.postValue(results)
            }
        }
    }

    fun search(q: String, offset: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (offset == 0 && searchTracks.isNotEmpty()) {
            progressBarObservable.postValue(false)
            tracksObservable.postValue(searchTracks)
        } else {
            val results = actor.search(q, offset, clientId).results
            searchTracks.addAll(results)
            tracksObservable.postValue(results)
        }
    }.also { this.q = q }

    fun myMusic() = viewModelScope.launch(Dispatchers.IO) {
        if (myTracks == null) myTracks = actor.myTracks()
        myTracksObservable.postValue(myTracks)
    }

    fun del(file: File?) {
        val deleted = file?.delete()
        Log.i(TAG, "file to del=>${file?.name},  deleted=>$deleted")
        if (deleted == true) myTracks?.remove(file)
        myMusic()
    }
}