package muz.all.mvp.presenter

import muz.all.mvp.view.MVPView

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