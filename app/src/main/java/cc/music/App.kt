package cc.music

import android.content.Context
import androidx.multidex.MultiDex
import cc.music.component.DaggerAppComponent
import dagger.android.DaggerApplication
import dagger.android.HasAndroidInjector

class App : DaggerApplication(), HasAndroidInjector {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun applicationInjector() = DaggerAppComponent.factory().create(this)
}