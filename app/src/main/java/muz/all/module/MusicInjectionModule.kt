package muz.all.module

import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerDialogFragment
import muz.all.fragment.PlayerFragment

@Module
interface MusicInjectionModule : AndroidInjector<DaggerDialogFragment> {

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    fun player(): PlayerFragment
}
