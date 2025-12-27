package com.example.localgrubshop.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.localgrubshop.data.models.Order
import com.example.localgrubshop.databinding.OrderHistoryItemBinding
import com.example.localgrubshop.utils.OrderStatus
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(private val onOrderClick: (Order) -> Unit) :
    ListAdapter<Order, OrderHistoryAdapter.OrderHistoryViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val binding =
            OrderHistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
        holder.itemView.setOnClickListener { onOrderClick(order) }
    }
    // View Holder class
    class OrderHistoryViewHolder(private val binding: OrderHistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // load data into view
        fun bind(order: Order) {
            binding.customerNameTextView.text = order.userName
            binding.orderDateTextView.text = formatDate(Date(order.placeAt))
            binding.totalPriceTextView.text = "Rs. ${order.totalPrice}"

            val status = order.status
            when (status) {
                OrderStatus.DELIVERED -> {
                    binding.orderStatusTextView.setBackgroundResource(com.example.localgrubshop.R.drawable.green_status_background)
                }
                OrderStatus.CANCELLED -> {
                    binding.orderStatusTextView.setBackgroundResource(com.example.localgrubshop.R.drawable.red_status_background)
                }
                else -> {
                    binding.orderStatusTextView.setBackgroundResource(com.example.localgrubshop.R.drawable.orange_status_background)
                }
            }
            binding.orderStatusTextView.text = order.status
        }
        // formate date into readable format
        private fun formatDate(date: Date): String {
            val sdf = SimpleDateFormat("dd MMM yyyy 'at' hh:mm a", Locale.getDefault())
            return sdf.format(date)
        }
    }
    // change data into one item
    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}