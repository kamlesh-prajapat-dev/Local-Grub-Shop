package com.example.localgrubshop.ui.screens.menu

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.localgrubshop.R
import com.example.localgrubshop.data.models.OldDish
import com.example.localgrubshop.databinding.FragmentMenuBinding
import com.example.localgrubshop.ui.adapter.MenuAdapter
import com.example.localgrubshop.ui.sharedviewmodel.SharedMDViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MenuViewModel by viewModels()
    private val sharedViewModel: SharedMDViewModel by activityViewModels()

    private lateinit var menuAdapter: MenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMenu()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        menuAdapter = MenuAdapter(
            onEditClick = { dish ->
                // Navigate to an edit screen, passing the dish ID
                sharedViewModel.onSetDish(dish)
                findNavController().navigate(R.id.action_menuFragment_to_dishFragment)
                Toast.makeText(requireContext(), "Edit ${dish.name}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { dish ->
                showDeleteConfirmationDialog(dish)
            },
            onStockChange = { dish, isChecked ->
                viewModel.updateStockStatus(dish, isChecked)
            }
        )
        binding.foodItemsRecyclerView.adapter = menuAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect {
                when (it) {
                    is MenuUIState.Failure -> {
                        onSetLoading(false)
                    }

                    MenuUIState.Idle -> {
                        onSetLoading(false)
                    }

                    MenuUIState.IsInternetAvailable -> {
                        showNoInternetDialog()
                        onSetLoading(false)
                    }

                    MenuUIState.Loading -> {
                        onSetLoading(true)
                    }

                    is MenuUIState.Success -> {
                        viewModel.onSetMenuItems(it.data)
                        onSetLoading(false)
                    }

                    is MenuUIState.DeleteSuccess -> {
                        viewModel.loadMenu()
                        onSetLoading(false)
                    }

                    is MenuUIState.StockUpdateSuccess -> {
                        viewModel.loadMenu()
                        onSetLoading(false)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.menuItems.collect { dishes ->
                menuAdapter.submitList(dishes)
            }
        }
    }

    private fun setupClickListeners() {
        binding.addFoodItemFab.setOnClickListener {
            // Navigate to an add/edit screen
            findNavController().navigate(R.id.action_menuFragment_to_dishFragment)
            Toast.makeText(requireContext(), "Add new food item", Toast.LENGTH_SHORT).show()
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showDeleteConfirmationDialog(dish: OldDish) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete ${dish.name}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMenuItem(dish)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNoInternetDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.no_internet_connection)
            .setMessage(R.string.check_internet_connection)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
                viewModel.loadMenu()
            }
            .create()
            .show()
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        sharedViewModel.reset()
    }
}
