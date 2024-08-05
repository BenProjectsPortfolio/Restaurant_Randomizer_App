package com.example.watueat.ui.view

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watueat.databinding.MainFragmentBinding
import com.example.watueat.model.RestaurantViewModel
import com.example.watueat.ui.RestaurantDiffAdapter
import kotlin.random.Random

class MainFragment : Fragment() {

    private val viewModel: RestaurantViewModel by activityViewModels()
    private lateinit var adapter: RestaurantDiffAdapter
    private var _binding: MainFragmentBinding? = null
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
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RestaurantDiffAdapter(this.requireContext(), viewModel){
            val action = MainFragmentDirections.actionMainFragmentToSelectedRestaurant(it)
            findNavController().navigate(action)
        }
        binding.restaurantRV.adapter = adapter
        binding.restaurantRV.layoutManager = LinearLayoutManager(this.context)

        // Referenced: FC9 pressing enter invokes DONE action
        // https://developer.android.com/reference/android/widget/TextView.OnEditorActionListener#onEditorAction(android.widget.TextView,%20int,%20android.view.KeyEvent)
        binding.foodSearchET.setOnEditorActionListener { _, actionId, event ->
            if ((event != null
                        && (event.action == KeyEvent.ACTION_DOWN)
                        && (event.keyCode == KeyEvent.KEYCODE_ENTER))
                || (actionId == EditorInfo.IME_ACTION_DONE)) {
                hideKeyboard()
                binding.searchButton.callOnClick()
            }
            false
        }

        binding.locationSearchET.setOnEditorActionListener { _, actionId, event ->
            if ((event != null
                        && (event.action == KeyEvent.ACTION_DOWN)
                        && (event.keyCode == KeyEvent.KEYCODE_ENTER))
                || (actionId == EditorInfo.IME_ACTION_DONE)) {
                hideKeyboard()
                binding.searchButton.callOnClick()
            }
            false
        }

        binding.searchButton.setOnClickListener {
            viewModel.fetchRestaurants(binding.foodSearchET.text.toString(), binding.locationSearchET.text.toString())
            hideKeyboard()
        }

        viewModel.observeListOfRestaurants().observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        binding.watueatButton.setOnClickListener {
            if (adapter.currentList.isNotEmpty()) {
                val list = adapter.currentList
                val randomRestaurantIndex = Random.nextInt(list.size)
                val randomRestaurant = list[randomRestaurantIndex]
                val action = MainFragmentDirections.actionMainFragmentToSelectedRestaurant(randomRestaurant)
                findNavController().navigate(action)
                Toast.makeText(context, "Lets try ${randomRestaurant.name} \uD83C\uDF7D", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Search some areas first!!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}
