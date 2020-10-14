package z.music.module

import android.media.MediaPlayer
import dagger.Module
import dagger.Provides
import z.music.manager.ApiManager
import z.music.manager.MuzApiManager

@Module
class FragmentModule {

    @Provides
    fun provideMediaPlayer() = MediaPlayer()

    @Provides
    fun provideMuzApiManager(): ApiManager = MuzApiManager()

}