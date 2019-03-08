package muz.all.component

import dagger.Component
import muz.all.activity.MainActivity
import muz.all.module.ActivityModule

@Component(modules = [ActivityModule::class])
interface ActivityComponent {
    fun inject(activity: MainActivity)
}