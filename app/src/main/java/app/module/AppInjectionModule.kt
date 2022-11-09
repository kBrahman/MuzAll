package app.module

import androidx.compose.foundation.ExperimentalFoundationApi
import app.activity.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
interface AppInjectionModule {
    @ExperimentalFoundationApi
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    fun mainActivity(): MainActivity
}
