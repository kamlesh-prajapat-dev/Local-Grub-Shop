package com.example.localgrubshop.ui.screens.eachorderstatus

import android.annotation.SuppressLint
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
import androidx.navigation.fragment.navArgs
import com.example.localgrubshop.R
import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.databinding.FragmentEachOrderStatusBinding
import com.example.localgrubshop.domain.mapper.firebase.GetReqDomainFailure
import com.example.localgrubshop.domain.mapper.firebase.WriteReqDomainFailure
import com.example.localgrubshop.ui.adapter.OrderSummaryAdapter
import com.example.localgrubshop.utils.OrderStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class EachOrderStatusFragment : Fragment() {
    private var _binding: FragmentEachOrderStatusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EachOrderStatusViewModel by viewModels()
    private val orderSummaryAdapter: OrderSummaryAdapter by lazy {
        OrderSummaryAdapter()
    }
    private val args: EachOrderStatusFragmentArgs by navArgs()
    private var nextStatus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orderId = args.orderId
        viewModel.observeOrderById(orderId)
    }

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
        setupClickListeners()
        observeOrderStatusUpdate()
    }

    private fun onSetRecyclerView() {
        binding.orderedItemsRecyclerView.adapter = orderSummaryAdapter
    }

    private fun setupUI(order: Order) {
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
    }

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
                viewModel.updateOrderStatus(order = order, newStatus = nextStatus!!)
            }
        }
    }

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

                        is EachOrderUIState.GetFailure -> {
                            when (val failure = result.failure) {
                                is GetReqDomainFailure.DataNotFount -> {
                                    viewModel.onSetOrder(null)
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

                        is EachOrderUIState.Success -> {
                            Toast.makeText(
                                requireContext(),
                                "Order status updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            onSetLoading(false)
                        }

                        EachOrderUIState.NoInternet -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }

                        is EachOrderUIState.OrderGetSuccess -> {
                            viewModel.onSetOrder(result.order)
                            onSetLoading(false)
                        }

                        is EachOrderUIState.WriteFailure -> {
                            when(val failure = result.failure) {
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
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.order.collect {
                    if (it != null) {
                        setupUI(it)
                    }
                }
            }
        }
    }
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

    fun onSetLoading(isLoading: Boolean) {
        binding.loadingIndicator.isVisible = isLoading
        binding.btnUpdateOrderStatus.isEnabled = !isLoading
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

    // destroy binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}