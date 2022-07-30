package edu.illinois.cs.cs124.ay2021.mp.network

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.opencsv.CSVReaderBuilder
import edu.illinois.cs.cs124.ay2021.mp.application.EatableApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.io.IOException
import java.io.StringReader
import java.net.HttpURLConnection
import java.util.Scanner
import java.util.logging.Level
import java.util.logging.Logger

/*
 * Restaurant API server.
 *
 * Normally this code would run on a separate machine from your app, which would make requests to it over the internet.
 * However, for our MP we have this run inside the app alongside the rest of your code, to allow you to gain experience
 * with full-stack app development.
 * You are both developing the client (the Android app) and the server that it requests data from.
 * This is a very common programming paradigm and one used by most or all of the smartphone apps that you use regularly.
 *
 * You will need to some of the code here and make changes starting with MP1.
 */

// You may find this useful for debugging
@Suppress("unused")
private val TAG = Server::class.java.simpleName

/*
 * Load restaurant information from a CSV file and create a JSON array.
 * You will need to modify this code for MP1, and replicate it with some small changes using new data that we'll
 * provide starting with MP2.
 */
fun loadRestaurants(): String {
    val input = Scanner(Server::class.java.getResourceAsStream("/restaurants.csv"), "UTF-8").useDelimiter("\\A").next()
    val csvReader = CSVReaderBuilder(StringReader(input)).withSkipLines(1).build() // skip headers
    val restaurants = JsonNodeFactory.instance.arrayNode()
    for (parts in csvReader) {
        val restaurant = JsonNodeFactory.instance.objectNode().apply { // same as restaurant.put()
            put("id", parts[0])
            put("name", parts[1])
            put("cuisine", parts[2])
            put("url", parts[3])
        }
        restaurants.add(restaurant)
    }
    return restaurants.toPrettyString()
}

// Number of restaurants that we expect to find in the CSV file
// Normally this wouldn't be hardcoded but it's useful for testing
const val RESTAURANT_COUNT = 255

object Server : Dispatcher() {

    // Stores the JSON string containing information about all of the restaurants created during
    // server startup, initialized with the result of calling loadRestaurants
    private val restaurantsJson: String = loadRestaurants()

    // Helper method for the GET /restaurants route, called by the dispatch method below
    private fun getRestaurants() = MockResponse()
        // Indicate that the request succeeded (HTTP 200 OK)
        .setResponseCode(HttpURLConnection.HTTP_OK)
        // Load the JSON string with restaurant information into the body of the response
        .setBody(restaurantsJson)
        /*
         * Set the HTTP header that indicates that this is JSON with the utf-8 charset.
         * There are some special characters in our data set, so it's important to mark it as utf-8 so it is parsed
         * properly by clients.
         */
        .setHeader("Content-Type", "application/json; charset=utf-8")

    /*
     * Server request dispatcher.
     * Responsible for parsing the HTTP request and determining how to respond.
     * You will need to understand this code and augment it starting with MP2.
     */
    override fun dispatch(request: RecordedRequest): MockResponse {
        // Reject malformed requests
        if (request.path == null || request.method == null) {
            return MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
        }
        return try {
            /*
             * We perform a few normalization steps before we begin route dispatch, since this makes the if-else
             * statement below simpler.
             */
            // Normalize the path by removing trailing slashes and replacing multiple repeated slashes with single
            // slashes
            val path = request.path!!.removeSuffix("/").replace("/+", "/")
            // Normalize the request method by converting to uppercase
            val method = request.method!!.uppercase()

            // Main route dispatch tree, dispatching routes based on request path and type
            when {
                // This route is used by the client during startup, so don't remove
                path == "" && method == "GET" ->
                    MockResponse().setBody("CS 124").setResponseCode(HttpURLConnection.HTTP_OK)
                // Return the JSON list of restaurants for a GET request to the path /restaurants
                path == "/restaurants" && method == "GET" -> getRestaurants()
                // If the route didn't match above, then we return a 404 NOT FOUND
                else -> MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
                    // If we don't set a body here Volley will choke with a strange error.
                    // Normally a 404 for a web API would not need a body
                    .setBody("Not Found")
            }
        } catch (e: Exception) {
            // Return a HTTP 500 if an exception is thrown
            MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
        }
    }

    /*
     * You do not need to modify the code below.
     * However, you may want to understand how it works.
     * It implements the singleton pattern and initializes the server when Server.start() is called.
     * We also check to make sure that no other servers are running on the same machine, which can cause problems.
     */
    init {
        if (!isRunning(false)) {
            Logger.getLogger(MockWebServer::class.java.name).level = Level.OFF
            MockWebServer().apply {
                dispatcher = this@Server
                start(EatableApplication.DEFAULT_SERVER_PORT)
            }
        }
    }

    // Dummy method to force singleton creation
    fun start() {}
}

/*
 * You do not need to modify the code below.
 * However, you may want to understand how it works.
 * It determines whether a server is running by making a GET request for the / path and checking the response body.
 */
fun isRunning(wait: Boolean, retryCount: Int = 8, retryDelay: Long = 512): Boolean {
    for (i in 0 until retryCount) {
        val client = OkHttpClient()
        val request: Request = Request.Builder().url(EatableApplication.SERVER_URL).get().build()
        try {
            val response = client.newCall(request).execute()
            check(response.isSuccessful)
            check(response.body?.string() == "CS 124") {
                "Another server is running on ${EatableApplication.DEFAULT_SERVER_PORT}"
            }
            return true
        } catch (ignored: IOException) {
            if (!wait) {
                break
            }
            try {
                Thread.sleep(retryDelay)
            } catch (ignored1: InterruptedException) {
            }
        }
    }
    return false
}
