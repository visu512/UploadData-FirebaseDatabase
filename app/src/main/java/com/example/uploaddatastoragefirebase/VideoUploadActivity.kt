//
//package com.example.uploaddatastoragefirebase
//
//import android.app.Activity
//import android.app.ProgressDialog
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.webkit.MimeTypeMap
//import android.widget.MediaController
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.isVisible
//import com.example.uploaddatastoragefirebase.databinding.ActivityVideoUploadBinding
//import com.google.firebase.database.ktx.database
//import com.google.firebase.ktx.Firebase
//import com.google.firebase.storage.ktx.storage
//
//class VideoUploadActivity : AppCompatActivity() {
//    private val binding: ActivityVideoUploadBinding by lazy {
//        ActivityVideoUploadBinding.inflate(layoutInflater)
//    }
//
//    private lateinit var progressDialog: ProgressDialog
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//
//        // Initialize the ProgressDialog
//        progressDialog = ProgressDialog(this).apply {
//            setCancelable(false)
//        }
//
//        binding.videoView.isVisible = false
//
//        binding.button.setOnClickListener {
//            val intent = Intent()
//            intent.action = Intent.ACTION_PICK // pick (choose action)
//            intent.type = "video/*" // upload single video
//            videoLauncher.launch(intent)
//        }
//    }
//
//    private val videoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
//
//            // Show progress dialog
//            progressDialog.setTitle("Video uploading...")
//            progressDialog.show()
//
//            // Get video URI
//            val videoUri = it.data!!.data
//            val ref = Firebase.storage.reference.child("Video/${System.currentTimeMillis()}.${getFileType(videoUri)}")
//
//            // Upload video
//            ref.putFile(videoUri!!).addOnSuccessListener {
//                // After successful upload, dismiss the progress dialog
//                progressDialog.dismiss()
//                ref.downloadUrl.addOnSuccessListener { uri ->
//                    Firebase.database.reference.child("Video").push()
//                        .setValue(uri.toString())
//
//                    Toast.makeText(this, "Video uploaded Successful!", Toast.LENGTH_SHORT).show()
//                }
//            }.addOnFailureListener {
//                // Dismiss the progress dialog and show error message
//                progressDialog.dismiss()
//                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
//            }.addOnProgressListener { taskSnapshot ->
//                // Calculate the progress percentage
//                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
//                progressDialog.setMessage("Upload $progress %")
//
//            }
//
//            // Set up video view
//            binding.button.isVisible = false // Hide button when video is selected
//            binding.videoView.isVisible = true // Now be visible
//
//            val mediaController = MediaController(this)
//            mediaController.setAnchorView(binding.videoView)
//
//            binding.videoView.setVideoURI(videoUri) // Get video from file
//            binding.videoView.setMediaController(mediaController)
//            binding.videoView.start() // Start video
//        }
//    }
//
//    // Function to get the file type from URI
//    private fun getFileType(data: Uri?): String {
//        val r = contentResolver
//        val mimeTypeMap = MimeTypeMap.getSingleton()
//        return mimeTypeMap.getExtensionFromMimeType(r.getType(data!!)) ?: "mp4"
//    }
//}


package com.example.uploaddatastoragefirebase

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.uploaddatastoragefirebase.databinding.ActivityVideoUploadBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class VideoUploadActivity : AppCompatActivity() {
    private val binding: ActivityVideoUploadBinding by lazy {
        ActivityVideoUploadBinding.inflate(layoutInflater)
    }

    private lateinit var progressDialog: ProgressDialog
    private var videoUrl: String? = null // Store the uploaded video URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize the ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setCancelable(false)
        }

        binding.videoView.isVisible = false

        binding.button.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK // pick (choose action)
            intent.type = "video/*" // upload single video
            videoLauncher.launch(intent)
        }

        // Set click listener for delete button
        binding.deleteButton.setOnClickListener {
            videoUrl?.let {
                deleteVideo(it)
            } ?: Toast.makeText(this, "No video to delete", Toast.LENGTH_SHORT).show()
        }
    }

    private val videoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {

            // Show progress dialog
            progressDialog.setTitle("Video uploading...")
            progressDialog.show()

            // Get video URI
            val videoUri = it.data!!.data
            val ref = Firebase.storage.reference.child("Video/${System.currentTimeMillis()}.${getFileType(videoUri)}")

            // Upload video
            ref.putFile(videoUri!!).addOnSuccessListener {
                // After successful upload, dismiss the progress dialog
                progressDialog.dismiss()
                ref.downloadUrl.addOnSuccessListener { uri ->
                    videoUrl = uri.toString() // Store the video URL
                    Firebase.database.reference.child("Video").push()
                        .setValue(videoUrl)

                    Toast.makeText(this, "Video uploaded Successfully!", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                // Dismiss the progress dialog and show error message
                progressDialog.dismiss()
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }.addOnProgressListener { taskSnapshot ->
                // Calculate the progress percentage
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                progressDialog.setMessage("Upload $progress %")
            }

            // Set up video view
            binding.button.isVisible = false // Hide button when video is selected
            binding.videoView.isVisible = true // Now be visible

            val mediaController = MediaController(this)
            mediaController.setAnchorView(binding.videoView)

            binding.videoView.setVideoURI(videoUri) // Get video from file
            binding.videoView.setMediaController(mediaController)
            binding.videoView.start() // Start video

            // Show the delete button
            binding.deleteButton.isVisible = true
        }
    }

    // Function to delete the video
    private fun deleteVideo(videoUrl: String) {
        val ref = Firebase.storage.getReferenceFromUrl(videoUrl)
        ref.delete().addOnSuccessListener {
            Toast.makeText(this, "Video deleted successfully!", Toast.LENGTH_SHORT).show()
            binding.deleteButton.isVisible = false // Hide delete button after deletion
            binding.videoView.isVisible = false // Hide video view after deletion
            this.videoUrl = null // Clear the stored URL
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to delete video", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to get the file type from URI
    private fun getFileType(data: Uri?): String {
        val r = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(r.getType(data!!)) ?: "mp4"
    }
}
