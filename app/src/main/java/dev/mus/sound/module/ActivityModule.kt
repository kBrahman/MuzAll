package dev.mus.sound.module

import dagger.Module
import dagger.Provides
import dev.mus.sound.manager.ApiManager
import dev.mus.sound.manager.MuzApiManager

@Module
class ActivityModule {

    @Provides
    fun provideMuzApiManager(): ApiManager = MuzApiManager()
}