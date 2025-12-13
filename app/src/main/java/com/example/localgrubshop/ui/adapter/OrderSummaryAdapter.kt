package com.example.localgrubshop.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.localgrubshop.data.models.Dishes
import com.example.localgrubshop.databinding.OrderItemSummaryRowBinding

class OrderSummaryAdapter : ListAdapter<Dishes, OrderSummaryAdapter.OrderItemViewHolder>(DishesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding =
            OrderItemSummaryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OrderItemViewHolder(private val binding: OrderItemSummaryRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint( "SetTextI18n")
        fun bind(item: Dishes) {
            binding.dishName.text = item.name
            binding.dishQuantity.text = "x${item.quantity}"
            binding.dishPrice.text = "Rs. ${item.price * item.quantity}"
        }
    }

    class DishesDiffCallback : DiffUtil.ItemCallback<Dishes>() {
        override fun areItemsTheSame(oldItem: Dishes, newItem: Dishes): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Dishes, newItem: Dishes): Boolean {
            return oldItem == newItem
        }
    }
}