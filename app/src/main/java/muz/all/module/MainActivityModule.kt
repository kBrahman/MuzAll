package muz.all.module

import android.app.Activity
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Environment
import dagger.Module
import dagger.Provides
import io.reactivex.disposables.CompositeDisposable
import muz.all.BuildConfig
import muz.all.manager.ApiManager
import muz.all.manager.MuzApiManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit


@Module
class MainActivityModule {
    companion object {
        private const val SERVER = BuildConfig.SERVER
        private val TAG = MainActivityModule::class.java.simpleName
    }

    @Provides
    fun provideMuzApiManager(manager: MuzApiManager): ApiManager = manager

    @Provides
    fun provideApiService(): MuzApiManager.APIService = Retrofit.Builder()
        .client(OkHttpClient.Builder().readTimeout(14, TimeUnit.SECONDS).build())
        .baseUrl(SERVER)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build().create(MuzApiManager.APIService::class.java)

    @Provides
    fun provideDisposable() = CompositeDisposable()

    @Provides
    fun provideIdIterator() = BuildConfig.IDS.iterator()

    @Provides
    fun provideClientId(idIterator: Iterator<String>) = idIterator.next()

    @Provides
    fun provideMediaPlayer(): MediaPlayer {
        val mp = MediaPlayer()
        mp.setAudioAttributes(
            AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(
                AudioAttributes.USAGE_MEDIA
            )
                .build()
        )
        return mp
    }

    @Provides
    fun provideMyMusicDir(activity: Activity): File {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        return directory
    }
}