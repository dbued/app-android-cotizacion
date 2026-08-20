package com.example.radiomodern.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.radiomodern.R
import com.example.radiomodern.data.database.AppDatabase
import com.example.radiomodern.data.repository.QuotationRepository
import com.example.radiomodern.databinding.FragmentSettingsBinding
import com.example.radiomodern.util.CsvExporter
import com.example.radiomodern.util.ExcelExporter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy {
        val dao = AppDatabase.getDatabase(requireContext().applicationContext).quotationDao()
        QuotationRepository(dao)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDarkModeSwitch()
        setupExportCards()
    }

    private fun setupDarkModeSwitch() {
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", true)
        binding.switchDarkMode.isChecked = isDark

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    private fun setupExportCards() {
        binding.cardExportExcel.setOnClickListener { exportExcel() }
        binding.cardExportCsv.setOnClickListener { exportCsv() }
    }

    private fun exportExcel() {
        viewLifecycleOwner.lifecycleScope.launch {
            val quotations = repository.allQuotations.first()
            if (quotations.isEmpty()) {
                Snackbar.make(binding.root, R.string.snackbar_no_data, Snackbar.LENGTH_SHORT).show()
                return@launch
            }
            val uri = ExcelExporter.exportToExcel(requireContext(), quotations)
            if (uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.ms-excel"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Compartir Excel"))
                Snackbar.make(binding.root, R.string.snackbar_exported_excel, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportCsv() {
        viewLifecycleOwner.lifecycleScope.launch {
            val quotations = repository.allQuotations.first()
            if (quotations.isEmpty()) {
                Snackbar.make(binding.root, R.string.snackbar_no_data, Snackbar.LENGTH_SHORT).show()
                return@launch
            }
            val uri = CsvExporter.exportToCsv(requireContext(), quotations)
            if (uri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Compartir CSV"))
                Snackbar.make(binding.root, R.string.snackbar_exported_csv, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
