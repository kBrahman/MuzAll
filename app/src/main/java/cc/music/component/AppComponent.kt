package cc.music.component

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import cc.music.module.ActivityInjector

@Component(modules = [AndroidInjectionModule::class, ActivityInjector::class])
interface AppComponent : AndroidInjector<DaggerApplication> {

    @Component.Factory
    abstract class Factory : AndroidInjector.Factory<DaggerApplication>
}