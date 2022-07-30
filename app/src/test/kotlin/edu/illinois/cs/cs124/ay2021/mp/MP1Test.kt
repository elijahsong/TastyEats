@file:Suppress("SpellCheckingInspection")

package edu.illinois.cs.cs124.ay2021.mp

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import edu.illinois.cs.cs124.ay2021.mp.application.EatableApplication
import edu.illinois.cs.cs124.ay2021.mp.models.Restaurant
import edu.illinois.cs.cs124.ay2021.mp.models.search
import edu.illinois.cs.cs124.ay2021.mp.network.Server
import edu.illinois.cs.cs124.ay2021.mp.network.loadRestaurants
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.http.HttpStatus
import org.junit.Ignore
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode
import java.util.Random

/*
 * This is the MP1 test suite.
 * The code below is used to evaluate your app during testing, local grading, and official grading.
 * You will probably not understand all of the code below, but you'll need to have some understanding of how it works
 * so that you can determine what is wrong with your app and what you need to fix.
 *
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 * You may modify the code below if it is useful during your own local testing, but any changes you make will
 * be lost once you submit.
 * Please keep that in mind, particularly if you see differences between your local scores and your official scores.
 *
 * For more notes on testing, please see the MP0 test suites (MP0Test.java).
 *
 * Version 2, updated 10/24/2021.
 */

// Random number generator, seeded to provide a reproducible random number stream
private val random = Random(124)

// Before testing begins, load the restaurant list using Server.loadRestaurants so that we have a loaded list for
// testing purposes
// A Kotlin run block runs the first time the variable is accessed and can initialize it
private var restaurants = run {
    val objectMapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    try {
        objectMapper.readValue(loadRestaurants(), object : TypeReference<List<Restaurant>>() {})
    } catch (e: JsonProcessingException) {
        throw IllegalStateException(e)
    }
}.also {
    // Also runs as a separate step after the block it is attached to, and is passed the result from the previous
    // block as it
    check(it.size == RESTAURANT_COUNT) { "Wrong restaurant count" }
}

@RunWith(Enclosed::class)
class MP1Test {

    // Unit tests that don't require simulating the entire app
    class UnitTests {
        // Create an HTTP client to test the server with
        private val httpClient = OkHttpClient()

        init {
            // Start the API server
            Server.start()
        }

        // Test whether the restaurant comparator works properly
        @Graded(points = 20)
        @Test(timeout = 1000L)
        fun testRestaurantCompareByName() {
            // The list returned from Server.loadRestaurants should not be sorted, to prevent solving the problem by
            // modifying restaurants.csv
            assertWithMessage("Initial list should not be sorted").that(isSortedByName(restaurants)).isFalse()
            // Repeat the test 32 times
            repeat(32) {
                // Copy the restaurant list
                val sortedRestaurants = restaurants.toMutableList()
                // Remove a random subset of restaurants
                repeat(random.nextInt(7) + 1) {
                    sortedRestaurants.removeAt(random.nextInt(sortedRestaurants.size))
                }
                // Check to make sure that the pruned list is not sorted
                assertWithMessage("Pruned list should not be sorted").that(isSortedByName(sortedRestaurants)).isFalse()
                // Check to make sure that the sorted list is sorted
                assertWithMessage("List should be sorted")
                    .that(isSortedByName(sortedRestaurants.sortedWith((Restaurant.SORT_BY_NAME))))
                    .isTrue()
            }
        }

        // UNGRADED TEST
        // THIS TEST SHOULD NOT WORK INITIALLY
        // To enable it, remove or comment out the @Ignore annotation below
        // This test checks to make sure that you are parsing the right fields from the node JSON
        @Ignore("Enable once you begin working on search")
        @Test(timeout = 1000L)
        fun testLoadRestaurantFields() {
            // Build a GET request for /restaurants
            val courseRequest = Request.Builder().url(EatableApplication.SERVER_URL + "restaurants/").build()
            // Execute the request
            val courseResponse = httpClient.newCall(courseRequest).execute()
            // The request should have succeeded
            assertWithMessage("Request should succeed").that(courseResponse.code).isEqualTo(HttpStatus.SC_OK)
            // The response body should not be null
            val body = courseResponse.body
            assertWithMessage("Body should not be null").that(body).isNotNull()
            // The response body should be a JSON array
            val restaurantList = ObjectMapper().readTree(body!!.string())
            assertWithMessage("Request should return a JSON array").that(restaurantList is ArrayNode).isTrue()
            // The JSON array should contain the correct number of restaurants
            assertWithMessage("Wrong restaurant count").that(restaurantList).hasSize(RESTAURANT_COUNT)
            // Check the JSON nodes for the correct fields
            for (restaurantNode in restaurantList) {
                assertWithMessage("JSON is missing field id").that(restaurantNode.has("id")).isTrue()
                assertWithMessage("JSON is missing field name").that(restaurantNode.has("name")).isTrue()
                assertWithMessage("JSON is missing field cuisine").that(restaurantNode.has("cuisine")).isTrue()
                assertWithMessage("JSON is missing field url").that(restaurantNode.has("url")).isTrue()
            }
        }

