package z.music.module

import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import z.music.activity.MainActivity
import z.music.activity.MusicActivity

@Module
interface AppInjectionModule : AndroidInjector<DaggerAppCompatActivity> {

    @ContributesAndroidInjector(modules = [ActivityModule::class, ActivityInjectionModule::class])
    fun mainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [ActivityModule::class, ActivityInjectionModule::class])
    fun musicActivity(): MusicActivity
}
