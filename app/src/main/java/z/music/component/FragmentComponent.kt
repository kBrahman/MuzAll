package z.music.component

import dagger.Component
import z.music.fragment.PlayerFragment
import z.music.module.FragmentModule

@Component(modules = [FragmentModule::class])
interface FragmentComponent {

    fun inject(playerFragment: PlayerFragment)
}