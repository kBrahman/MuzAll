package z.music.module

import android.media.MediaPlayer
import dagger.Module
import dagger.Provides

@Module
class FragmentModule {

    @Provides
    fun provideMediaPlayer() = MediaPlayer()
}