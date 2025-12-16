package com.example.localgrubshop.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.localgrubshop.R
import com.example.localgrubshop.data.models.OldDish
import com.example.localgrubshop.databinding.FoodItemCardBinding

class MenuAdapter(
    private val onEditClick: (OldDish) -> Unit,
    private val onDeleteClick: (OldDish) -> Unit,
    private val onStockChange: (OldDish, Boolean) -> Unit
) : ListAdapter<OldDish, MenuAdapter.MenuViewHolder>(DishDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = FoodItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MenuViewHolder(private val binding: FoodItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dish: OldDish) {
            binding.dishNameTextView.text = dish.name
            binding.priceTextView.text = "Rs. ${dish.price}"
            binding.descriptionTextView.text = dish.description

            val rawImageUrl = dish.thumbnail.replace("github.com", "raw.githubusercontent.com").replace("/blob/", "/")

            Glide.with(binding.dishImageView.context)
                .load(rawImageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.dishImageView)

            if(dish.isAvailable) {
                binding.inStockSwitch.isChecked = dish.isAvailable
                binding.inStockSwitch.text = "In Stock"
            } else {
                binding.inStockSwitch.isChecked = dish.isAvailable
                binding.inStockSwitch.text = "Out of Stock"
            }

            binding.inStockSwitch.setOnCheckedChangeListener { _, isChecked ->
                onStockChange(dish, isChecked)
            }

            binding.editButton.setOnClickListener {
                onEditClick(dish)
            }

            binding.deleteButton.setOnClickListener {
                onDeleteClick(dish)
            }
        }
    }

    class DishDiffCallback : DiffUtil.ItemCallback<OldDish>() {
        override fun areItemsTheSame(oldItem: OldDish, newItem: OldDish): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OldDish, newItem: OldDish): Boolean {
            return oldItem == newItem
        }
    }
}
