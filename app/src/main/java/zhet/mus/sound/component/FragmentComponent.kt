package zhet.mus.sound.component

import dagger.Component
import zhet.mus.sound.fragment.PlayerFragment
import zhet.mus.sound.module.FragmentModule

@Component(modules = [FragmentModule::class])
interface FragmentComponent {

    fun inject(playerFragment: PlayerFragment)
}