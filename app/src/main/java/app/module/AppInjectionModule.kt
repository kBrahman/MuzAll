package app.module

import androidx.compose.foundation.ExperimentalFoundationApi
import dagger.Module
import dagger.android.ContributesAndroidInjector
import app.activity.MainActivity

@Module
interface AppInjectionModule {
    @ExperimentalFoundationApi
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    fun mainActivity(): MainActivity
}
