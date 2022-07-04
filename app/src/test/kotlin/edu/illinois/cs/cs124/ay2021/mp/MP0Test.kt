package edu.illinois.cs.cs124.ay2021.mp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.google.common.truth.Truth.assertWithMessage
import edu.illinois.cs.cs124.ay2021.mp.application.EatableApplication
import edu.illinois.cs.cs124.ay2021.mp.models.Restaurant
import edu.illinois.cs.cs124.ay2021.mp.network.Client
import edu.illinois.cs.cs124.ay2021.mp.network.Client.getRestaurants
import edu.illinois.cs.cs124.ay2021.mp.network.RESTAURANT_COUNT
import edu.illinois.cs.cs124.ay2021.mp.network.Server
import edu.illinois.cs.cs124.ay2021.mp.network.loadRestaurants
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.http.HttpStatus
import org.junit.After
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.robolectric.annotation.LooperMode
import java.util.concurrent.CompletableFuture

/*
 * This is the MP0 test suite.
 * The code below is used to evaluate your app during testing, local grading, and official grading.
 * You will probably not understand all of the code below, but you'll need to have some understanding of how it works
 * so that you can determine what is wrong with your app and what you need to fix.
 *
 * ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.
 * You may modify the code below if it is useful during your own local testing, but any changes you make will
 * be lost once you submit.
 * Please keep that in mind, particularly if you see differences between your local scores and your official scores.
 *
 * Our test suites are broken into two parts.
 * The unit tests (in the UnitTests class) are tests that we can perform without running your app.
 * They test things like whether a specific method works properly, or the behavior of your API server.
 * Unit tests are usually fairly fast.
 *
 * The integration tests (in the IntegrationTests class) are tests that require simulating your app.
 * This allows us to test things like your API client, and higher-level aspects of your app's behavior, such as whether
 * it displays the right thing on the display.
 * Because integration tests require simulating your app, they run slower.
 *
 * Our test suites will also include a mixture of graded and ungraded tests.
 * The graded tests are marking with the `@Graded` annotation which contains a point total.
 * Ungraded tests do not have this annotation.
 * Some ungraded tests will work immediately, and are there to help you pinpoint regressions: meaning changes that
 * you made that might have broken things that were working previously.
 * The ungraded tests below were actually written by me (Geoff) during MP development.
 * Other ungraded tests are simply there to help your development process.
 */
@RunWith(Enclosed::class)
class MP0Test {

    // Unit tests that don't require simulating the entire app
    class UnitTests {
        // Create an HTTP client to test the server with
        private val httpClient = OkHttpClient()

        init {
            // Start the API server
            Server.start()
        }

        // THIS TEST SHOULD WORK
        // Test whether the loadRestaurants method works properly
        @Test(timeout = 1000L)
        fun testLoadRestaurants() {
            // Parse the String returned by loadRestaurants as JSON
            val restaurantList = ObjectMapper().readTree(loadRestaurants())
            // Check that it's a JSON array
            assertWithMessage("Restaurants is not a JSON array")
                .that(restaurantList).isInstanceOf(ArrayNode::class.java)
            // Check that the array has the right size
            assertWithMessage("Restaurant list is not the right size").that(restaurantList).hasSize(RESTAURANT_COUNT)
        }

        // THIS TEST SHOULD WORK
        // Test whether the GET /restaurants server route works properly
        @Test(timeout = 1000L)
        fun testRestaurantsRoute() {
            // Formulate a GET request to the API server for the /restaurants route
            val courseRequest = Request.Builder().url(EatableApplication.SERVER_URL + "restaurants/").build()
            // Execute the request
            val courseResponse = httpClient.newCall(courseRequest).execute()
            // The request should have succeeded
            assertWithMessage("Request should have succeeded").that(courseResponse.code).isEqualTo(HttpStatus.SC_OK)
            // The response body should not be null
            val body = courseResponse.body
            assertWithMessage("Response body should not be null").that(body).isNotNull()
            // The response body should be a JSON array with the expected size
            val restaurantList = ObjectMapper().readTree(body!!.string())
            assertWithMessage("Restaurant list is not the right size").that(restaurantList).hasSize(RESTAURANT_COUNT)
        }
    }

    // Integration tests that require simulating the entire app
    @RunWith(AndroidJUnit4::class)
    @LooperMode(LooperMode.Mode.PAUSED)
    class IntegrationTests {
        init {
            // Set up logging so that you can see log output during testing
            configureLogging()
            // Establish a separate API client for testing
            Client.start()
        }

        // After each test make sure the client connected successfully
        @After
        fun checkClient() {
            assertWithMessage("Client should be connected").that(Client.connected)
        }

        // Graded test that the activity displays the correct title
        @Graded(points = 90)
        @Test(timeout = 10000L)
        fun testActivityTitle() {
            // Start the main activity
            startActivity().onActivity { activity ->
                // Once the activity starts, check that it has the correct title
                assertWithMessage("MainActivity has wrong title").that(activity.title).isEqualTo("Find Restaurants")
            }
        }

        // THIS TEST SHOULD WORK
        // Test that the main activity displays the right number of restaurants after launch
        @Test(timeout = 10000L)
        fun testActivityRestaurantCount() {
            startActivity()
            onView(withId(R.id.recycler_view)).check(countRecyclerView(RESTAURANT_COUNT))
        }

        // THIS TEST SHOULD WORK
        // Test that the API client retrieves the list of restaurants correctly
        @Test(timeout = 1000L)
        fun testClientGetRestaurants() {
            // A CompletableFuture allows us to wait for the result of an asynchronous call
            val completableFuture = CompletableFuture<List<Restaurant?>>()
            // When getRestaurants returns, we complete the CompletableFuture
            getRestaurants { restaurants -> completableFuture.complete(restaurants) }
            // Wait for the CompletableFuture to complete
            val restaurants = completableFuture.get()
            // The List<Restaurant> should not be null, which is returned by getRestaurants when something
            // went wrong
            assertWithMessage("Request failed").that(restaurants).isNotNull()
            // Check that the List<Restaurant> has the correct size
            assertWithMessage("Restaurant list is not the right size").that(restaurants).hasSize(RESTAURANT_COUNT)
        }
    }
}
