package cc.music.component

import dagger.Component
import cc.music.fragment.PlayerFragment
import cc.music.module.FragmentModule

@Component(modules = [FragmentModule::class])
interface FragmentComponent {

    fun inject(playerFragment: PlayerFragment)
}