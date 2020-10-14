package z.music.module

import dagger.Module
import dagger.Provides
import z.music.manager.ApiManager
import z.music.manager.MuzApiManager

@Module
class ActivityModule {

    @Provides
    fun provideMuzApiManager(): ApiManager = MuzApiManager()
}