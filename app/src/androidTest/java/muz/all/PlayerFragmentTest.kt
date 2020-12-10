package muz.all

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import muz.all.activity.MusicActivity
import muz.all.adapter.MusicAdapter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class PlayerFragmentTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MusicActivity> =
        ActivityScenarioRule(MusicActivity::class.java)

    @Test
    fun floatingButtonTest() {
        onView(withId(R.id.rvMusic)).perform(actionOnItemAtPosition<MusicAdapter.MusicVH>(0, click()))
//        onView(withId(R.id.fab)).perform(click())
//        onView(withId(R.id.tvTaskName)).check(matches(isDisplayed()))
    }
}