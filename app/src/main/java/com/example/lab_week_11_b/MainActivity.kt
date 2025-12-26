package com.example.lab_week_11_b

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 3
        private const val REQUEST_CAMERA = 4
        private const val REQUEST_MEDIA_PERMISSIONS = 5
    }

    private lateinit var providerFileManager: ProviderFileManager
    private var photoInfo: FileInfo? = null
    private var videoInfo: FileInfo? = null
    private var isCapturingVideo = false

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            providerFileManager.insertImageToStore(photoInfo)
            Toast.makeText(this, "Photo saved to gallery", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    private val takeVideoLauncher = registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        if (success) {
            providerFileManager.insertVideoToStore(videoInfo)
            Toast.makeText(this, "Video saved to gallery", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to capture video", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            checkStorageAndMediaPermissions {
                if (isCapturingVideo) {
                    openVideoCapture()
                } else {
                    openImageCapture()
                }
            }
        } else {
            Toast.makeText(this, "Camera permission is required to take photos/videos", Toast.LENGTH_LONG).show()
        }
    }

    private val requestMediaPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            if (isCapturingVideo) {
                openVideoCapture()
            } else {
                openImageCapture()
            }
        } else {
            Toast.makeText(this, "Media permissions are required to save photos/videos", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        providerFileManager = ProviderFileManager(
            applicationContext,
            FileHelper(applicationContext),
            contentResolver,
            Executors.newSingleThreadExecutor(),
            MediaContentHelper()
        )

        findViewById<Button>(R.id.photo_button).setOnClickListener {
            isCapturingVideo = false
            checkCameraPermission()
        }

        findViewById<Button>(R.id.video_button).setOnClickListener {
            isCapturingVideo = true
            checkCameraPermission()
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkStorageAndMediaPermissions {
                    if (isCapturingVideo) {
                        openVideoCapture()
                    } else {
                        openImageCapture()
                    }
                }
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkStorageAndMediaPermissions(onPermissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionsToRequest = mutableListOf<String>()

            if (isCapturingVideo) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }

            if (permissionsToRequest.isNotEmpty()) {
                requestMediaPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
            } else {
                onPermissionGranted()
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            when (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )) {
                PackageManager.PERMISSION_GRANTED -> {
                    onPermissionGranted()
                }
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
            }
        } else {
            onPermissionGranted()
        }
    }

    private fun openImageCapture() {
        photoInfo = providerFileManager.generatePhotoUri(System.currentTimeMillis())
        val uri = photoInfo?.uri
        if (uri != null) {
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(this, "Failed to create photo file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openVideoCapture() {
        videoInfo = providerFileManager.generateVideoUri(System.currentTimeMillis())
        val uri = videoInfo?.uri
        if (uri != null) {
            takeVideoLauncher.launch(uri)
        } else {
            Toast.makeText(this, "Failed to create video file", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (isCapturingVideo) {
                        openVideoCapture()
                    } else {
                        openImageCapture()
                    }
                } else {
                    Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}