package muz.all.mvp.presenter

import muz.all.mvp.view.MainView

abstract class MainPresenter : Presenter<MainView>() {
    abstract fun onQueryTextSubmit(q: String)
    abstract fun onScrolled()
}
