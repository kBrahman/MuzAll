package zhet.mus.sound.component

import dagger.Module
import dagger.android.ContributesAndroidInjector
import zhet.mus.sound.activity.MainActivity

@Module
interface AppModule {

    @ContributesAndroidInjector
    fun mainActivity(): MainActivity

}