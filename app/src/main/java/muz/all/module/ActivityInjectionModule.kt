package muz.all.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import muz.all.fragment.PlayerFragment

@Module
interface ActivityInjectionModule {

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    fun player(): PlayerFragment
}
