package com.example.paint

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.paint.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val path = File("${Environment.getExternalStorageDirectory().path}/Download")
    private var filename = ""

    private val launchGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when(it.resultCode) {
            RESULT_OK -> {
                getBitmapFromData(it.data)
            }
        }
    }

    private fun getBitmapFromData(data: Intent?) {
        try {
            binding.imageDrawing.setImageFromUri(data?.data, contentResolver)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        setFilename()
    }

    private fun setListeners() {
        binding.addImageButton.setOnClickListener {
            launchGallery.launch(
                Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            )
        }

        binding.redoImageButton.setOnClickListener {
            binding.imageDrawing.redo()
        }

        binding.undoImageButton.setOnClickListener {
            binding.imageDrawing.undo()
        }

        binding.saveImageButton.setOnClickListener {
            saveImage()
        }
    }

    private fun setFilename() {
        val formatDate = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentDate = formatDate.format(Date())
        filename = "$path/$currentDate.png"
    }

    private fun saveImage() {
        val file = File(filename)
        val bitmap = binding.imageDrawing.getBitmap()
        val arrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, arrayOutputStream)
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.apply {
            write(arrayOutputStream.toByteArray())
            flush()
            close()
        }
        Toast.makeText(this, "Image is saved", Toast.LENGTH_SHORT).show()
    }
}