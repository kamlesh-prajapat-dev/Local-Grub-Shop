package com.example.localgrubshop.ui.fragment

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.localgrubshop.R
import com.example.localgrubshop.databinding.FragmentHomeBinding
import com.example.localgrubshop.domain.models.OrderHistoryResult
import com.example.localgrubshop.ui.adapter.OrderHistoryAdapter
import com.example.localgrubshop.ui.viewmodel.HomeViewModel
import com.example.localgrubshop.ui.viewmodel.SharedHFToEOSFViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var orderHistoryAdapter: OrderHistoryAdapter
    private val sharedViewModel: SharedHFToEOSFViewModel by activityViewModels()
    private var currentStatusFilter: String? = null
    private var currentStartDate: Date? = null
    private var currentEndDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOrderHistoryItems()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupListeners()
    }

    private fun setupListeners() {
        binding.filterChip.setOnClickListener {
            val bottomSheet = FilterBottomSheetFragment { status, startDate, endDate ->
                currentStatusFilter = status
                currentStartDate = startDate
                currentEndDate = endDate
                filterOrders()
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

    private fun filterOrders() {
        val currentList = viewModel.historyOrder.value
        val filteredList = currentList.filter { order ->
            val statusMatch = currentStatusFilter == null || order.status == currentStatusFilter
            val dateMatch = isDateInRange(order.placeAt.toDate(), currentStartDate, currentEndDate)
            statusMatch && dateMatch
        }
        orderHistoryAdapter.submitList(filteredList)
    }

    private fun isDateInRange(date: Date, startDate: Date?, endDate: Date?): Boolean {
        if (startDate == null && endDate == null) return true
        if (startDate != null && endDate != null) {
            return date.after(startDate) && date.before(endDate)
        }
        if (startDate != null) {
            return date.after(startDate)
        }
        if (endDate != null) {
            return date.before(endDate)
        }
        return false
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
            viewModel.uiState.collect {
                when (it) {
                    OrderHistoryResult.Idle -> {
                        // Initial state, do nothing
                        onSetLoading(false)
                    }

                    OrderHistoryResult.Loading -> {
                        // Loading state, show loading indicator
                        onSetLoading(true)
                    }

                    is OrderHistoryResult.Success -> {
                        // Success state, handle as needed
                        val orders = it.orders
                        if (orders.isNotEmpty()) {
                            binding.orderHistoryContainer.visibility = View.VISIBLE
                            binding.noOrdersTextView.visibility = View.GONE
                            orderHistoryAdapter.submitList(orders)
                            viewModel.onSetHistoryOrder(orders)
                            filterOrders()
                        } else {
                            binding.orderHistoryContainer.visibility = View.GONE
                            binding.noOrdersTextView.visibility = View.VISIBLE
                        }
                        onSetLoading(false)
                    }

                    is OrderHistoryResult.Error -> {
                        // Error state, handle as needed
                        onSetLoading(false)
                        Toast.makeText(
                            requireContext(),
                            "Failed to update order status",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is OrderHistoryResult.UpdateSuccess -> {
                        onSetLoading(false)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isNetworkAvailable.collect {
                if (!it) showNoInternetDialog()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect {
                if (it != null) {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
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
                viewModel.onSetIsNetworkAvailable()
                viewModel.loadOrderHistoryItems()
            }
            .create()
            .show()
    }

    fun onSetLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
