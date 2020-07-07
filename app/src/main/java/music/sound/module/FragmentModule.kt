package music.sound.module

import android.media.MediaPlayer
import dagger.Module
import dagger.Provides
import music.sound.manager.ApiManager
import music.sound.manager.MuzApiManager

@Module
class FragmentModule {

    @Provides
    fun provideMediaPlayer() = MediaPlayer()

    @Provides
    fun provideMuzApiManager(): ApiManager = MuzApiManager()

}