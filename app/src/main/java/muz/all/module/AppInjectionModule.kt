package muz.all.module

import androidx.compose.foundation.ExperimentalFoundationApi
import dagger.Module
import dagger.android.ContributesAndroidInjector
import muz.all.activity.MainActivity


@Module
interface AppInjectionModule {

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    fun mainActivity(): MainActivity

}
