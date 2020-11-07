package cc.music.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AppViewModel : ViewModel() {
    var tracks: MutableLiveData<List<Track>>? = null
}