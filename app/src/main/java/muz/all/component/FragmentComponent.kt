package muz.all.component

import dagger.Component
import muz.all.fragment.PlayerFragment
import muz.all.module.FragmentModule

@Component(modules = [FragmentModule::class])
interface FragmentComponent {

    fun inject(playerFragment: PlayerFragment)
}