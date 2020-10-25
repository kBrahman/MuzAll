package zhet.mus.sound.component

import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.DaggerActivity
import dagger.android.support.DaggerAppCompatActivity
import zhet.mus.sound.activity.MainActivity

@Module
interface AppModule:AndroidInjector<DaggerAppCompatActivity> {

    @ContributesAndroidInjector
    fun mainActivity(): MainActivity

}