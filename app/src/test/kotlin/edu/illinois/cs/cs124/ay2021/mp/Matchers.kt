@file:Suppress("unused")

package edu.illinois.cs.cs124.ay2021.mp

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

/*
 * This file contains helper code used by the test suites.
 * You should not need to modify it.
 */

class RecyclerViewMatcher(private val recyclerViewId: Int) {
    fun atPosition(position: Int, targetViewId: Int = -1): Matcher<View> =
        object : TypeSafeMatcher<View>() {
            var resources: Resources? = null
            var childView: View? = null

            override fun describeTo(description: Description) {
                val idDescription = try {
                    resources?.getResourceName(recyclerViewId)
                } catch (_: Resources.NotFoundException) {
                    "$recyclerViewId (resource name not found)"
                } ?: recyclerViewId.toString()
                description.appendText("RecyclerView with id: $idDescription at position: $position")
            }

            override fun matchesSafely(view: View): Boolean {
                resources = view.resources
                if (childView == null) {
                    childView =
                        (view.rootView.findViewById<View>(recyclerViewId) as? RecyclerView)
                            ?.findViewHolderForAdapterPosition(position)
                            ?.itemView
                            ?: return false
                }
                return when (targetViewId) {
                    -1 -> childView === view
                    else -> childView!!.findViewById<View>(targetViewId) === view
                }
            }
        }
}

fun withRecyclerView(recyclerViewId: Int) = RecyclerViewMatcher(recyclerViewId)

class ContainsMatcher(private val text: String, private val ignoreCase: Boolean) :
    BoundedMatcher<View?, TextView>(TextView::class.java) {
    override fun matchesSafely(item: TextView): Boolean {
        return try {
            if (!ignoreCase) {
                item.text.toString().contains(text)
            } else {
                item.text.toString().lowercase().contains(text.lowercase())
            }
        } catch (e: Exception) {
            false
        }
    }

    override fun describeTo(description: Description) {
        description.appendText("(looking for text $text)")
    }
}

fun containsText(text: String): ContainsMatcher {
    return ContainsMatcher(text, true)
}

fun containsTextWithCase(text: String): ContainsMatcher {
    return ContainsMatcher(text, false)
}
