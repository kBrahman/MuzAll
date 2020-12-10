package muz.all.module

import dagger.Module
import dagger.Provides
import muz.all.fragment.PlayerFragment

@Module
class ActivityModule {

    @Provides
    fun player(): PlayerFragment = PlayerFragment();
}
