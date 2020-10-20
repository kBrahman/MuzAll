package zhet.mus.sound.module

import dagger.Module
import dagger.Provides
import zhet.mus.sound.manager.ApiManager
import zhet.mus.sound.manager.MuzApiManager

@Module
class ActivityModule {

    @Provides
    fun provideMuzApiManager(): ApiManager = MuzApiManager()
}