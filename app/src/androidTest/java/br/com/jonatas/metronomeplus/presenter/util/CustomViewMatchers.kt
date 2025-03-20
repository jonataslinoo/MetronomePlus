package br.com.jonatas.metronomeplus.presenter.util

import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

object CustomViewMatchers {

    fun childOfParentAtIndex(parentMatcher: Matcher<View>, childIndex: Int): Matcher<View> {
        return object : BoundedMatcher<View, View>(View::class.java) {

            override fun describeTo(description: Description?) {
                description?.appendText("With child at index $childIndex of parent")
                parentMatcher.describeTo(description)
            }

            override fun matchesSafely(view: View?): Boolean {
                val parent = view?.parent ?: return false

                if (parent !is ViewGroup) return false

                if (!parentMatcher.matches(parent)) return false

                return parent.getChildAt(childIndex) == view
            }
        }
    }
}