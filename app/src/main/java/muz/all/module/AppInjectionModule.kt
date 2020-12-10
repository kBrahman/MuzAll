package muz.all.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import muz.all.activity.MainActivity
import muz.all.activity.MusicActivity


@Module
interface AppInjectionModule {

    @ContributesAndroidInjector(modules = [ActivityModule::class, ActivityInjectionModule::class])
    fun musicActivity(): MusicActivity

    @ContributesAndroidInjector(modules = [MainActivityModule::class, ActivityModule::class, ActivityInjectionModule::class])
    fun mainActivity(): MainActivity

}
