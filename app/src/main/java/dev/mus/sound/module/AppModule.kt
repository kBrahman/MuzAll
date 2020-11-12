package dev.mus.sound.module

import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import dev.mus.sound.activity.MainActivity

@Module
interface AppModule : AndroidInjector<DaggerAppCompatActivity> {

    @ContributesAndroidInjector(modules = [ActivityModule::class])
    fun mainActivity(): MainActivity

}