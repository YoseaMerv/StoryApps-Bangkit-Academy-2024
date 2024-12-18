package com.yosea.kirimstory.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.yosea.kirimstory.R
import com.yosea.kirimstory.api.AddStoryResponse
import com.yosea.kirimstory.api.RetrofitClient
import com.yosea.kirimstory.helper.StoryRepository
import com.yosea.kirimstory.viewmodel.StoryViewModel
import com.yosea.kirimstory.viewmodel.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.media.ExifInterface
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AddStoryFragment : Fragment() {
    private lateinit var storyImageView: ImageView
    private lateinit var cameraButton: Button
    private lateinit var galleryButton: Button
    private lateinit var descriptionEditText: EditText
    private lateinit var uploadButton: Button
    private lateinit var locationSwitch: Switch

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_GALLERY_IMAGE = 2
    private val REQUEST_LOCATION_PERMISSION = 3
    private var currentPhotoPath: String? = null
    private var imageUri: Uri? = null
    private var currentLocation: Location? = null
    private var extractedLat: Float? = null
    private var extractedLon: Float? = null
    private lateinit var storyRepository: StoryRepository
    private val viewModel: StoryViewModel by viewModels {
        ViewModelFactory(StoryRepository(RetrofitClient.instance))
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_story, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storyImageView = view.findViewById(R.id.storyImageView)
        cameraButton = view.findViewById(R.id.cameraButton)
        galleryButton = view.findViewById(R.id.galleryButton)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        uploadButton = view.findViewById(R.id.uploadButton)
        locationSwitch = view.findViewById(R.id.locationSwitch)

        storyRepository = StoryRepository(RetrofitClient.instance)
        cameraButton.setOnClickListener { openCamera() }
        galleryButton.setOnClickListener { openGallery() }
        uploadButton.setOnClickListener { validateAndUploadStory() }
        locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getLocation()
            }
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        // Menggunakan FusedLocationProviderClient untuk mendapatkan lokasi yang lebih akurat
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Lokasi ditemukan
                currentLocation = location
            } else {
                // Lokasi tidak ditemukan
                Log.e("Location", "Lokasi tidak ditemukan")
            }
        }
    }

    private fun validateAndUploadStory() {
        val description = descriptionEditText.text.toString()
        if (description.isEmpty() || imageUri == null) {
            Toast.makeText(
                requireContext(),
                "Description and image are required",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val token = requireContext().getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                "Token not found. Please login again.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val latBody = if (locationSwitch.isChecked && currentLocation != null) {
            currentLocation?.latitude?.toFloat()
        } else {
            extractedLat
        }

        val lonBody = if (locationSwitch.isChecked && currentLocation != null) {
            currentLocation?.longitude?.toFloat()
        } else {
            extractedLon
        }

        uploadStory(token, description, latBody, lonBody)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_IMAGE_CAPTURE
            )
            return
        }

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }

            photoFile?.let {
                imageUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        } else {
            Toast.makeText(requireContext(), "No camera app available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGE_CAPTURE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation()
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE)
    }

    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            .apply { currentPhotoPath = absolutePath }
    }

    private fun compressImage(bitmap: Bitmap): File? {
        val compressedFile = File(requireContext().cacheDir, "compressed_image.jpg")
        return try {
            compressedFile.createNewFile()
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bitmapData = outputStream.toByteArray()

            FileOutputStream(compressedFile).use {
                it.write(bitmapData)
                it.flush()
            }
            compressedFile
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    if (currentPhotoPath != null && File(currentPhotoPath!!).exists()) {
                        imageUri?.let { loadAndCompressImage(it) }
                    } else {
                        imageUri = null
                        currentPhotoPath = null
                        Toast.makeText(requireContext(), "No photo captured", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                REQUEST_GALLERY_IMAGE -> {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        Log.d("onActivityResult", "Selected gallery image URI: $selectedImageUri")
                        imageUri = selectedImageUri

                        // Extract and compress the image
                        loadAndCompressImage(selectedImageUri)
                    } else {
                        Log.e("onActivityResult", "Failed to pick image from gallery.")
                        Toast.makeText(
                            requireContext(),
                            "Failed to pick image from gallery",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imageUri = null
                currentPhotoPath = null
                Toast.makeText(requireContext(), "Photo capture canceled", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun extractExifData(uri: Uri) {
        try {
            val filePath = getRealPathFromUri(uri)
            if (filePath != null) {
                val exif = ExifInterface(filePath)
                val latLongArray = FloatArray(2)
                if (exif.getLatLong(latLongArray)) {
                    extractedLat = latLongArray[0]
                    extractedLon = latLongArray[1]
                    Log.d("AddStoryFragment", "Extracted Lat: $extractedLat, Lon: $extractedLon")
                } else {
                    Log.d("AddStoryFragment", "No LatLong data found in EXIF")
                }
            } else {
                Log.d("AddStoryFragment", "Invalid file path from URI")
            }
        } catch (e: IOException) {
            Log.e("AddStoryFragment", "Error reading EXIF data: ${e.message}")
        }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        return if (uri.scheme == "content") {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            requireContext().contentResolver.query(uri, projection, null, null, null)
                ?.use { cursor ->
                    val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    cursor.moveToFirst()
                    cursor.getString(columnIndex)
                }
        } else {
            uri.path
        }
    }

    private fun loadAndCompressImage(uri: Uri) {
        Glide.with(this).load(uri).into(storyImageView)
        val bitmap = getBitmapFromUri(uri)
        bitmap?.let { bmp ->
            compressImage(bmp)?.let { compressedFile ->
                currentPhotoPath = compressedFile.absolutePath
            }
        }
    }

    private fun uploadStory(
        token: String,
        description: String,
        lat: Float? = null,
        lon: Float? = null
    ) {
        Log.d("AddStoryFragment", "Uploading story with lat: $lat and lon: $lon")
        val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val imageFile = currentPhotoPath?.let { File(it) } ?: run {
            Toast.makeText(requireContext(), "Image not available", Toast.LENGTH_SHORT).show()
            return
        }

        val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageBody = MultipartBody.Part.createFormData("photo", imageFile.name, requestFile)

        val latBody = lat?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val lonBody = lon?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

        Log.d("AddStoryFragment", "Uploading with description: $description, Lat: $lat, Lon: $lon")

        val call = storyRepository.uploadStory(
            "Bearer $token",
            descriptionBody,
            imageBody,
            latBody,
            lonBody
        )
        call.enqueue(object : Callback<AddStoryResponse> {
            override fun onResponse(
                call: Call<AddStoryResponse>,
                response: Response<AddStoryResponse>
            ) {
                if (response.isSuccessful) {
                    viewModel.refreshStories()
                    Toast.makeText(
                        requireContext(),
                        "Story uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                } else {
                    Log.e("Upload Story", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(requireContext(), "Failed to upload story", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Failed to upload story: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    }
}

