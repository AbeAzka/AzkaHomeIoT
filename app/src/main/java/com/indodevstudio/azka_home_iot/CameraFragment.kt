package com.indodevstudio.azka_home_iot

import ApiService
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
/*private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"*/

class CameraFragment: Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: ImageButton
    private lateinit var flashButton: ImageButton
    private lateinit var closeButton: ImageButton
    private lateinit var galleryButton: ImageButton
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var isFlashOn = false // Status flash
    private val api = Server.instance.create(ApiService::class.java)
    val REQUEST_IMAGE_CAPTURE = 100
    private lateinit var focusOverlay: View

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val file = getFileFromUri(uri)
                if (file != null) {
                    sendImageToFinansial(file)

                    // Konversi file ke bitmap dan upload ke server
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    uploadPhoto(bitmap)

                } else {
                    Log.e("CameraFragment", "Gagal mengonversi URI ke File")
                }
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        Server.setToken("2025A")
        focusOverlay = view.findViewById(R.id.focusOverlay) // Overlay Fokus
        previewView = view.findViewById(R.id.previewView)
        captureButton = view.findViewById(R.id.captureButton)
        flashButton = view.findViewById(R.id.flashButton)
        galleryButton = view.findViewById(R.id.galleryButton)
        /*closeButton = view.findViewById(R.id.closeButton)*/
        captureButton.setOnClickListener { takePhoto() }
        flashButton.setOnClickListener { toggleFlash() } // Toggle flash
        galleryButton.setOnClickListener { openGallery() } // Pilih gambar dari galeri

       /* closeButton.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }*/

        (activity as? AppCompatActivity)?.supportActionBar?.hide()

        val toolbar = view.findViewById<Toolbar>(R.id.toolbarSetup)
        toolbar.setNavigationOnClickListener(View.OnClickListener {
            //What to do on back clicked
            requireActivity().supportFragmentManager.popBackStack()
        })

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        return view
    }

    fun onBackPressed() {

        // Menggunakan parentFragmentManager untuk mengganti fragmen di dalam aktivitas
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DeviceListFragment()) // Gantilah dengan fragmen yang sesuai
            .commit()
    }



   /* override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }*/

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            imageCapture = ImageCapture.Builder()
                .setFlashMode(if (isFlashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e("CameraX", "Gagal memulai kamera", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val file = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "photo_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(requireContext(), "Foto berhasil", Toast.LENGTH_SHORT).show()
                    //sendImageToFinansial(file)
                    val bitmap = BitmapFactory.decodeFile(file.toString())
                    //imgThumbnail.setImageBitmap(bitmap)
                    uploadPhoto(bitmap) // Upload ke server
                   /* val bitmap = BitmapFactory.decodeFile(file.toString())
                    uploadPhoto(bitmap)*/
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Gagal menyimpan foto: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imagePath = data?.getStringExtra("image_path")
            if (imagePath != null) {

                val bitmap = BitmapFactory.decodeFile(imagePath)
                //imgThumbnail.setImageBitmap(bitmap)
                uploadPhoto(bitmap) // Upload ke server
            }
        }
    }


    private var flashMode = 2 // 0 = OFF, 1 = ON, 2 = AUTO

    private fun toggleFlash() {
        flashMode = (flashMode + 1) % 3 // Ganti mode secara berurutan: OFF -> ON -> AUTO

        when (flashMode) {
            0 -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
                flashButton.setImageResource(R.drawable.flash_off_black)
                Toast.makeText(requireContext(), "Flash OFF", Toast.LENGTH_SHORT).show()
            }
            1 -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_ON
                flashButton.setImageResource(R.drawable.flash_on_black)
                Toast.makeText(requireContext(), "Flash ON", Toast.LENGTH_SHORT).show()
            }
            2 -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_AUTO
                flashButton.setImageResource(R.drawable.flash_auto_black) // Ganti dengan ikon flash auto
                Toast.makeText(requireContext(), "Flash AUTO", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun sendImageToFinansial(file: File) {
        val resultIntent = Intent().apply {
            putExtra("image_path", file.absolutePath)
        }
        requireActivity().setResult(Activity.RESULT_OK, resultIntent)
    }

    private fun getFileFromUri(uri: Uri): File? {
        val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
        val tempFile = File(requireContext().cacheDir, "temp_image.jpg")

        tempFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }

        return tempFile
    }

    private fun compressBitmap(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream) // Coba kualitas 80 agar lebih ringan
        return stream.toByteArray()
    }



    private fun uploadPhoto(bitmap: Bitmap) {
        val compressedImage = compressBitmap(bitmap)
        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), compressedImage)
        val photoPart = MultipartBody.Part.createFormData("photo", "bill.jpg", requestBody)

        api.uploadPhoto2(photoPart).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Photo uploaded!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("Upload", "Error: ${response.message()} - ${response.code()}")
                    Toast.makeText(requireContext(), "Upload Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                Log.e("Upload", "Upload failed: ${t.message}", t)
                Toast.makeText(requireContext(), "Upload failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }
}

