package muz.all.module

import android.media.AudioAttributes
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioAttributes.USAGE_MEDIA
import android.media.AudioManager.STREAM_MUSIC
import android.media.MediaPlayer
import android.os.Build
import dagger.Module
import dagger.Provides

@Module
class FragmentModule {
    @Provides
    fun provideMediaPlayer(): MediaPlayer {
        val mp = MediaPlayer()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mp.setAudioAttributes(
                AudioAttributes.Builder().setContentType(CONTENT_TYPE_MUSIC).setUsage(USAGE_MEDIA)
                    .build()
            )
        } else {
            mp.setAudioStreamType(STREAM_MUSIC)
        }
        return mp
    }
}