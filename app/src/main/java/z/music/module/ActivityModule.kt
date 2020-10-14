package z.music.module

import android.content.Context.MODE_PRIVATE
import dagger.Module
import dagger.Provides
import dagger.android.DaggerActivity
import dagger.android.DaggerApplication
import z.music.manager.ApiManager
import z.music.manager.MuzApiManager
import z.music.util.Z_MUSIC_PREFS

@Module
class ActivityModule {

    @Provides
    fun provideMuzApiManager(): ApiManager = MuzApiManager()

    @Provides
    fun sharedPrefs(app: DaggerApplication) = app.getSharedPreferences(Z_MUSIC_PREFS, MODE_PRIVATE)
}