package cc.music.mvp.view

import cc.music.adapter.TrackAdapter
import cc.music.model.Track

interface MainView : MVPView {
    var trackAdapter: TrackAdapter?
    fun showServiceUnavailable()
    fun show(tracks: MutableList<Track>?)
    fun addAndShow(tracks: List<Track>?)
}
