package edu.illinois.cs.cs124.ay2021.mp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.wrdlbrnft.sortedlistadapter.SortedListAdapter
import edu.illinois.cs.cs124.ay2021.mp.databinding.ItemRestaurantBinding
import edu.illinois.cs.cs124.ay2021.mp.models.Restaurant

/*
 * Helper class for our restaurant list.
 * You should not need to modify this code.
 */
internal class RestaurantListAdapter(context: Context, private val listener: Listener) :
    SortedListAdapter<Restaurant>(context, Restaurant::class.java, Restaurant.SORT_BY_NAME) {
    interface Listener {
        fun onClicked(restaurant: Restaurant)
    }

    override fun onCreateViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder<out Restaurant> {
        val binding: ItemRestaurantBinding = ItemRestaurantBinding.inflate(inflater, parent, false)
        return RestaurantViewHolder(binding, listener)
    }
}

internal class RestaurantViewHolder(
    private val binding: ItemRestaurantBinding,
    setListener: RestaurantListAdapter.Listener?
) : SortedListAdapter.ViewHolder<Restaurant>(binding.root) {
    init {
        binding.listener = setListener
    }

    override fun performBind(restaurant: Restaurant) {
        binding.restaurant = restaurant
    }
}
