package com.example.watueat.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watueat.R
import com.example.watueat.databinding.FavoritesFragmentBinding
import com.example.watueat.model.RestaurantViewModel
import com.example.watueat.ui.RestaurantDiffAdapter
import kotlin.random.Random

class FavoritesFragment : Fragment() {

    private val viewModel: RestaurantViewModel by activityViewModels()
    private lateinit var adapter: RestaurantDiffAdapter
    private var _binding: FavoritesFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FavoritesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMenu()

        adapter = RestaurantDiffAdapter(this.requireContext(), viewModel){
            val action = FavoritesFragmentDirections.actionFavoritesToSelectedRestaurant(it)
            findNavController().navigate(action)
        }
        binding.favoritesRV.layoutManager = LinearLayoutManager(activity)
        binding.favoritesRV.adapter = adapter

        viewModel.observeListOfFavoriteRestaurants().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.pickRandomFavorite.setOnClickListener {
            if (adapter.currentList.isNotEmpty()) {
                val list = adapter.currentList
                val randomRestaurantIndex = Random.nextInt(list.size)
                val randomFavoriteRestaurant = list[randomRestaurantIndex]
                val action = FavoritesFragmentDirections.actionFavoritesToSelectedRestaurant(randomFavoriteRestaurant)
                findNavController().navigate(action)
                Toast.makeText(context, "Lets go eat at ${randomFavoriteRestaurant.name} again!!! \uD83D\uDE0B", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Favorite some restaurants first!!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initMenu() {
        requireActivity().addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.findItem(R.id.goToFavorites).setVisible(false)
                menu.findItem(R.id.logoutOfApp).setVisible(false)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.goToMaps -> {
                        findNavController().navigate(R.id.mapsFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
    }
}
