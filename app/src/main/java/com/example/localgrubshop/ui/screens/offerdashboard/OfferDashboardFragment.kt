package com.example.localgrubshop.ui.screens.offerdashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.localgrubshop.R
import com.example.localgrubshop.data.models.GetOffer
import com.example.localgrubshop.databinding.FragmentOfferDashboardBinding
import com.example.localgrubshop.ui.adapter.OfferAdapter
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OfferDashboardFragment : Fragment() {

    private var _binding: FragmentOfferDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OfferDashboardViewModel by viewModels()
    private val adapter: OfferAdapter by lazy {
        OfferAdapter(
            onEditClick = { offer ->
                val action = OfferDashboardFragmentDirections
                    .actionOfferDashboardFragmentToOfferBuilderFragment(
                        offer
                    )
                findNavController().navigate(action)
            },
            onItemClick = { offer ->
                val action =
                    OfferDashboardFragmentDirections.actionOfferDashboardFragmentToOfferDetailsFragment(
                        offer
                    )
                findNavController().navigate(action)
            }
        )
    }
    private var currentOffers: List<GetOffer> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.fetchOffers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfferDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        setupTabLayout()
        setupSearchView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvOffers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@OfferDashboardFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAddOffer.setOnClickListener {
            findNavController().navigate(R.id.action_offerDashboardFragment_to_offerBuilderFragment)
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> adapter.submitList(viewModel.activeOffers.value)
                    1 -> adapter.submitList(viewModel.expiredOffers.value)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearchView() {
        binding.searchOffer.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = currentOffers.filter {
                    it.promoCode.contains(newText ?: "", ignoreCase = true)
                }
                adapter.submitList(filteredList)
                return true
            }
        })
    }


    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is OfferDashboardUIState.Loading -> {
                                // binding.progressBar.visibility = View.VISIBLE
                            }
                            is OfferDashboardUIState.Success -> {
                                // binding.progressBar.visibility = View.GONE
                                viewModel.onSetOffers(state.offers)
                            }
                            is OfferDashboardUIState.Failure -> {
                               // binding.progressBar.visibility = View.GONE
                                Toast.makeText(context, "Failed to load offers", Toast.LENGTH_SHORT).show()
                            }
                            is OfferDashboardUIState.NoInternet -> {
                                // binding.progressBar.visibility = View.GONE
                                Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                            }
                            else -> Unit
                        }
                    }
                }
                launch {
                    viewModel.activeOffers.collectLatest {
                        if (binding.tabLayout.selectedTabPosition == 0) {
                            currentOffers = it
                            adapter.submitList(it)
                            binding.emptyState.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                }
                launch {
                    viewModel.expiredOffers.collectLatest {
                        if (binding.tabLayout.selectedTabPosition == 1) {
                            currentOffers = it
                            adapter.submitList(it)
                            binding.emptyState.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
