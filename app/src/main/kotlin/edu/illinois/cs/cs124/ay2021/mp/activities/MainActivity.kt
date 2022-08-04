package edu.illinois.cs.cs124.ay2021.mp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import edu.illinois.cs.cs124.ay2021.mp.R
import edu.illinois.cs.cs124.ay2021.mp.adapters.RestaurantListAdapter
import edu.illinois.cs.cs124.ay2021.mp.application.EatableApplication
import edu.illinois.cs.cs124.ay2021.mp.databinding.ActivityMainBinding
import edu.illinois.cs.cs124.ay2021.mp.models.Preference
import edu.illinois.cs.cs124.ay2021.mp.models.Restaurant
import edu.illinois.cs.cs124.ay2021.mp.models.search
import edu.illinois.cs.cs124.ay2021.mp.network.Client

// You may find this useful when adding logging
@Suppress("unused")
val TAG = MainActivity::class.java.name

/*
 * App main activity.
 * Started when the app is launched, based on the configuration in the Android Manifest (AndroidManifest.xml).
 * Should display a sorted list of restaurants based on data retrieved from the server.
 *
 * You will need to understand some of the code here and make changes starting with MP1.
 */

@Suppress("unused")
class MainActivity :
    AppCompatActivity(),
    SearchView.OnQueryTextListener,
    RestaurantListAdapter.Listener {

    // The lateinit keyword allows us to not initialize a non-nullable value immediately, since in this case these are
    // set in onCreate.
    // The first time the variable is used Kotlin will throw an exception if it has not been initialized.

    // Binding to the layout defined in activity_main.xml
    private lateinit var binding: ActivityMainBinding

    // List adapter for displaying the list of restaurants
    private lateinit var listAdapter: RestaurantListAdapter

    // Reference to the persistent Application instance
    private lateinit var application: EatableApplication

    // onCreate will put a copy of the restaurant list here, we can put lateinit to force the list to initialize only
    // when needed, thereby avoid the using of not knowing how long the list of restaurants is (advanced)
    private lateinit var restaurants: List<Restaurant>

    // similarly, we will use a lateinit List to store our preferences
    private lateinit var preferences: List<Preference>

    /*
     * onCreate is the first method called when this activity is created.
     * Code here normally does a variety of setup tasks.
     */
    override fun onCreate(unused: Bundle?) {
        super.onCreate(unused)

        Log.d(TAG, "onCreate ran")
        // Initialize our data binding
        // This allows us to more easily access elements of our layout
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Create a new list adapter for restaurants and attach it to our app layout
        listAdapter = RestaurantListAdapter(this, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = listAdapter

        /*
         * Use our restaurant API to retrieve a list of restaurants.
         * What is passed to getRestaurants is a callback, which we'll discuss in more detail in the MP lessons.
         * Here we use Kotlin's trailing lambda syntax to define the code body of the callback directly.
         * Callbacks allow us to wait for something to complete and run code when it does.
         * In this case, once we retrieve a list of restaurants, we use it to update the contents of our list.
         */
        /*
        Client.getRestaurants(fun(restaurants: List<Restaurant>?) {
            check(restaurants != null)
            for (restaurant in restaurants) {
                copyOfRestaurants += restaurant
            }
            listAdapter.edit().replaceAll(restaurants).commit()
        })
         */
        // Using trailing lambda syntax
        Log.d("TRACE", "[MainActivity] Calling Client.getRestaurants")
        Client.getRestaurants { r ->
            Log.d("TRACE", "[MainActivity] Client.getRestaurants completed")
            check(r != null)
            restaurants = r
            listAdapter.edit().replaceAll(restaurants).commit()
        }
        Log.d("TRACE", "[MainActivity] Continuing with MainActivity")
        // Call Cilent.getPreference so the MainActivity stores a list of references AND a list of preferences
        // Make sure this comes AFTER getRestaurants completes
        Client.getPreferences { p ->
            check(p != null)
            preferences = p
        }
        // Create relatedRestaurant object using the constructor. We should not need a new route in Client.kt
        // Call methods in RestaurantActivity.kt
        // Bind to the search component so that we can receive events when the contents of the search box change

        // registers the callback in onCreate method
        binding.search.setOnQueryTextListener(this)

        // Bind the toolbar that contains our search component
        setSupportActionBar(binding.toolbar)

        // Set the activity title
        title = "Find Restaurants"
    }

    /*
     * Called when the user changes the text in the search bar.
     * Eventually (MP1) we'll want to update the list of restaurants shown based on their input.
     */
    override fun onQueryTextChange(query: String): Boolean {
        Log.i(TAG, "onQueryTextChange: $query")
        listAdapter.edit().replaceAll(restaurants.search(query)).commit()
        return true
    }

    /*
     * Called when the user clicks on one of the restaurants in the list.
     * Eventually (MP2) we'll launch a new activity here so they can see the restaurant details.
     */
    override fun onClicked(restaurant: Restaurant) {
        Log.i(TAG, "Someone clicked on ${restaurant.name} with id ${restaurant.id}")
        val launchRestaurantActivity = Intent(this, RestaurantActivity::class.java)
        // Add the restaurant ID to the intent as an extra with key id
        launchRestaurantActivity.putExtra("id", restaurant.id)
        startActivity(launchRestaurantActivity)
    }

    /*
     * Called when the user submits their search query.
     * We update the list as the text changes, so don't need to do anything special here.
     */
    override fun onQueryTextSubmit(query: String) = true
}
