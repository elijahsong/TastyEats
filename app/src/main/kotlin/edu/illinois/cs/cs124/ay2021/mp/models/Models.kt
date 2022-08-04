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
class Restaurant(val name: String, val cuisine: String, val id: String, var strength: Int = 0) : SortedListAdapter
    .ViewModel {
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
    override fun toString(): String = this.name
    override fun equals(other: Any?) = when {
        javaClass != other?.javaClass -> false
        else -> {
            other as Restaurant
            id == other.id
        }
    }
    override fun hashCode() = id.hashCode()
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
    private val restMap: MutableMap<String, Restaurant> = mutableMapOf()
    init {
        // the passed list of restaurants is the list of valid restaurants we can use; ones not in this list should
        // not be in either map
        for (restaurant in restaurants) {
            restMap[restaurant.id] = restaurant
        }
        // populate restaurantRelationship; each Preference object in preferences
        for (preference in preferences) {
            for (r1 in preference.restaurantIDs) {
                if (!restMap.containsKey(r1)) {
                    break
                }
                for (r2 in preference.restaurantIDs) {
                    if (r1 != r2 && restMap.containsKey(r2)) {
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
    fun getRelatedInOrder(from: String): List<Restaurant> {
        require(restMap.containsKey(from))
        val myMap = getRelated(from) // Map<String, Int>, where String is RestaurantIDs
        val newList = mutableListOf<Restaurant>()
        for ((restID, strength) in myMap) {
            val restObject = restMap[restID]
            restObject!!.strength = strength
            newList += restObject
        }
        return newList.sortedBy { it.name }.sortedBy { -1 * it.strength }
    }
    fun getConnectedTo(from: String): Set<Restaurant> {
        require(restMap.containsKey(from))
        val connectionIDs = mutableSetOf<String>()
        val toReturn = mutableSetOf<Restaurant>()
        helper(from, connectionIDs, 1)
        for (restID in connectionIDs) {
            if (restID != from) {
                toReturn += restMap[restID]!!
            }
        }
        return toReturn
    }
    private fun helper(restID: String, returnIDs: MutableSet<String>, distance: Int) { // void helper method that
        if (distance > 2) {
            return
        }
        val neighbors = getRelated(restID) // list of nodes connected to restID
        for ((neighbor, strength) in neighbors) {
            if (strength >= 2) {
                returnIDs += neighbor
                helper(neighbor, returnIDs, distance + 1)
            }
        }
    }
}
