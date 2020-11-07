package cc.music.mvp.presenter

import cc.music.mvp.view.MVPView

abstract class Presenter<V : MVPView> {

    var view: V? = null
        set(value) {
            field = value
            onViewAttached()
        }

    abstract fun onViewAttached()
    abstract fun showLoading()
    abstract fun hideLoading()
}