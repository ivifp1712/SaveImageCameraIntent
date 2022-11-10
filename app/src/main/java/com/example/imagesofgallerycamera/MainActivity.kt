package com.example.imagesofgallerycamera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    var frame: ImageView? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        frame = findViewById(R.id.imageView)
        var textView2: TextView = findViewById(R.id.textView2)
        textView2.text = "Long press to select image and short press to capture image"

        //TODO ask for permission of camera upon first launch of application
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {
                val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                requestPermissions(permission, 112)
            }
        }


        //TODO captue image using camera
        frame?.setOnLongClickListener(OnLongClickListener {
            println("Long press")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED) {
                    val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission, 121)
                } else {
                    openCamera()
                }
            } else {
                openCamera()
            }
            true
        })


        //TODO chose image from gallery
        frame?.setOnClickListener(View.OnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE)
        })



    }

    var image_uri: Uri? = null
    private val RESULT_LOAD_IMAGE = 123
    val IMAGE_CAPTURE_CODE = 654

    // cambiar texto textview2

    private fun saveImg(bitmap: Bitmap) {
        val file = getDisc()
        //val file = File(Environment.DIRECTORY_PICTURES, "Imagenes favoritas")
        if (!file.exists() && !file.mkdirs()) {
            file.mkdir()
        }

        val milisegundos = System.currentTimeMillis()
        val name = "foto_$milisegundos.jpg"
        val fileName = file.absolutePath + "/" + name
        val newFile = File(fileName)
        // fileName path
        println("fileName path: $fileName")

        try {
            val fileOutPutStream = FileOutputStream(newFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutPutStream)
            Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()
            var savedFile = newFile
            fileOutPutStream.flush()
            fileOutPutStream.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getDisc(): File {
        val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(file, "Imagenes favoritas")
    }


    //TODO opens camera so that user can capture image
    private fun openCamera() {
        println("Open camera")
        val values = ContentValues()
        // String milisegundos hora actual
        val milisegundos = System.currentTimeMillis()
        values.put(MediaStore.Images.Media.TITLE, "foto_$milisegundos.jpg")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Desde la camaritaaaa")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            //imageView.setImageURI(image_uri);
            val bitmap = uriToBitmap(image_uri!!)
            println("Bitmap: ${bitmap}")
            frame?.setImageBitmap(bitmap)
            if (bitmap != null) {
                saveImg(bitmap)
            }

        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            image_uri = data.data
            //imageView.setImageURI(image_uri);
            val bitmap = uriToBitmap(image_uri!!)
            frame?.setImageBitmap(bitmap)
        }

    }

    //TODO takes URI of the image and returns bitmap
    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}