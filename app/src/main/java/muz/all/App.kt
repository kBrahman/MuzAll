package muz.all

import dagger.android.DaggerApplication
import dagger.android.HasAndroidInjector
import muz.all.component.DaggerAppComponent

class App : DaggerApplication(), HasAndroidInjector {

    override fun applicationInjector() = DaggerAppComponent.factory().create(this)
}