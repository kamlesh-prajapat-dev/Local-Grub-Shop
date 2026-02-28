package com.example.localgrubshop.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.localgrubshop.R
import com.example.localgrubshop.data.models.GetOffer
import com.example.localgrubshop.databinding.ItemOfferBinding
import com.example.localgrubshop.utils.OfferStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OfferAdapter(
    private val onEditClick: (GetOffer) -> Unit,
    private val onItemClick: (GetOffer) -> Unit
) : ListAdapter<GetOffer, OfferAdapter.OfferViewHolder>(OfferDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val binding = ItemOfferBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OfferViewHolder(private val binding: ItemOfferBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(offer: GetOffer) {
            binding.apply {
                tvPromoCode.text = offer.promoCode
                tvOfferDescription.text = offer.description
                
                val discountText = if (offer.discountType == "percentage") {
                    "${offer.discountValue.toInt()}%"
                } else {
                    "₹${offer.discountValue.toInt()}"
                }
                tvDiscountValue.text = discountText

                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val dateString = sdf.format(Date(offer.expiryDate))
                tvExpiryDate.text = "Expires on: $dateString"

                // Set tag color based on status or type
                val tagColor = if (offer.offerStatus == OfferStatus.ACTIVE) R.color.orange else android.R.color.darker_gray
                discountTypeTag.setBackgroundColor(ContextCompat.getColor(root.context, tagColor))

                btnEditOffer.setOnClickListener { onEditClick(offer) }
                root.setOnClickListener { onItemClick(offer) }
            }
        }
    }

    class OfferDiffCallback : DiffUtil.ItemCallback<GetOffer>() {
        override fun areItemsTheSame(oldItem: GetOffer, newItem: GetOffer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GetOffer, newItem: GetOffer): Boolean {
            return oldItem == newItem
        }
    }
}
