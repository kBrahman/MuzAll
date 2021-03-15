package muz.all.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import muz.all.activity.MainActivity
import muz.all.activity.MusicActivity


@Module
interface AppInjectionModule {

    @ContributesAndroidInjector
    fun musicActivity(): MusicActivity

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    fun mainActivity(): MainActivity

}
