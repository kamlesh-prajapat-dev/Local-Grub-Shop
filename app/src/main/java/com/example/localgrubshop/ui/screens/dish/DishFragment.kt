package com.example.localgrubshop.ui.screens.dish

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
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
import com.example.localgrubshop.databinding.FragmentDishBinding
import com.example.localgrubshop.domain.mapper.firebase.WriteReqDomainFailure
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DishFragment : Fragment() {

    private var _binding: FragmentDishBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DishViewModel by viewModels()
    private val args: DishFragmentArgs by navArgs()
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dish = args.dish
        if (dish != null) {
            viewModel.onSetDish(dish)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.dishImageView.setOnClickListener {
            openGallery()
        }

        binding.saveButton.setOnClickListener {
            val name = binding.dishNameEditText.text.toString().trim()
            val description = binding.descriptionEditText.text.toString().trim()
            val price = binding.priceEditText.text.toString().toDoubleOrNull()
            val isAvailable = binding.availabilitySwitch.isChecked

            if (name.isNotEmpty() && description.isNotEmpty() && price != null) {
                viewModel.saveDish(name = name, description = description, price = price, imageUri = imageUri ?: viewModel.dish.value?.thumbnail?.toUri(), isAvailable = isAvailable)
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dish.collect { dish ->
                    if (dish != null) {
                        binding.dishNameEditText.setText(dish.name)
                        binding.descriptionEditText.setText(dish.description)
                        binding.priceEditText.setText(dish.price.toString())

                        if (dish.thumbnail.isNotEmpty()) {
                            val rawImageUrl = dish.thumbnail.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/")

                            Glide.with(binding.dishImageView.context)
                                .load(rawImageUrl)
                                .placeholder(R.drawable.ic_launcher_foreground)
                                .error(R.drawable.ic_launcher_foreground)
                                .into(binding.dishImageView)
                        }

                        binding.saveButton.text = "Update"
                        binding.availabilitySwitch.isChecked = dish.available
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when(it) {
                        is DishUIState.Failure -> {
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
                        DishUIState.Idle -> {
                            onSetLoading(false)
                        }
                        DishUIState.NoInternet -> {
                            showNoInternetDialog()
                            onSetLoading(false)
                        }
                        DishUIState.Loading -> {
                            onSetLoading(true)
                        }
                        is DishUIState.Success -> {
                            Toast.makeText(requireContext(), "Dish saved successfully", Toast.LENGTH_SHORT).show()
                            onSetLoading(false)
                            findNavController().navigateUp()
                        }
                        is DishUIState.Error -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                            onSetLoading(false)
                        }
                    }
                }
            }
        }
    }

    private val imagePicker =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                imageUri = uri
                binding.dishImageView.setImageURI(uri)
            }
        }

    private fun openGallery() {
        imagePicker.launch("image/*")
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

        binding.saveButton.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}