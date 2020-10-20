package zhet.mus.sound.component

import dagger.Component
import zhet.mus.sound.activity.MainActivity
import zhet.mus.sound.module.ActivityModule

@Component(modules = [ActivityModule::class])
interface ActivityComponent {

    fun inject(activity: MainActivity)
}