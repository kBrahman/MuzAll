package muz.all.mvp.presenter

import muz.all.model.Track
import muz.all.mvp.view.MainView

abstract class MainPresenter : Presenter<MainView>() {
    var results: MutableList<Track>? = null
    abstract fun onQueryTextSubmit(q: String)
    abstract fun onScrolled()
}
