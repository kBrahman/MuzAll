package muz.all.module

import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.DaggerAppCompatActivity
import muz.all.activity.MainActivity
import muz.all.activity.MusicActivity


@Module
interface ActivityInjector : AndroidInjector<DaggerAppCompatActivity> {

    @ContributesAndroidInjector
    fun musicActivity(): MusicActivity

    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    fun mainActivity(): MainActivity
}
