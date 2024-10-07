package com.example.uploaddatastoragefirebase

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.uploaddatastoragefirebase.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Go to video view Activity
        binding.GotoVideoView.setOnClickListener {
            startActivity(Intent(this, VideoUploadActivity::class.java))
        }

        // Upload image button
        binding.uploadImageBtn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_PICK // pick (choose action)
            intent.type = "image/*" // upload single image
            imageLauncher.launch(intent)
        }
    }

    // Activity result launcher
    val imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            if (it.data != null && it.data!!.data != null) {
                val imageUri = it.data!!.data

                // Firebase storage reference with unique file name
                val ref = Firebase.storage.reference.child("Photos/"+System.currentTimeMillis()+"" + ""+getFileType(it.data!!.data))


                binding.imageView.isVisible = true

                ref.putFile(imageUri!!).addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        // Get image URL and set image to ImageView using Picasso
                        Picasso.get().load(uri.toString()).into(binding.imageView)
                        Toast.makeText(this, "Profile Picture Updated..", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to get the file type from URI
    private fun getFileType(data: Uri?): String {
        val r = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(r.getType(data!!)) ?: "jpg"
    }
}
