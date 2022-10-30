package muz.all

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import muz.all.action.WaitUntilGoneAction
import app.activity.MainActivity
import muz.all.adapter.TrackAdapter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MuzAllTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun rvItemClickTest() {
        onView(withId(R.id.pb)).check(matches(isDisplayed())).perform(WaitUntilGoneAction(10000))
        onView(withId(R.id.rv)).perform(actionOnItemAtPosition<TrackAdapter.VH>(0, click()))
    }
}