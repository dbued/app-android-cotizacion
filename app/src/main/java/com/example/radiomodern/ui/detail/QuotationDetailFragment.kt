package com.example.radiomodern.ui.detail

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.radiomodern.R
import com.example.radiomodern.databinding.FragmentQuotationDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QuotationDetailFragment : Fragment() {

    private var _binding: FragmentQuotationDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailQuotationViewModel by viewModels()

    private val currencyFormat = DecimalFormat("#,##0.00")
    private val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, HH:mm", Locale("es"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuotationDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val quotationId = arguments?.getLong("quotationId", -1L) ?: -1L
        if (quotationId <= 0) {
            findNavController().navigateUp()
            return
        }

        setupToolbar()
        setupButtons()
        observeQuotation(quotationId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupButtons() {
        binding.btnDelete.setOnClickListener {
            viewModel.quotation.value?.let { quotation ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.dialog_delete_title)
                    .setMessage(R.string.dialog_delete_message)
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .setPositiveButton(R.string.dialog_confirm_delete) { _, _ ->
                        viewModel.delete(quotation)
                    }
                    .show()
            }
        }

        binding.btnEdit.setOnClickListener {
            viewModel.quotation.value?.let { quotation ->
                val bundle = Bundle().apply { putLong("quotationId", quotation.id) }
                findNavController().navigate(R.id.AddQuotationFragment, bundle)
            }
        }

        binding.btnShare.setOnClickListener {
            viewModel.quotation.value?.let { quotation -> shareQuotation(quotation) }
        }
    }

    private fun observeQuotation(quotationId: Long) {
        viewModel.loadQuotation(quotationId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.quotation.collect { quotation ->
                        quotation?.let { bindQuotation(it) }
                    }
                }
                launch {
                    viewModel.isDeleted.collect { deleted ->
                        if (deleted) {
                            Snackbar.make(binding.root, R.string.snackbar_deleted, Snackbar.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

    private fun bindQuotation(q: com.example.radiomodern.data.model.Quotation) {
        binding.tvDetailPrice.text = "$${currencyFormat.format(q.price)}"
        binding.tvDetailDescription.text = q.description
        binding.tvDetailDate.text = dateFormat.format(Date(q.createdAt))

        if (!q.photoUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(q.photoUri)
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                }
                binding.ivHeroImage.setImageBitmap(bitmap)
                binding.ivHeroImage.visibility = View.VISIBLE
            } catch (_: Exception) {
                binding.ivHeroImage.visibility = View.GONE
            }
        } else {
            binding.ivHeroImage.visibility = View.GONE
        }
    }

    private fun shareQuotation(q: com.example.radiomodern.data.model.Quotation) {
        val text = buildString {
            appendLine("Cotización")
            appendLine("Precio: $${currencyFormat.format(q.price)}")
            appendLine("Descripción: ${q.description}")
            appendLine("Fecha: ${dateFormat.format(Date(q.createdAt))}")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            if (!q.photoUri.isNullOrBlank()) {
                putExtra(Intent.EXTRA_STREAM, Uri.parse(q.photoUri))
                type = "image/*"
            }
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
