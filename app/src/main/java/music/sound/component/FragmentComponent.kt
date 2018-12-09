package music.sound.component

import dagger.Component
import music.sound.fragment.PlayerFragment
import music.sound.module.FragmentModule

@Component(modules = [FragmentModule::class])
interface FragmentComponent {

    fun inject(playerFragment: PlayerFragment)
}