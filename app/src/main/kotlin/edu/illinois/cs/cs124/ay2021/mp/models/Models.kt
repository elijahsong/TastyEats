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
class Restaurant(val name: String, val cuisine: String, val id: String) : SortedListAdapter.ViewModel {
    constructor() : this("", "", "")

    companion object {
        /*
         * Function to compare Restaurant instances by name.
         * Currently this does not work, but you will need to implement it correctly for MP1.
         * Comparator is like Comparable, except it defines one possible ordering, not a canonical ordering for a class,
         * and so is implemented as a separate method rather than directly by the class as is done with Comparable.
         * !! We may implement other sorts in the future!
         */
        val SORT_BY_NAME = Comparator<Restaurant> { restaurant1, restaurant2 ->
            restaurant1.name.compareTo(restaurant2.name)
        }
    }

    // You should eventually add a toString method for debugging purposes

    // You should not need to modify this code, which is used by the list adapter component
    override fun <T : Any> isSameModelAs(model: T) = this == model

    // You should not need to modify this code, which is used by the list adapter component
    override fun <T : Any> isContentTheSameAs(model: T) = this == model
}

/*
Kotlin extension method for lists
 */
fun List<Restaurant>.search(input: String): List<Restaurant> {
    val cleanInput = input.lowercase().trim()
    val toReturn = mutableListOf<Restaurant>()

    if (cleanInput.isEmpty() || cleanInput == "") {
        return this.toList()
    }
    // Add all cuisines to a list
    val cuisines = mutableSetOf<String>()
    for (restaurant in this) { cuisines += restaurant.cuisine }

    // Check if cuisine in the list
    if (cuisines.contains(cleanInput)) {
        // return all restaurants from that cuisine
        for (restaurant in this) {
            if (restaurant.cuisine == cleanInput) {
                toReturn += restaurant
            }
        }
    } else {
        for (restaurant in this) {
            if (restaurant.name.lowercase().contains(cleanInput) || restaurant.cuisine.lowercase().contains
                (cleanInput)
            ) {
                println("Added ${restaurant.name}")
                toReturn += restaurant
            }
        }
    }
    return toReturn
}

/*
 * Model storing information about preferences retrieved from the restaurant server.
 *
 * You will need to understand some of the code in this file and make changes starting with MP2 part 1.
 *
 */
class Preference(val id: String, val restaurantIDs: List<String>) {
    constructor() : this("", listOf<String>())
}

/*
 * Related restaurants
 */
class RelatedRestaurants(restaurants: List<Restaurant>, preferences: List<Preference>) {
    private val restRel: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()
    init {
        // the passed list of restaurants is the list of valid restaurants we can use; ones not in this list should
        // not be in either map
        val validRestaurants = mutableListOf<String>()
        for (restaurant in restaurants) {
            validRestaurants += restaurant.id
        }
        // populate restaurantRelationship; each Preference object in preferences
        for (preference in preferences) {
            for (r1 in preference.restaurantIDs) {
                if (!validRestaurants.contains(r1)) {
                    break
                }
                for (r2 in preference.restaurantIDs) {
                    if (r1 != r2 && validRestaurants.contains(r2)) {
                        var strength = restRel[r1]?.get(r2) ?: 0
                        strength++
                        if (restRel[r1] != null) {
                            restRel[r1]!![r2] = strength
                        } else {
                            restRel[r1] = mutableMapOf(r2 to strength)
                        }
                    }
                }
            }
        }
    }
    fun getRelated(from: String): Map<String, Int> {
        return restRel[from] ?: mapOf()
    }
}
