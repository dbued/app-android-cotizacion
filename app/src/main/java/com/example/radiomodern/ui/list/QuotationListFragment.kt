package com.example.radiomodern.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.radiomodern.R
import com.example.radiomodern.data.model.Quotation
import com.example.radiomodern.databinding.FragmentQuotationListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class QuotationListFragment : Fragment() {

    private var _binding: FragmentQuotationListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuotationListViewModel by viewModels()
    private lateinit var adapter: QuotationListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuotationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeQuotations()
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_main)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_export_csv -> {
                    exportCsv()
                    true
                }
                R.id.action_export_excel -> {
                    exportExcel()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = QuotationListAdapter(
            onClick = { quotation ->
                val bundle = Bundle().apply { putLong("quotationId", quotation.id) }
                findNavController().navigate(R.id.QuotationDetailFragment, bundle)
            },
            onLongClick = { quotation ->
                showDeleteDialog(quotation)
            }
        )
        binding.recyclerView.adapter = adapter

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val quotation = adapter.currentList[position]
                viewModel.delete(quotation)
                Snackbar.make(binding.root, R.string.snackbar_deleted, Snackbar.LENGTH_LONG)
                    .setAction(R.string.snackbar_undo) {
                        viewModel.insert(quotation)
                    }
                    .show()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyclerView)
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_list_to_add)
        }
    }

    private fun observeQuotations() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allQuotations.collect { quotations ->
                    adapter.submitList(quotations)
                    binding.emptyState.visibility = if (quotations.isEmpty()) View.VISIBLE else View.GONE
                    binding.recyclerView.visibility = if (quotations.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun showDeleteDialog(quotation: Quotation) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_title)
            .setMessage(R.string.dialog_delete_message)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_confirm_delete) { _, _ ->
                viewModel.delete(quotation)
                Snackbar.make(binding.root, R.string.snackbar_deleted, Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun exportCsv() {
        val quotations = viewModel.allQuotations.value
        if (quotations.isEmpty()) {
            Snackbar.make(binding.root, R.string.snackbar_no_data, Snackbar.LENGTH_SHORT).show()
            return
        }
        val uri = com.example.radiomodern.util.CsvExporter.exportToCsv(requireContext(), quotations)
        if (uri != null) {
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "Compartir CSV"))
            Snackbar.make(binding.root, R.string.snackbar_exported_csv, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun exportExcel() {
        val quotations = viewModel.allQuotations.value
        if (quotations.isEmpty()) {
            Snackbar.make(binding.root, R.string.snackbar_no_data, Snackbar.LENGTH_SHORT).show()
            return
        }
        val uri = com.example.radiomodern.util.ExcelExporter.exportToExcel(requireContext(), quotations)
        if (uri != null) {
            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/vnd.ms-excel"
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(android.content.Intent.createChooser(shareIntent, "Compartir Excel"))
            Snackbar.make(binding.root, R.string.snackbar_exported_excel, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
