package music.sound.module

import dagger.Module
import dagger.Provides
import music.sound.manager.ApiManager
import music.sound.manager.MuzApiManager

@Module
class ActivityModule {

    @Provides
    fun provideMuzApiManager(): ApiManager = MuzApiManager()
}