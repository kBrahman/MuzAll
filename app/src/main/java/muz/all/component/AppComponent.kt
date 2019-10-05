package muz.all.component

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import muz.all.module.AppModule

@Component(modules = [AndroidInjectionModule::class, AppModule::class])
interface AppComponent : AndroidInjector<DaggerApplication> {

    @Component.Factory
    abstract class Factory : AndroidInjector.Factory<DaggerApplication>
}