package cc.music.module

import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import cc.music.activity.MainActivity
import cc.music.activity.MusicActivity


@Module
interface ActivityInjector : AndroidInjector<DaggerAppCompatActivity> {

    @ContributesAndroidInjector
    fun musicActivity(): MusicActivity

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    fun mainActivity(): MainActivity
}
