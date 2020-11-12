package dev.mus.sound.component

import dagger.Component
import dev.mus.sound.fragment.PlayerFragment
import dev.mus.sound.module.FragmentModule

@Component(modules = [FragmentModule::class])
interface FragmentComponent {

    fun inject(playerFragment: PlayerFragment)
}