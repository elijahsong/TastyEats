package edu.illinois.cs.cs124.ay2021.mp

import android.os.Looper
import android.view.View
import android.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers
import com.google.common.truth.Truth
import edu.illinois.cs.cs124.ay2021.mp.activities.MainActivity
import edu.illinois.cs.cs124.ay2021.mp.network.isRunning
import org.hamcrest.Matchers
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowLog

/*
 * This file contains helper code used by the test suites.
 * You should not need to modify it.
 */

fun startActivity(): ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java).apply {
    moveToState(Lifecycle.State.CREATED)
    moveToState(Lifecycle.State.RESUMED)
}.also {
    assert(isRunning(true))
}

fun pause(length: Long = 100) {
    Shadows.shadowOf(Looper.getMainLooper()).runToEndOfTasks()
    Thread.sleep(length)
}

fun countRecyclerView(expected: Int) =
    ViewAssertion { v: View, noViewFoundException: NoMatchingViewException? ->
        when {
            noViewFoundException != null -> throw noViewFoundException
            else -> {
                Truth.assertThat((v as RecyclerView).adapter!!.itemCount)
                    .isEqualTo(expected)
            }
        }
    }

fun searchFor(query: String, submit: Boolean = false) = object : ViewAction {
    override fun getConstraints() = Matchers.allOf(ViewMatchers.isDisplayed())
    override fun getDescription() = when (submit) {
        true -> "Set query to $query and submit"
        false -> "Set query to $query but don't submit"
    }

    override fun perform(uiController: UiController, view: View) =
        (view as SearchView).setQuery(query, submit)
}

fun configureLogging() {
    if (System.getenv("OFFICIAL_GRADING") == null) {
        ShadowLog.stream = System.out
    }
}
