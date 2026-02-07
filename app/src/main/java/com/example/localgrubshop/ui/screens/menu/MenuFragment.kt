package com.example.localgrubshop.ui.screens.menu

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.localgrubshop.R
import com.example.localgrubshop.data.models.FetchedDish
import com.example.localgrubshop.databinding.FragmentMenuBinding
import com.example.localgrubshop.domain.mapper.firebase.GetReqDomainFailure
import com.example.localgrubshop.domain.mapper.firebase.WriteReqDomainFailure
import com.example.localgrubshop.ui.adapter.MenuAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MenuViewModel by viewModels()
    private val menuAdapter: MenuAdapter by lazy {
        MenuAdapter(
            onEditClick = { dish ->
                // Navigate to an edit screen, passing the dish ID
                val action = MenuFragmentDirections.actionMenuFragmentToDishFragment(dish)
                findNavController().navigate(action)
                Toast.makeText(requireContext(), "Edit ${dish.name}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { dish ->
                showDeleteConfirmationDialog(dish)
            },
            onStockChange = { dish, isChecked ->
                viewModel.updateStockStatus(dish, isChecked)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadMenu()
    }

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
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        binding.foodItemsRecyclerView.adapter = menuAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        is MenuUIState.GetFailure -> {
                            when (val failure = it.failure) {
                                is GetReqDomainFailure.DataNotFount -> {
                                    viewModel.onSetMenuItems(emptyList())
                                }
                                is GetReqDomainFailure.InvalidData -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                GetReqDomainFailure.Network -> {
                                    showNoInternetDialog()
                                }
                                is GetReqDomainFailure.PermissionDenied -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                is GetReqDomainFailure.Unknown -> {
                                    Toast.makeText(requireContext(), failure.cause.message, Toast.LENGTH_LONG).show()
                                }
                            }
                            onSetLoading(false)
                        }

                        is MenuUIState.WriteFailure -> {
                            when(val failure = it.failure) {
                                is WriteReqDomainFailure.Cancelled -> Unit
                                is WriteReqDomainFailure.DataNotFound -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                WriteReqDomainFailure.NoInternet -> {
                                    showNoInternetDialog()
                                }
                                is WriteReqDomainFailure.PermissionDenied -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                                is WriteReqDomainFailure.Unknown -> {
                                    Toast.makeText(requireContext(), failure.cause.message, Toast.LENGTH_LONG).show()
                                }
                                is WriteReqDomainFailure.ValidationError -> {
                                    Toast.makeText(requireContext(), failure.message, Toast.LENGTH_LONG).show()
                                }
                            }
                            onSetLoading(false)
                        }

                        MenuUIState.Idle -> {
                            onSetLoading(false)
                        }

                        is MenuUIState.IsInternetAvailable -> {
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
                            onSetLoading(false)
                        }

                        is MenuUIState.StockUpdateSuccess -> {
                            onSetLoading(false)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.menuItems.collect { dishes ->
                    if (dishes.isNotEmpty()) {
                        binding.emptyStateTxt.isVisible = false
                        binding.foodItemsRecyclerViewContainer.isVisible = true
                        menuAdapter.submitList(dishes)
                    } else {
                        binding.emptyStateTxt.isVisible = true
                        binding.foodItemsRecyclerViewContainer.isVisible = false
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.addFoodItemFab.setOnClickListener {
            // Navigate to an add/edit screen
            val action = MenuFragmentDirections.actionMenuFragmentToDishFragment(null)
            findNavController().navigate(action)
            Toast.makeText(requireContext(), "Add new food item", Toast.LENGTH_SHORT).show()
        }

        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showDeleteConfirmationDialog(dish: FetchedDish) {
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
            }
            .create()
            .show()
    }

    private fun onSetLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.addFoodItemFab.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
