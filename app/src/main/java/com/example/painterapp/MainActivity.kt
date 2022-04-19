package com.example.painterapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get

class MainActivity : AppCompatActivity() {

    private val DEFAULT_BRUSH_SIZE: Float = 20.toFloat()
    private val GRANTED_READ_STORAGE_PERMISSION: String =
        "Permission to read from storage was granted."
    private val DENIED_READ_STORAGE_PERMISSION: String =
        "Permission to read from storage was denied."
    private val DIALOG_DEFAULT_TITLE: String = "Painter App"
    private val STORAGE_ACCESS_RATIONALE: String =
        "Your permission to access image storage is required"

    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageBackground: ImageView = findViewById(R.id.ivBackground)
                imageBackground.setImageURI(result.data?.data)
            }
        }

    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    val intent: Intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    this.openGalleryLauncher.launch(intent)
                } else {
                    Toast.makeText(this, DENIED_READ_STORAGE_PERMISSION, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    private var drawingView: DrawingView? = null
    private var brushSizeButton: ImageButton? = null
    private var linearLayoutColorPallet: LinearLayout? = null
    private var currentBrushColorImageButton: ImageButton? = null
    private var addImageButton: ImageButton? = null
    private var undoButton: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.drawingView = findViewById(R.id.dvMain)
        this.drawingView?.setBrushSize(DEFAULT_BRUSH_SIZE)

        this.brushSizeButton = findViewById(R.id.ibBrushSize)
        this.brushSizeButton?.setOnClickListener {
            onSizeSelect()
        }

        this.linearLayoutColorPallet = findViewById(R.id.llColorPallet)
        this.currentBrushColorImageButton = this.linearLayoutColorPallet!![0] as ImageButton
        this.currentBrushColorImageButton?.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.color_pallet_pressed)
        )

        this.addImageButton = findViewById(R.id.ibAddImage)
        this.addImageButton?.setOnClickListener {
            requestStoragePermission()
        }

        this.undoButton = findViewById(R.id.ibUndo)
        this.undoButton?.setOnClickListener {
            this.drawingView?.undo()
        }
    }

    private fun onSizeSelect() {
        val brushDialog: Dialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        val smallButton = brushDialog.findViewById<ImageButton>(R.id.ib_smallBrush)
        smallButton.setOnClickListener {
            this.drawingView?.setBrushSize(10.toFloat())
            brushDialog.dismiss()
        }
        val mediumButton = brushDialog.findViewById<ImageButton>(R.id.ib_mediumBrush)
        mediumButton.setOnClickListener {
            this.drawingView?.setBrushSize(20.toFloat())
            brushDialog.dismiss()
        }
        val largeButton = brushDialog.findViewById<ImageButton>(R.id.ib_largeBrush)
        largeButton.setOnClickListener {
            this.drawingView?.setBrushSize(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    fun onColorSelect(view: View) {
        if (view == this.currentBrushColorImageButton)
            return
        val imageButton = view as ImageButton
        val colorTag: String = imageButton.tag.toString()
        this.drawingView?.setBrushColor(colorTag)

        imageButton.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.color_pallet_pressed)
        )
        this.currentBrushColorImageButton?.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.color_pallet_normal)
        )
        this.currentBrushColorImageButton = view
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            showRationaleDialog(DIALOG_DEFAULT_TITLE, STORAGE_ACCESS_RATIONALE)
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private fun showRationaleDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

}