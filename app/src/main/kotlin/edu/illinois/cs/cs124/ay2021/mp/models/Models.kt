// ktlint-disable filename

package edu.illinois.cs.cs124.ay2021.mp.models

import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter

/*
 * Model storing information about a restaurant retrieved from the restaurant server.
 *
 * You will need to understand some of the code in this file and make changes starting with MP1.
 *
 */
@Suppress("unused")
class Restaurant(val name: String) : SortedListAdapter.ViewModel {
    constructor() : this("")

    companion object {
        /*
         * Function to compare Restaurant instances by name.
         * Currently this does not work, but you will need to implement it correctly for MP1.
         * Comparator is like Comparable, except it defines one possible ordering, not a canonical ordering for a class,
         * and so is implemented as a separate method rather than directly by the class as is done with Comparable.
         */
        val SORT_BY_NAME = Comparator<Restaurant> { restaurant1, restaurant2 -> 0 }
    }

    // You should not need to modify this code, which is used by the list adapter component
    override fun <T : Any> isSameModelAs(model: T) = this == model

    // You should not need to modify this code, which is used by the list adapter component
    override fun <T : Any> isContentTheSameAs(model: T) = this == model
}
