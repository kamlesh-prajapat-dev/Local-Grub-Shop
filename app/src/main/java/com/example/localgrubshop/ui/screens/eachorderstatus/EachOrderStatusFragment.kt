package com.example.localgrubshop.ui.screens.eachorderstatus

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.localgrubshop.databinding.FragmentEachOrderStatusBinding
import com.example.localgrubshop.domain.models.OrderHistoryResult
import com.example.localgrubshop.ui.adapter.OrderSummaryAdapter
import com.example.localgrubshop.ui.sharedviewmodel.SharedHFToEOSFViewModel
import com.example.localgrubshop.utils.Constant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class EachOrderStatusFragment : Fragment() {
    private var _binding: FragmentEachOrderStatusBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EachOrderStatusViewModel by viewModels()
    private lateinit var orderSummaryAdapter: OrderSummaryAdapter

    private val sharedViewModel: SharedHFToEOSFViewModel by activityViewModels()
    private var nextStatus: String? = null

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

    private fun onSetRecyclerView() {
        orderSummaryAdapter = OrderSummaryAdapter()
        binding.orderedItemsRecyclerView.adapter = orderSummaryAdapter
    }

    private fun observeSharedViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
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

    private fun setupClickListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUpdateOrderStatus.setOnClickListener {
            val currentStatus = binding.orderStatusTextView.text.toString()
            nextStatus = when (currentStatus) {
                Constant.PLACED.name -> Constant.CONFIRMED.name
                Constant.CONFIRMED.name -> Constant.PREPARING.name
                Constant.PREPARING.name -> Constant.OUT_FOR_DELIVERY.name
                Constant.OUT_FOR_DELIVERY.name -> Constant.DELIVERED.name
                else -> null
            }

            val order = viewModel.order.value
            if (nextStatus != null && order != null) {
                viewModel.updateOrderStatus(order, nextStatus!!)
            }

        }
    }

    private fun observeOrderStatusUpdate() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateStatusResult.collect { result ->
                when (result) {
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
                        onSetLoading(false)
                    }
                    is OrderHistoryResult.Error -> {
                        // Error state, handle as needed
                        onSetLoading(false)
                        Toast.makeText(requireContext(), "Failed to update order status", Toast.LENGTH_SHORT).show()
                        viewModel.onSetStatusResult(OrderHistoryResult.Idle)
                    }
                    is OrderHistoryResult.UpdateSuccess -> {
                        Toast.makeText(requireContext(), "Order status updated successfully", Toast.LENGTH_SHORT).show()
                        if (nextStatus != null) {
                            updateUiForStatus(nextStatus!!)
                            viewModel.onSetStatusResult(OrderHistoryResult.Idle)
                        }
                        onSetLoading(false)
                    }
                }
            }
        }
    }

    private fun updateUiForStatus(status: String) {
        binding.orderStatusTextView.text = status
        when (status) {
            Constant.PLACED.name -> {
                binding.btnUpdateOrderStatus.text = "Confirm Order"
                binding.btnUpdateOrderStatus.isEnabled = true
            }
            Constant.CONFIRMED.name -> {
                binding.btnUpdateOrderStatus.text = "Start Preparing"
                binding.btnUpdateOrderStatus.isEnabled = true
            }
            Constant.PREPARING.name -> {
                binding.btnUpdateOrderStatus.text = "Out for Delivery"
                binding.btnUpdateOrderStatus.isEnabled = true
            }
            Constant.OUT_FOR_DELIVERY.name -> {
                binding.btnUpdateOrderStatus.text = "Delivered"
                binding.btnUpdateOrderStatus.isEnabled = true
            }
            Constant.DELIVERED.name -> {
                binding.btnUpdateOrderStatus.text = "Order Complete"
                binding.btnUpdateOrderStatus.isEnabled = false
            }
            else -> {
                binding.btnUpdateOrderStatus.visibility = View.GONE
            }
        }
    }

    fun onSetLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
