package music.sound.component

import dagger.Component
import music.sound.activity.MainActivity
import music.sound.module.ActivityModule

@Component(modules = [ActivityModule::class])
interface ActivityComponent {

    fun inject(activity: MainActivity)
}