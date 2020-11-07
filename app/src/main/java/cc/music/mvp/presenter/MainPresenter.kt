package cc.music.mvp.presenter

import cc.music.model.Track
import cc.music.mvp.view.MainView

abstract class MainPresenter : Presenter<MainView>() {
    var results: MutableList<Track>? = null
    abstract fun onQueryTextSubmit(q: String)
    abstract fun onScrolled()
}