        // Test the restaurant search method
        @Graded(points = 20)
        @Test(timeout = 1000L)
        fun testRestaurantSearch() {
            // Test corner cases
            // Empty string should return all restaurants
            assertThat(restaurants.search("")).hasSize(RESTAURANT_COUNT)
            assertThat(restaurants.search("  ")).hasSize(RESTAURANT_COUNT)
            // Empty string should return a copy of the list, not the original list
            assertThat(restaurants.search("")).isNotSameInstanceAs(restaurants)

            // We use a mix of inputs that either match or don't match a cuisine value
            assertThat(restaurants.search("pizz")).hasSize(17)
            // The search method should not modify the passed list
            assertWithMessage("search modified the passed list").that(restaurants).hasSize(RESTAURANT_COUNT)

            // Test other searches
            assertThat(restaurants.search("pizza")).hasSize(14)
            assertThat(restaurants.search("ba")).hasSize(49)
            assertThat(restaurants.search("bar")).hasSize(2)
            assertThat(restaurants.search("BAR")).hasSize(2)
            assertThat(restaurants.search("bar ")).hasSize(2)
            assertThat(restaurants.search("t a")).hasSize(4)
            assertThat(restaurants.search("n a")).hasSize(2)
            assertThat(restaurants.search("store")).hasSize(11)
            assertThat(restaurants.search("SteakHouse")).hasSize(4)
            assertThat(restaurants.search("GrIll")).hasSize(24)
            assertThat(restaurants.search(" TharA")).hasSize(1)
            assertThat(restaurants.search("juicery ")).hasSize(1)
            assertThat(restaurants.search("gracie").isEmpty())
        }
    }

    @RunWith(AndroidJUnit4::class)
    @LooperMode(LooperMode.Mode.PAUSED)
    class IntegrationTests {
        init {
            // Set up logging so that you can see log output during testing
            configureLogging()
            // Start the MainActivity before each test
            startActivity()
        }

        // Test that the MainActivity displays the list of restaurants properly
        // The list should be sorted, and the cuisine values should be displayed
        @Graded(points = 20)
        @Test(timeout = 10000L)
        fun testRestaurantListView() {
            // Check that we still have the full list of restaurants
            assertThat(restaurants).hasSize(RESTAURANT_COUNT)
            // Generate a sorted lists of restaurants to use
            val sortedRestaurants = restaurants.sortedWith(Restaurant.SORT_BY_NAME)
            // Check to make sure that the sorted list is actually sorted, in case SORT_BY_NAME is still broken
            assertWithMessage("List should be sorted").that(isSortedByName(sortedRestaurants)).isTrue()

            // Pause to let the UI catch up
            pause()
            // Once the list is shown by the MainActivity, check to make sure it has the right number of restaurants
            onView(withId(R.id.recycler_view)).check(countRecyclerView(RESTAURANT_COUNT))
            // Check the first displayed restaurant
            onView(withRecyclerView(R.id.recycler_view).atPosition(0))
                .check(matches(hasDescendant(containsText("A Taste of Both Worlds"))))

            // Count non-empty cuisine values
            var cuisineCount = 0
            // Check a random subset of restaurants
            repeat(8) {
                // Pick a random restaurant from the list
                val position = random.nextInt(sortedRestaurants.size)
                // Track non-empty cuisine values
                val restaurant = sortedRestaurants[position]
                if (restaurant.cuisine != "") {
                    cuisineCount++
                }
                // Scroll to the right place
                onView(withId(R.id.recycler_view))
                    .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(position))
                // Make sure that the item at that position has the correct name and that the cuisine is also shown
                onView(withRecyclerView(R.id.recycler_view).atPosition(position))
                    .check(matches(hasDescendant(containsText(restaurant.name))))
                onView(withRecyclerView(R.id.recycler_view).atPosition(position))
                    .check(matches(hasDescendant(containsText(restaurant.cuisine))))
            }
            // Make sure we see the right number of non-empty cuisine values
            assertWithMessage("Didn't find enough non-empty cuisine value").that(cuisineCount).isEqualTo(7)
        }

        // Test that the search bar in the MainActivity works
        // This requires both that the restaurant search work correctly and a set of changes to the MainActivity
        @Graded(points = 20)
        @Test(timeout = 10000L)
        fun testRestaurantSearchFunction() {
            // Check that the right number of restaurants is shown initially
            onView(withId(R.id.recycler_view)).check(countRecyclerView(RESTAURANT_COUNT))

            // Perform a search that returns no results
            onView(withId(R.id.search)).perform(searchFor("ethiopian"))
            // Pauses are required here to let the UI catch up
            pause()
            // There should be no results shown
            onView(withId(R.id.recycler_view)).check(countRecyclerView(0))
            // Make sure that clearing the search causes the full list to be displayed again
            onView(withId(R.id.search)).perform(searchFor(""))
            pause()
            onView(withId(R.id.recycler_view)).check(countRecyclerView(RESTAURANT_COUNT))

            // Check a few more searches

            onView(withId(R.id.search)).perform(searchFor("PIZZ"))
            pause()
            onView(withId(R.id.recycler_view)).check(countRecyclerView(17))

            onView(withId(R.id.search)).perform(searchFor("Ba"))
            pause()
            onView(withId(R.id.recycler_view)).check(countRecyclerView(49))

            onView(withId(R.id.search)).perform(searchFor("Thara Thai"))
            pause()
            onView(withId(R.id.recycler_view)).check(countRecyclerView(1))
        }
    }
}

// Helper method for checking the comparator
private fun isSortedByName(restaurants: List<Restaurant>): Boolean {
    for (i in 0 until restaurants.size - 1) {
        if (restaurants[i].name > restaurants[i + 1].name) {
            return false
        }
    }
    return true
}

// Need to move this here so that it can't be modified by submitted code
private const val RESTAURANT_COUNT = 255
