package z.music.module

import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import z.music.activity.MainActivity

@Module
interface AppModule:AndroidInjector<DaggerAppCompatActivity> {

    fun mainActivity(): MainActivity
}
