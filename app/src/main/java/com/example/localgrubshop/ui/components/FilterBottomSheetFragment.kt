package com.example.localgrubshop.ui.components

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.localgrubshop.databinding.BottomSheetFilterBinding
import com.example.localgrubshop.utils.OrderStatus
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FilterBottomSheetFragment(
    private val onFilterSelected: (String?, Date?, Date?) -> Unit
) : BottomSheetDialogFragment() {
    private var _binding: BottomSheetFilterBinding? = null
    private val binding get() = _binding!!
    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }
    // setup click listeners for filter buttons
    private fun setupClickListeners() {
        binding.startDateEditText.setOnClickListener {
            showDatePickerDialog(true)
        }

        binding.endDateEditText.setOnClickListener {
            showDatePickerDialog(false)
        }

        binding.applyButton.setOnClickListener {
            val selectedStatus = when (binding.statusChipGroup.checkedChipId) {
                binding.chipAll.id -> null
                binding.chipPlaced.id -> OrderStatus.PLACED
                binding.chipConfirmed.id -> OrderStatus.CONFIRMED
                binding.chipPreparing.id -> OrderStatus.PREPARING
                binding.chipOutForDelivery.id -> OrderStatus.OUT_FOR_DELIVERY
                binding.chipDelivered.id -> OrderStatus.DELIVERED
                else -> null
            }
            onFilterSelected(selectedStatus, startDate, endDate)
            dismiss()
        }

        binding.clearButton.setOnClickListener {
            binding.statusChipGroup.clearCheck()
            binding.startDateEditText.text = null
            binding.endDateEditText.text = null
            startDate = null
            endDate = null
            onFilterSelected(null, null, null)
            dismiss()
        }
    }
    // show date picker dialog
    private fun showDatePickerDialog(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                val selectedDate = selectedCalendar.time
                val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate)

                if (isStartDate) {
                    startDate = selectedDate
                    binding.startDateEditText.setText(formattedDate)
                } else {
                    endDate = selectedDate
                    binding.endDateEditText.setText(formattedDate)
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    // destroy binding
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}