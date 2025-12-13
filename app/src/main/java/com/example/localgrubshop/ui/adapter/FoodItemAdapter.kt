package com.example.localgrubshop.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.localgrubshop.R
import com.example.localgrubshop.databinding.FoodItemCardBinding
import com.example.localgrubshop.domain.models.FoodItem

class FoodItemAdapter(private val listener: FoodItemClickListener) : ListAdapter<FoodItem, FoodItemAdapter.FoodItemViewHolder>(FoodItemDiffCallback()) {

    interface FoodItemClickListener {
        fun onSelectItem(item: FoodItem, isSelected: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodItemViewHolder {
        val binding = FoodItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FoodItemViewHolder(private val binding: FoodItemCardBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: FoodItem) {
            binding.dishNameTextView.text = item.name
            binding.dishDescriptionTextView.text = item.description
            binding.priceTextView.text = "Rs. ${item.price}"

            val rawImageUrl = item.imageUrl.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/")

            Glide.with(binding.root.context)
                .load(rawImageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.dishImageView)

            val context = binding.root.context
            if (!item.isSelected) {
                binding.addBtn.text = "Add"
                binding.addBtn.setIconResource(R.drawable.ic_add)
                binding.addBtn.setBackgroundColor(ContextCompat.getColor(context,R.color.green))
            } else {
                binding.addBtn.text = "Added"
                binding.addBtn.setIconResource(R.drawable.ic_right)
                binding.addBtn.setIconTintResource(R.color.green)
                binding.addBtn.setBackgroundColor(ContextCompat.getColor(context,R.color.orange))
            }

            binding.addBtn.setOnClickListener { listener.onSelectItem(item = item, isSelected = !item.isSelected) }
        }
    }
    class FoodItemDiffCallback : DiffUtil.ItemCallback<FoodItem>() {
        override fun areItemsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FoodItem, newItem: FoodItem): Boolean {
            return oldItem == newItem
        }
    }
}
