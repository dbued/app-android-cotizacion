package com.example.radiomodern.ui.list

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.radiomodern.data.model.Quotation
import com.example.radiomodern.databinding.ItemQuotationBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuotationListAdapter(
    private val onClick: (Quotation) -> Unit,
    private val onLongClick: (Quotation) -> Unit
) : ListAdapter<Quotation, QuotationListAdapter.QuotationViewHolder>(QuotationDiffCallback()) {

    private val currencyFormat = DecimalFormat("#,##0.00")
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuotationViewHolder {
        val binding = ItemQuotationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return QuotationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuotationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuotationViewHolder(
        private val binding: ItemQuotationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(quotation: Quotation) {
            binding.tvPrice.text = "$${currencyFormat.format(quotation.price)}"
            binding.tvDate.text = dateTimeFormat.format(Date(quotation.createdAt))
            binding.tvDescription.text = quotation.description

            if (!quotation.photoUri.isNullOrBlank()) {
                binding.ivThumbnail.visibility = View.VISIBLE
                try {
                    val uri = Uri.parse(quotation.photoUri)
                    val context = binding.root.context
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                    binding.ivThumbnail.setImageBitmap(bitmap)
                } catch (_: Exception) {
                    binding.ivThumbnail.visibility = View.GONE
                }
            } else {
                binding.ivThumbnail.visibility = View.GONE
            }

            binding.card.setOnClickListener { onClick(quotation) }
            binding.card.setOnLongClickListener {
                onLongClick(quotation)
                true
            }
        }
    }

    class QuotationDiffCallback : DiffUtil.ItemCallback<Quotation>() {
        override fun areItemsTheSame(oldItem: Quotation, newItem: Quotation) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Quotation, newItem: Quotation) =
            oldItem == newItem
    }
}
