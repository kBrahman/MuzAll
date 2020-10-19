package z.music.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import z.music.fragment.PlayerFragment

@Module
interface ActivityModuleContrib {

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    fun playerFrag(): PlayerFragment
}