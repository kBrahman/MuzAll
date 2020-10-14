package z.music.module

import android.content.Context.MODE_PRIVATE
import dagger.Module
import dagger.Provides
import dagger.android.DaggerApplication
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import z.music.BuildConfig
import z.music.manager.ApiManager
import z.music.manager.MuzApiManager
import z.music.util.Z_MUSIC_PREFS
import java.util.concurrent.TimeUnit

@Module
class ActivityModule {

    @Provides
    fun apiService() = Retrofit.Builder()
        .client(OkHttpClient.Builder().readTimeout(14, TimeUnit.SECONDS).build())
        .baseUrl(BuildConfig.SERVER)
        .addConverterFactory(GsonConverterFactory.create())
        .build().create(MuzApiManager.APIService::class.java)

    @Provides
    fun provideMuzApiManager(apiService: MuzApiManager.APIService): ApiManager =
        MuzApiManager(apiService)

    @Provides
    fun sharedPrefs(app: DaggerApplication) = app.getSharedPreferences(Z_MUSIC_PREFS, MODE_PRIVATE)

}