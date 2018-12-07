package muz.all.module

import dagger.Module
import dagger.Provides
import muz.all.manager.ApiManager
import muz.all.manager.MuzApiManager

@Module
class ActivityModule {

    @Provides
    fun provideMuzApiManager(): ApiManager = MuzApiManager()
}