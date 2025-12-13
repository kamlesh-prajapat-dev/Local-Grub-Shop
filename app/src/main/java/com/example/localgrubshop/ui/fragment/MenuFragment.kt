package com.example.localgrubshop.ui.fragment

import android.app.AlertDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.localgrubshop.R
import com.example.localgrubshop.databinding.FragmentMenuBinding
import com.example.localgrubshop.domain.models.FoodItem
import com.example.localgrubshop.ui.adapter.FoodItemAdapter
import com.example.localgrubshop.ui.viewmodel.MenuViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuFragment : Fragment(), FoodItemAdapter.FoodItemClickListener {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by viewModels()
    private lateinit var foodItemAdapter: FoodItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        foodItemAdapter = FoodItemAdapter(this)
        binding.foodItemsRecyclerView.adapter = foodItemAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {

        }

        viewLifecycleOwner.lifecycleScope.launch {

        }
    }

    private fun showNoInternetDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.no_internet_connection)
            .setMessage(R.string.check_internet_connection)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onSelectItem(
        item: FoodItem,
        isSelected: Boolean
    ) {

    }
}