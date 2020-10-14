package z.music

import android.content.Context
import androidx.multidex.MultiDex
import dagger.android.DaggerApplication
import dagger.android.HasAndroidInjector
import z.music.component.DaggerAppComponent

class App : DaggerApplication(), HasAndroidInjector {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun applicationInjector() = DaggerAppComponent.factory().create(this)
}