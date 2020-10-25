package zhet.mus.sound

import android.content.Context
import androidx.multidex.MultiDex
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.HasAndroidInjector

class App : DaggerApplication(), AndroidInjector<App> {


        override fun attachBaseContext(base: Context?) {
            super.attachBaseContext(base)
            MultiDex.install(this)
        }

    override fun applicationInjector(): AndroidInjector<DaggerApplication> {
       return DaggerAppComponent.factory().create(this)
    }

//        override fun applicationInjector() = DaggerAppComponent.factory().create(this)
}