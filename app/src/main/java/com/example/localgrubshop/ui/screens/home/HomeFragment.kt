package com.example.localgrubshop.ui.screens.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.localgrubshop.R
import com.example.localgrubshop.databinding.FragmentHomeBinding
import com.example.localgrubshop.ui.adapter.OrderHistoryAdapter
import com.example.localgrubshop.ui.components.FilterBottomSheetFragment
import com.example.localgrubshop.ui.sharedviewmodel.SharedHFToEOSFViewModel
import com.google.firebase.database.DatabaseError
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null // Data binding mutable variable
    private val binding get() = _binding!! // Data binding immutable variable
    private val viewModel: HomeViewModel by viewModels() // View model for its fragment
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter // Adapter for order history
    private val sharedViewModel: SharedHFToEOSFViewModel by activityViewModels() // Shared view model between HomeFragment and EachOrderStatusFragment for data share

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView() // Setup recycler view
        observeViewModel() // Observe ui state and orders state from view model and take action according this
        setupListeners() // setup listeners for click event and perform action
    }

    private fun setupListeners() {
        binding.filterChip.setOnClickListener {
            val bottomSheet = FilterBottomSheetFragment { status, startDate, endDate ->
                viewModel.filterOrders(status = status, startDate = startDate, endDate = endDate)
            }
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuBar -> {
                    showPopupMenu(binding.topAppBar.findViewById(R.id.menuBar))
                    true
                }
                else -> false
            }
        }
    }

    private fun showPopupMenu(anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.home_popup_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToMenuFragment()
                    findNavController().navigate(action)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun setupRecyclerView() {
        orderHistoryAdapter = OrderHistoryAdapter { order ->
            sharedViewModel.onSetOrder(order)
            val action = HomeFragmentDirections.actionHomeFragmentToEachOrderStatusFragment()
            findNavController().navigate(action)
        }
        binding.orderHistoryRecyclerView.adapter = orderHistoryAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        HomeUIState.Idle -> {
                            onSetLoading(false)
                        }

                        HomeUIState.Loading -> {
                            onSetLoading(true)
                        }

                        is HomeUIState.Success -> {
                            val orders = it.orders
                            if (orders.isNotEmpty()) {
                                viewModel.onSetHistoryOrder(orders)
                                binding.orderHistoryContainer.visibility = View.VISIBLE
                                binding.noOrdersTextView.visibility = View.GONE
                            } else {
                                binding.orderHistoryContainer.visibility = View.GONE
                                binding.noOrdersTextView.visibility = View.VISIBLE
                            }
                            viewModel.saveFCMToken()
                            onSetLoading(false)
                        }

                        is HomeUIState.Error -> {
                            Toast.makeText(
                                requireContext(),
                                it.e.message ?: "An error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                            onSetLoading(false)
                        }

                        is HomeUIState.UpdateSuccess -> {
                            onSetLoading(false)
                        }

                        HomeUIState.NoInternet -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyOrder.collect {
                    orderHistoryAdapter.submitList(it)
                }
            }
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

    private fun onSetLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
