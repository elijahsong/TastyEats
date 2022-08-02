package edu.illinois.cs.cs124.ay2021.mp.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import edu.illinois.cs.cs124.ay2021.mp.R
import edu.illinois.cs.cs124.ay2021.mp.databinding.ActivityRestaurantBinding
import edu.illinois.cs.cs124.ay2021.mp.network.Client

class RestaurantActivity : AppCompatActivity() {
    // Binding to the layout defined in activity_main.xml
    private lateinit var binding: ActivityRestaurantBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("RestaurantActivity", "[RActivity] Started")

        // Retrieve the restaurant ID from the intent
        try {
            val restaurantID = intent.extras!!.get("id").toString()
            Log.d("restaurantID", restaurantID)
            // Convert into restaurant object using the map from client
            val restaurant = Client.restaurantMap[restaurantID]
            // Populate the UI with the name and cuisine
            binding = DataBindingUtil.setContentView(this, R.layout.activity_restaurant)
            binding.name.text = restaurant!!.name
            binding.cuisine.text = restaurant.cuisine
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            Log.e(TAG, "Error in retrieving Client info")
        }
    }
}
