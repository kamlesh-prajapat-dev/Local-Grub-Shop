package com.example.localgrubshop.ui.screens.offerbuilder

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.localgrubshop.R
import com.example.localgrubshop.data.models.NewOffer
import com.example.localgrubshop.databinding.FragmentOfferBuilderBinding
import com.example.localgrubshop.utils.OfferStatus
import com.example.localgrubshop.utils.OfferType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class OfferBuilderFragment : Fragment() {

    private var _binding: FragmentOfferBuilderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OfferBuilderViewModel by viewModels()
    private val args: OfferBuilderFragmentArgs by navArgs()
    private var selectedExpiryDate: Long = 0L
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.ivOfferBanner.setImageURI(it)
                binding.ivOfferBanner.visibility = View.VISIBLE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfferBuilderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        checkForEditMode()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUploadBanner.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.etExpiryDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSaveOffer.setOnClickListener {
            viewModel.saveOffer(
                offer = createOfferFromInputs(),
                id = args.offer?.id ?: "",
                imageUri = selectedImageUri
            )
        }

        binding.toggleDiscountType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    binding.btnPercentage.id -> binding.tilMaxDiscount.visibility = View.VISIBLE
                    binding.btnFixed.id -> binding.tilMaxDiscount.visibility = View.GONE
                }
            }
        }
    }

    private fun checkForEditMode() {
        args.offer?.let { offer ->
            binding.toolbar.title = "Edit Offer"
            binding.etPromoCode.setText(offer.promoCode)
            binding.etDescription.setText(offer.description)
            binding.etDiscountValue.setText(offer.discountValue.toString())
            binding.etMinOrder.setText(offer.minOrderAmount.toString())
            offer.maxDiscountAmount.let { binding.etMaxDiscount.setText(it.toString()) }

            selectedExpiryDate = offer.expiryDate
            updateExpiryDateText()

            if (offer.discountType == OfferType.PERCENTAGE) {
                binding.toggleDiscountType.check(binding.btnPercentage.id)
            } else {
                binding.toggleDiscountType.check(binding.btnFixed.id)
            }
            binding.switchActive.isChecked = (offer.offerStatus == OfferStatus.ACTIVE)

            if (offer.bannerImageUrl.isNotEmpty()) {
                binding.ivOfferBanner.visibility = View.VISIBLE
                Glide.with(binding.ivOfferBanner.context)
                    .load(offer.bannerImageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(binding.ivOfferBanner)
            } else {
                binding.ivOfferBanner.isVisible = false
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        if (selectedExpiryDate > 0) calendar.timeInMillis = selectedExpiryDate

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selected = Calendar.getInstance()
                selected.set(year, month, day)
                selectedExpiryDate = selected.timeInMillis
                updateExpiryDateText()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
            show()
        }
    }

    private fun updateExpiryDateText() {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.etExpiryDate.setText(sdf.format(Date(selectedExpiryDate)))
    }

    private fun createOfferFromInputs(): NewOffer {
        return NewOffer(
            promoCode = binding.etPromoCode.text.toString().trim().uppercase(),
            description = binding.etDescription.text.toString().trim(),
            discountType = if (binding.toggleDiscountType.checkedButtonId == binding.btnPercentage.id) OfferType.PERCENTAGE else OfferType.FIXED,
            discountValue = binding.etDiscountValue.text.toString().toDoubleOrNull() ?: 0.0,
            minOrderAmount = binding.etMinOrder.text.toString().toDoubleOrNull() ?: 0.0,
            maxDiscountAmount = binding.etMaxDiscount.text.toString().toDoubleOrNull(),
            expiryDate = selectedExpiryDate,
            offerStatus = if (binding.switchActive.isChecked) OfferStatus.ACTIVE else OfferStatus.INACTIVE
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Inside observeViewModel()
                viewModel.uiState.collect { state ->
                    when (state) {
                        is OfferBuilderUIState.Loading -> {
                            binding.btnSaveOffer.isEnabled = false
                        }

                        is OfferBuilderUIState.Success -> {
                            Toast.makeText(context, "Offer saved successfully", Toast.LENGTH_SHORT)
                                .show()
                            findNavController().navigateUp()
                        }

                        is OfferBuilderUIState.Failure -> {
                            binding.btnSaveOffer.isEnabled = true
                            Toast.makeText(context, "${state.failure}", Toast.LENGTH_SHORT).show()
                        }

                        is OfferBuilderUIState.ValidationError -> {
                            binding.btnSaveOffer.isEnabled = true
                            Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                        }

                        is OfferBuilderUIState.NoInternet -> {
                            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT)
                                .show()
                        }

                        is OfferBuilderUIState.Idle -> {
                            binding.btnSaveOffer.isEnabled = true
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
