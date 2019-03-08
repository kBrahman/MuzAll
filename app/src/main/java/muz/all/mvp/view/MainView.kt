package muz.all.mvp.view

import muz.all.adapter.TrackAdapter
import muz.all.model.Track

interface MainView : MVPView {
    var trackAdapter: TrackAdapter?
    fun showServiceUnavailable()
    fun show(tracks: MutableList<Track>?)
    fun addAndShow(tracks: List<Track>?)
}
