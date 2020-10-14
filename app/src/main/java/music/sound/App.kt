package music.sound

import android.content.Context
import androidx.multidex.MultiDex
import dagger.android.DaggerApplication

class App : DaggerApplication() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun applicationInjector() = DaggerAppComponent.factory().create(this)
}