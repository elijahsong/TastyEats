package edu.illinois.cs.cs124.ay2021.mp.network

import android.os.Build
import android.util.Log
import com.android.volley.ExecutorDelivery
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.VolleyLog
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.NoCache
import com.android.volley.toolbox.StringRequest
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import edu.illinois.cs.cs124.ay2021.mp.application.EatableApplication
import edu.illinois.cs.cs124.ay2021.mp.models.Preference
import edu.illinois.cs.cs124.ay2021.mp.models.Restaurant
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors

// You may find this helpful during debugging
private val TAG = Client::class.java.simpleName

// Various constants used by the client during startup
private const val INITIAL_CONNECTION_RETRY_DELAY = 1000L
private const val MAX_STARTUP_RETRIES = 8
private const val THREAD_POOL_SIZE = 4

// We are using the Jackson JSON serialization library to deserialize data from the server
private val objectMapper = ObjectMapper().apply {
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}

/*
 * Client object used by the app to interact with the restaurant API server.
 *
 * In Kotlin a class declared using object rather than class follows what is called the singleton pattern, meaning
 * that there can only be up to 1 instance of that class created, and it will be created as soon as it is needed.
 * Other than that it functions identically to the classes that you are familiar with, including allowing us to use
 * init blocks, define functions, and store state.
 *
 * You will need to understand some of the code here and make changes starting with MP2.
 */
object Client {
    /*
     * Retrieve and deserialize a list of restaurants from the backend server.
     * Takes as an argument a callback method to call when the request completes which will be passed the deserialized
     * list of restaurants received from the server.
     * We will discuss callbacks in more detail once you need to augment this code in MP2.
     *
     * Note that this is an example of the ability in Kotlin to define a parameter that is a method.
     * In this case callback is any method that receives a List<Restaurant> as its parameter and does not return a
     * value, or has return type Unit.
     */
    fun getRestaurants(callback: ((List<Restaurant>?) -> Unit)) {
        /*
         * Construct the request itself.
         * We use a StringRequest allowing us to receive a String from the server.
         * The String will be valid JSON containing a list of restaurant objects which we can deserialize into
         * instances of our Restaurant model.
         */
        val restaurantsRequest = StringRequest(
            Request.Method.GET,
            EatableApplication.SERVER_URL + "restaurants/", // URL to retrieve
            { response: String? ->
                // This code runs on success
                Log.d("TRACE", "[Client] restaurant request completed")
                /*
                 * Deserialize the String into a List<Restaurant> using Jackson.
                 * The new TypeReference<List<Restaurant>>() {} is the bit of magic required to have Jackson
                 * return a List with the correct type.
                 * We wrap this in a try-catch to handle deserialization errors that may occur.
                 */
                try {
                    val restaurants =
                        objectMapper.readValue(
                            response,
                            object : TypeReference<List<Restaurant>>() {}
                        )
                    // Call the callback method and pass it the list of restaurants
                    callback(restaurants)
                } catch (e: Exception) {
                    // There are better approaches than returning null here, but we need to do something to make sure
                    // that the callback returns even on error
                    callback(null)
                }
            },
            { error: VolleyError ->
                // This code runs on failure
                Log.e(TAG, error.toString())
                // There are better approaches than returning null here, but we need to do something to make sure that
                // the callback returns even on error
                callback(null)
            }
        )
        Log.d("TRACE", "[Client] Queueing request for restaurants using volley")
        requestQueue.add(restaurantsRequest) // ask volley to perform this request from the restaurantRequest object
    }

    /*
     * Retrieves preferences
     */
    fun getPreferences(callback: ((List<Preference>?) -> Unit)) {
        val preferencesRequest = StringRequest(
            Request.Method.GET,
            EatableApplication.SERVER_URL + "preferences/", // URL to retrieve
            { response: String? ->
                // This code runs on success
                Log.d("TRACE", "[Client] preference request completed")
                try {
                    val preferences =
                        objectMapper.readValue(
                            response,
                            object : TypeReference<List<Preference>>() {}
                        )
                    callback(preferences)
                } catch (e: Exception) {
                    callback(null)
                }
            },
            { error: VolleyError ->
                //this code runs on failure
                Log.e(TAG, error.toString())
                callback(null)
            }
        )
        Log.d("TRACE", "[Client] Queueing request for restaurants using volley")
        requestQueue.add(preferencesRequest)
    }
    /*
     * You do not need to modify the code below.
     * However, you may want to understand how it works.
     * The client tests to make sure it can connect to the backend server on startup.
     * We also initialize the client somewhat differently depending on whether we are testing your code or actually
     * running the app.
     */
    var connected = false
    private val requestQueue = if (Build.FINGERPRINT == "robolectric") {
        RequestQueue(
            NoCache(),
            BasicNetwork(HurlStack()),
            THREAD_POOL_SIZE,
            ExecutorDelivery(Executors.newSingleThreadExecutor())
        )
    } else {
        RequestQueue(
            NoCache(),
            BasicNetwork(HurlStack()),
        )
    }

    init {
        // Quiet Volley's otherwise verbose logging
        VolleyLog.DEBUG = false

        HttpURLConnection.setFollowRedirects(true)

        val serverURL: URL = try {
            URL(EatableApplication.SERVER_URL)
        } catch (e: MalformedURLException) {
            Log.e(TAG, "Bad server URL: " + EatableApplication.SERVER_URL)
            throw e
        }

        Thread {
            repeat(MAX_STARTUP_RETRIES) {
                @Suppress("EmptyCatchBlock")
                try {
                    (serverURL.openConnection() as HttpURLConnection).apply {
                        connect()
                        check(inputStream.bufferedReader().readText() == "CS 124")
                        disconnect()
                    }
                    connected = true
                    requestQueue.start()
                    return@Thread
                } catch (e: Exception) {
                }
                try {
                    Thread.sleep(INITIAL_CONNECTION_RETRY_DELAY)
                } catch (ignored: InterruptedException) {
                }
            }
            error { "Could not connect to server" }
        }.start()
    }

    // Dummy method to force singleton creation
    fun start() {}
}
