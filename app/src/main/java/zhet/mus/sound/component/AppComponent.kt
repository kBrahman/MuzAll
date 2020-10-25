package zhet.mus.sound.component

import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

@Component(modules = [AndroidInjectionModule::class, AppModule::class])
interface AppComponent {

    @Component.Factory
    abstract class Factory : AndroidInjector.Factory<DaggerApplication>
}