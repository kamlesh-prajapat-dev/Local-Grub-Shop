package com.example.localgrubshop.ui.screens.eachorderstatus

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.localgrubshop.R
import com.example.localgrubshop.databinding.FragmentEachOrderStatusBinding
import com.example.localgrubshop.ui.adapter.OrderSummaryAdapter
import com.example.localgrubshop.ui.sharedviewmodel.SharedHFToEOSFViewModel
import com.example.localgrubshop.utils.OrderStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class EachOrderStatusFragment : Fragment() {
    private var _binding: FragmentEachOrderStatusBinding? = null // binding mutable variable
    private val binding get() = _binding!!// binding immutable variable
    private val viewModel: EachOrderStatusViewModel by viewModels() // view model for this fragment
    private lateinit var orderSummaryAdapter: OrderSummaryAdapter // adapter for the recycler view
    private val sharedViewModel: SharedHFToEOSFViewModel by activityViewModels() // Shared view model between HomeFragment and EachOrderStatusFragment for data share
    private var nextStatus: String? = null // next status of order for help to update order status

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEachOrderStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onSetRecyclerView()
        observeSharedViewModel()
        setupClickListeners()
        observeOrderStatusUpdate()
    }
    // Setup Order summary adapter Recycler for Recycler View
    private fun onSetRecyclerView() {
        orderSummaryAdapter = OrderSummaryAdapter()
        binding.orderedItemsRecyclerView.adapter = orderSummaryAdapter
    }
    // observe shared view model data for load order status screen
    private fun observeSharedViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.order.collect { order ->
                    if (order != null) {
                        binding.nameTextView.text = order.userName
                        binding.phoneNumberTextView.text = order.userPhoneNumber
                        binding.addressTextView.text = order.userAddress

                        val deliveryFee = 0.0 // Replace with your delivery fee logic
                        binding.itemTotalTextView.text = "Rs. ${order.totalPrice}"
                        binding.deliveryFeeTextView.text = "Rs. $deliveryFee"
                        binding.grandTotalTextView.text = "Rs. ${order.totalPrice + deliveryFee}"

                        updateUiForStatus(order.status)

                        val items = order.items
                        orderSummaryAdapter.submitList(items)

                        viewModel.onSetOrder(order)
                    }
                }
            }
        }
    }
    // Set up Click event
    private fun setupClickListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUpdateOrderStatus.setOnClickListener {
            val currentStatus = binding.orderStatusTextView.text.toString()
            nextStatus = when (currentStatus) {
                OrderStatus.PLACED -> OrderStatus.CONFIRMED
                OrderStatus.CONFIRMED -> OrderStatus.PREPARING
                OrderStatus.PREPARING -> OrderStatus.OUT_FOR_DELIVERY
                OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.DELIVERED
                else -> null
            }

            val order = viewModel.order.value
            if (nextStatus != null && order != null) {
                viewModel.updateOrderStatus(order, nextStatus!!)
            }

        }
    }
    // observe View model data for update ui
    private fun observeOrderStatusUpdate() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { result ->
                    when (result) {
                        EachOrderUIState.Idle -> {
                            onSetLoading(false)
                        }
                        EachOrderUIState.Loading -> {
                            onSetLoading(true)
                        }
                        is EachOrderUIState.Error -> {
                            onSetLoading(false)
                            Toast.makeText(requireContext(), result.e.message, Toast.LENGTH_SHORT).show()
                            viewModel.reset()
                        }
                        is EachOrderUIState.Success -> {
                            Toast.makeText(requireContext(), "Order status updated successfully", Toast.LENGTH_SHORT).show()
                            if (nextStatus != null) {
                                updateUiForStatus(nextStatus!!)
                                viewModel.reset()
                            }
                            onSetLoading(false)
                        }
                        EachOrderUIState.NoInternet -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }
                    }
                }
            }
        }
    }
    // update current status on screen
    private fun updateUiForStatus(status: String) {
        binding.orderStatusTextView.text = status
        when (status) {
            OrderStatus.PLACED -> {
                binding.orderStatusTextView.setBackgroundResource(R.drawable.orange_status_background)
                binding.btnUpdateOrderStatus.text = "Confirm Order"
                binding.btnUpdateOrderStatus.isEnabled = true
            }
            OrderStatus.CONFIRMED -> {
                binding.orderStatusTextView.setBackgroundResource(R.drawable.orange_status_background)
                binding.btnUpdateOrderStatus.text = "Start Preparing"
                binding.btnUpdateOrderStatus.isEnabled = true
            }
            OrderStatus.PREPARING -> {
                binding.orderStatusTextView.setBackgroundResource(R.drawable.orange_status_background)
                binding.btnUpdateOrderStatus.text = "Out for Delivery"
                binding.btnUpdateOrderStatus.isEnabled = true
            }
            OrderStatus.OUT_FOR_DELIVERY -> {
                binding.orderStatusTextView.setBackgroundResource(R.drawable.orange_status_background)
                binding.btnUpdateOrderStatus.text = "Delivered"
                binding.btnUpdateOrderStatus.isEnabled = true
            }
            OrderStatus.DELIVERED -> {
                binding.orderStatusTextView.setBackgroundResource(R.drawable.green_status_background)
                binding.btnUpdateOrderStatus.text = "Order Complete"
                binding.btnUpdateOrderStatus.isEnabled = false
            }
            else -> {
                binding.orderStatusTextView.setBackgroundResource(R.drawable.red_status_background)
                binding.btnUpdateOrderStatus.visibility = View.GONE
            }
        }
    }
    // set loading ui
    fun onSetLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    // Alert Dialog for no internet connection
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
    // destroy binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}