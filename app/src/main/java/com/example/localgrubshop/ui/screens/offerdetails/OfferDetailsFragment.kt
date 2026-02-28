package com.example.localgrubshop.ui.screens.offerdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.localgrubshop.R
import com.example.localgrubshop.databinding.FragmentOfferDetailsBinding
import com.example.localgrubshop.utils.OfferStatus
import com.example.localgrubshop.utils.OfferType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class OfferDetailsFragment : Fragment() {

    private var _binding: FragmentOfferDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OfferDetailsViewModel by viewModels()
    private val args: OfferDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfferDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        displayOfferDetails()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEditOffer.setOnClickListener {
            val action = OfferDetailsFragmentDirections.actionOfferDetailsFragmentToOfferBuilderFragment(args.offer)
            findNavController().navigate(action)
        }

        binding.btnDeleteOffer.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.switchOfferStatus.setOnCheckedChangeListener { _, isChecked ->
            val newStatus = if (isChecked) OfferStatus.ACTIVE else OfferStatus.INACTIVE
            if (newStatus != args.offer.offerStatus) {
                viewModel.updateOfferStatus(args.offer.id, newStatus)
            }
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Offer")
            .setMessage("Are you sure you want to delete this offer? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteOffer(args.offer.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun displayOfferDetails() {
        val offer = args.offer
        binding.apply {
            tvPromoCode.text = offer.promoCode
            tvDescription.text = offer.description
            
            val discountText = if (offer.discountType == OfferType.PERCENTAGE) {
                "${offer.discountValue.toInt()}% OFF"
            } else {
                "₹${offer.discountValue.toInt()} OFF"
            }
            tvDiscountMain.text = discountText

            tvMinOrder.text = "Min. Order Value: ₹${offer.minOrderAmount}"
            tvMaxDiscount.text = if (offer.maxDiscountAmount != null) {
                "Max Discount: ₹${offer.maxDiscountAmount}"
            } else {
                "Max Discount: No Limit"
            }

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvExpiryDate.text = "Expires on: ${sdf.format(Date(offer.expiryDate))}"

            updateStatusUI(offer.offerStatus)
            
            // Set switch initial state
            switchOfferStatus.isChecked = offer.offerStatus == OfferStatus.ACTIVE

            if (offer.bannerImageUrl.isNotEmpty()) {
                ivOfferBanner.visibility = View.VISIBLE
                bannerOverlay.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(offer.bannerImageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivOfferBanner)
            } else {
                ivOfferBanner.visibility = View.GONE
                bannerOverlay.visibility = View.GONE
            }
            
            // Mock Analytics data
            tvTotalRedeems.text = "124"
            tvTotalRevenue.text = "₹8.2k"
            tvTotalDiscountGiven.text = "Total discount given: ₹1,650"
        }
    }

    private fun updateStatusUI(status: String) {
        binding.apply {
            if (status == OfferStatus.ACTIVE) {
                tvStatusTag.text = "ACTIVE"
                tvStatusTag.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.green)
            } else {
                tvStatusTag.text = "INACTIVE"
                tvStatusTag.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.darker_gray)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is OfferDetailsUIState.Loading -> {
                            // Show loading indicator
                        }
                        is OfferDetailsUIState.DeleteSuccess -> {
                            Toast.makeText(context, "Offer deleted successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        is OfferDetailsUIState.StatusUpdateSuccess -> {
                            Toast.makeText(context, "Status updated", Toast.LENGTH_SHORT).show()
                            // Update local UI if needed, though usually, we'd navigate back or re-fetch
                        }
                        is OfferDetailsUIState.Failure -> {
                            Toast.makeText(context, "Error: ${state.failure}", Toast.LENGTH_SHORT).show()
                        }
                        is OfferDetailsUIState.NoInternet -> {
                            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
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
