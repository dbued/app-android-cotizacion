package com.example.radiomodern.ui.add

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.radiomodern.R
import com.example.radiomodern.databinding.FragmentAddQuotationBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddQuotationFragment : Fragment() {

    private var _binding: FragmentAddQuotationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddQuotationViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private var photoFile: File? = null
    private var photoUri: Uri? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showCameraPreview()
        } else {
            Snackbar.make(binding.root, R.string.camera_permission_required, Snackbar.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            viewModel.updatePhotoUri(photoUri.toString())
            binding.ivPhotoPreview.setImageURI(photoUri)
            binding.ivPhotoPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddQuotationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val quotationId = arguments?.getLong("quotationId", -1L) ?: -1L
        if (quotationId > 0) {
            viewModel.loadQuotation(quotationId)
            binding.toolbar.title = getString(R.string.edit_title)
            binding.fabSave.text = getString(R.string.btn_update)
        }

        setupToolbar()
        setupForm()
        setupPhotoButton()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupForm() {
        binding.etPrice.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validatePrice()
        }
        binding.etDescription.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) validateDescription()
        }
        binding.fabSave.setOnClickListener {
            if (validateAll()) {
                viewModel.save()
            }
        }
    }

    private fun setupPhotoButton() {
        binding.btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                showCameraPreview()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showCameraPreview() {
        val context = requireContext()

        photoFile = File(
            context.filesDir.resolve("photos").also { it.mkdirs() },
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        )

        photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile!!
        )

        takePictureLauncher.launch(photoUri!!)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.isSaved) {
                        val message = if (state.isEditing) R.string.snackbar_updated else R.string.snackbar_saved
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                        return@collect
                    }

                    if (binding.etPrice.text.isNullOrEmpty() && state.price.isNotEmpty()) {
                        binding.etPrice.setText(state.price)
                    }
                    if (binding.etDescription.text.isNullOrEmpty() && state.description.isNotEmpty()) {
                        binding.etDescription.setText(state.description)
                    }
                    if (state.photoUri != null) {
                        binding.ivPhotoPreview.setImageURI(Uri.parse(state.photoUri))
                        binding.ivPhotoPreview.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun validatePrice(): Boolean {
        val text = binding.etPrice.text?.toString() ?: ""
        val value = text.toDoubleOrNull()
        return if (text.isBlank()) {
            binding.tilPrice.error = getString(R.string.error_price_required)
            false
        } else if (value == null || value <= 0) {
            binding.tilPrice.error = getString(R.string.error_price_invalid)
            false
        } else {
            binding.tilPrice.error = null
            viewModel.updatePrice(text)
            true
        }
    }

    private fun validateDescription(): Boolean {
        val text = binding.etDescription.text?.toString() ?: ""
        return if (text.isBlank()) {
            binding.tilDescription.error = getString(R.string.error_description_required)
            false
        } else {
            binding.tilDescription.error = null
            viewModel.updateDescription(text)
            true
        }
    }

    private fun validateAll(): Boolean {
        viewModel.updatePrice(binding.etPrice.text?.toString() ?: "")
        viewModel.updateDescription(binding.etDescription.text?.toString() ?: "")
        return validatePrice() && validateDescription()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
