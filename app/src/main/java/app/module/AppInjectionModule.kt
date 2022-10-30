package app.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import app.activity.MainActivity


@Module
interface AppInjectionModule {

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    fun mainActivity(): MainActivity

}
