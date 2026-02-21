package com.example.localgrubshop.ui.screens.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.localgrubshop.R
import com.example.localgrubshop.databinding.FragmentHomeBinding
import com.example.localgrubshop.domain.mapper.firebase.GetReqDomainFailure
import com.example.localgrubshop.ui.adapter.OrderHistoryAdapter
import com.example.localgrubshop.ui.components.FilterBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private val orderHistoryAdapter: OrderHistoryAdapter by lazy {
        OrderHistoryAdapter { order ->
            val action =
                HomeFragmentDirections.actionHomeFragmentToEachOrderStatusFragment(order.id)
            findNavController().navigate(action)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadOrders()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
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
                            viewModel.onSetOrders(it.orders)
                            onSetLoading(false)
                        }

                        is HomeUIState.Failure -> {
                            when (val failure = it.failure) {
                                is GetReqDomainFailure.DataNotFount -> {
                                    viewModel.onSetOrders(emptyList())
                                }

                                is GetReqDomainFailure.InvalidData -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                GetReqDomainFailure.Network -> {
                                    showNoInternetDialog()
                                }

                                is GetReqDomainFailure.PermissionDenied -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                is GetReqDomainFailure.Unknown -> {
                                    Toast.makeText(
                                        requireContext(),
                                        failure.cause.message,
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            onSetLoading(false)
                        }

                        HomeUIState.NoInternet -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }

                        is HomeUIState.UserGetFailure -> {
                            onSetLoading(false)
                        }

                        is HomeUIState.UserGetSuccess -> {
                            onSetLoading(false)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredList.collect {
                    if (it.isNotEmpty()) {
                        binding.orderItemsRecyclerViewContainer.visibility = View.VISIBLE
                        binding.noOrdersTextView.visibility = View.GONE
                        orderHistoryAdapter.submitList(it)
                    } else {
                        binding.orderItemsRecyclerViewContainer.visibility = View.GONE
                        binding.noOrdersTextView.visibility = View.VISIBLE
                    }
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