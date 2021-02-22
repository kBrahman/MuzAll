package dev.mus.sound.module

import android.media.MediaPlayer
import dagger.Module
import dagger.Provides
import dev.mus.sound.manager.ApiManager
import dev.mus.sound.manager.MuzApiManager

@Module
class ActivityModule {

    @Provides
    fun provideMuzApiManager(): ApiManager = MuzApiManager()

    @Provides
    fun provideMediaPlayer() = MediaPlayer()
}