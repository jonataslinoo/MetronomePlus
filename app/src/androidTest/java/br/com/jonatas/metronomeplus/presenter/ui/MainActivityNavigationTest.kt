package br.com.jonatas.metronomeplus.presenter.ui

import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.jonatas.metronomeplus.R
import junit.framework.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun shouldShowsMetronomeFragmentInitially() {
        onView(withId(R.id.bottomNavigationView)).check(matches(isDisplayed()))

        onView(withId(R.id.metronomeFragmentContent)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldNavigatesToLibraryFragmentWhenClickingOnTheLibraryMenu() {
        onView(withId(R.id.libraryFragment)).perform(click())

        onView(withId(R.id.libraryFragmentContent)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldNavigatesToMetronomeFragmentWhenClickingOnTheMetronomeMenu() {
        onView(withId(R.id.libraryFragment)).perform(click())
        onView(withId(R.id.libraryFragmentContent)).check(matches(isDisplayed()))

        onView(withId(R.id.metronomeFragment)).perform(click())
        onView(withId(R.id.metronomeFragmentContent)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldBackToMetronomeFragmentWhenClickingOnBackPress() {
        onView(withId(R.id.libraryFragment)).perform(click())
        onView(withId(R.id.libraryFragmentContent)).check(matches(isDisplayed()))

        onView(withId(R.id.libraryFragmentContent)).perform(pressBack())

        onView(withId(R.id.libraryFragmentContent)).check(doesNotExist())
        onView(withId(R.id.metronomeFragmentContent)).check(matches(isDisplayed()))
    }

    @Test
    fun shouldExitTheAppWhenClickingOnBackPressWhileInTheMetronomeFragment() {
        onView(withId(R.id.metronomeFragmentContent)).check(matches(isDisplayed()))

        Espresso.pressBackUnconditionally()

        assertEquals(Lifecycle.State.DESTROYED, activityRule.scenario.state)
    }
}