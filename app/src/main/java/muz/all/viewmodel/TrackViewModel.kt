package muz.all.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import muz.all.R
import muz.all.domain.Track
import muz.all.ineractor.Interactor

class TrackViewModel(private val interactor: Interactor, private val idIterator: Iterator<String>) :
    ViewModel() {

    internal var q = ""
    private var clientId = idIterator.next()
    internal val trackObservable = MutableLiveData<List<Track>>()
    internal val toastObservable = MutableLiveData<Int>()
    internal val progressBarObservable = MutableLiveData<Boolean>()
    private val popularTracks = mutableListOf<Track>()
    val searchTracks = mutableListOf<Track>()
    fun getPopular(offset: Int): Job = viewModelScope.launch(Dispatchers.IO) {
        if (offset == 0 && popularTracks.isNotEmpty()) {
            progressBarObservable.postValue(false)
            trackObservable.postValue(popularTracks)
        } else {
            val results = interactor.getPopular(offset, clientId).results
            if (results.isEmpty() && popularTracks.isEmpty() && idIterator.hasNext()) {
                clientId = idIterator.next()
                getPopular(0)
            } else if (results.isEmpty()) toastObservable.postValue(R.string.service_unavailable)
            else {
                popularTracks.addAll(results)
                trackObservable.postValue(results)
            }
        }
    }

    fun search(q: String, offset: Int) = viewModelScope.launch(Dispatchers.IO) {
        if (offset == 0 && searchTracks.isNotEmpty()) {
            progressBarObservable.postValue(false)
            trackObservable.postValue(searchTracks)
        } else {
            val results = interactor.search(q, offset, clientId).results
            searchTracks.addAll(results)
            trackObservable.postValue(results)
        }
    }.also { this.q = q }
}