package z.music.module

import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import z.music.activity.MainActivity

@Module
interface AppModule : AndroidInjector<DaggerAppCompatActivity> {

    @ContributesAndroidInjector(modules = [ActivityModule::class, ActivityModuleContrib::class])
    fun mainActivity(): MainActivity
}
