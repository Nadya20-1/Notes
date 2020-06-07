package com.example.notes

import android.Manifest
import android.app.Activity
import android.app.LoaderManager
import android.app.ProgressDialog
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.notes.images.DataHelper
import com.example.notes.images.ImagesAdapter
import com.example.notes.images.ImagesProvider
import com.example.notes.ui.base.BaseActivity
import dev.sasikanth.colorsheet.ColorSheet
import kotlinx.android.synthetic.main.activity_new_note.*
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

@Suppress("DEPRECATION", "DEPRECATED_IDENTITY_EQUALS")
class NewNoteActivity : BaseActivity(), LoaderManager.LoaderCallbacks<Cursor>{

    var dbHelper: DataHelper? = null
    private var imagesAdapter: ImagesAdapter? = null
    private var progressBar: ProgressDialog? = null
    private var progressBarStatus = 0
    private var thumbnail: Bitmap? = null
    private val progressBarbHandler = Handler()
    private var selectedColor: Int = ColorSheet.NO_COLOR

    override fun getViewID(): Int = R.layout.activity_new_note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolBar2)

        image_recycler_view!!.layoutManager = LinearLayoutManager(this)

        imagesAdapter = ImagesAdapter(this)
        image_recycler_view!!.adapter = imagesAdapter

        loaderManager.initLoader(
            IMAGES_LOADER,
            null,
            this)

        if (intent.hasExtra(EXTRA_ID)) {
            supportActionBar?.title  = getText(R.string.edit_note)
            this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            editText_new_title.setText(intent.getStringExtra(EXTRA_REPLAY_TITLE))
            editText_new_description.setText(intent.getStringExtra(EXTRA_REPLAY_DESCRIPTION))
        } else {
            supportActionBar?.title  = getText(R.string.add_note)
        }

//        button_image.setOnClickListener {
//            if (ActivityCompat.checkSelfPermission(
//                    this@NewNoteActivity,
//                    Manifest.permission.READ_EXTERNAL_STORAGE
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this@NewNoteActivity,
//                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                    REQUEST_EXTERNAL_STORAGE
//                )
//            } else {
//                pickImage()
//            }
//        }

        if (ContextCompat.checkSelfPermission(this@NewNoteActivity,
                Manifest.permission.CAMERA) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@NewNoteActivity,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
        dbHelper = DataHelper(this)
    }

    override fun onCreateLoader(
        i: Int, bundle: Bundle?): Loader<Cursor?>? {
        val projection = arrayOf(DataHelper.COLUMN_NAME)
        return CursorLoader(
            this,
            ImagesProvider.CONTENT_URI,
            projection,
            null,
            null,
            null
        )
    }

    override fun onLoadFinished(
        loader: Loader<Cursor?>?, cursor: Cursor?) {
        imagesAdapter!!.swapCursor(cursor)
    }

    override fun onLoaderReset(loader: Loader<Cursor?>?) {
        imagesAdapter!!.swapCursor(null)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_note_item, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.colorPicked -> {
                val colors = resources.getIntArray(R.array.colors)
                    ColorSheet().cornerRadius(8)
                        .colorPicker(
                            colors = colors,
                            selectedColor = selectedColor,
                            listener = { color ->
                                selectedColor = color
                            }
                        ).show(supportFragmentManager)
            }
            R.id.save_note-> {
                saveNote()
                val byte = ByteArrayOutputStream()
                val data_image = byte.toByteArray()
                dbHelper!!.addToDb(data_image)
                Toast.makeText(this, R.string.image_saved, Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveNote() {
        val replayIntent = Intent()
        if(TextUtils.isEmpty(editText_new_title.text) || TextUtils.isEmpty(editText_new_description.text)) {
            setResult(Activity.RESULT_CANCELED, replayIntent)
        }else {
            replayIntent.putExtra(EXTRA_REPLAY_TITLE, editText_new_title.text.toString())
            replayIntent.putExtra(EXTRA_REPLAY_DESCRIPTION, editText_new_description.text.toString())
            replayIntent.putExtra(EXTRA_REPLAY_COLOR, selectedColor.toString())
            setResult(Activity.RESULT_OK, replayIntent)
        }

        if (editText_new_title.text.toString().trim().isBlank() || editText_new_description.text.toString().trim().isBlank()) {
            Toast.makeText(this, R.string.empty_note, Toast.LENGTH_SHORT).show()
            return
        }

        val data = Intent().apply {
            putExtra(EXTRA_REPLAY_TITLE, editText_new_title.text.toString())
            putExtra(EXTRA_REPLAY_DESCRIPTION, editText_new_description.text.toString())
            putExtra(EXTRA_REPLAY_COLOR, selectedColor.toString())
            if (intent.getIntExtra(EXTRA_ID, -1) != -1) {
                putExtra(EXTRA_ID, intent.getIntExtra(EXTRA_ID, -1))
            }
        }
        setResult(Activity.RESULT_OK, data)
        finish()
    }
    fun onClick(view: View) {
        when (view.id) {
            R.id.button_image ->
                MaterialDialog.Builder(this)
                .title(R.string.uploadImages)
                .items(R.array.uploadImages)
                .itemsIds(R.array.itemIds)
                .itemsCallback { _, _, which, _ ->
                    when (which) {
                        0 -> {
                            val photoPickerIntent = Intent(Intent.ACTION_PICK)
                            photoPickerIntent.type = "image/*"
                            startActivityForResult(photoPickerIntent, SELECT_PHOTO)
                        }
                        1 -> {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(intent, CAPTURE_PHOTO)
                        }
                    }
                }.show()
        }
    }

    private fun pickImage() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent = Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
        startActivityForResult(chooserIntent, REQUEST_EXTERNAL_STORAGE)


        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            REQUEST_EXTERNAL_STORAGE -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    pickImage()
//                } else {

//                }
//                return
//            }
//        }
    }

    private fun setProgressBar() {
        progressBar = ProgressDialog(this)
        progressBar!!.setCancelable(true)
        progressBar!!.setMessage(getText(R.string.please_wait))
        progressBar!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressBar!!.progress = 0
        progressBar!!.max = 100
        progressBar!!.show()
        progressBarStatus = 0
        Thread(Runnable {
            while (progressBarStatus < 100) {
                progressBarStatus += 30
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                progressBarbHandler.post { progressBar!!.progress = progressBarStatus }
            }
            if (progressBarStatus >= 100) {
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                progressBar!!.dismiss()
            }
        }).start()
    }

    private fun onCaptureImageResult(data: Intent) {
        thumbnail = data.extras!!["data"] as Bitmap?
        setProgressBar()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK) {
                try {
                    //val imageUri = data?.data
                    //val imageStream = contentResolver.openInputStream(imageUri!!)
                    //val selectedImage = BitmapFactory.decodeStream(imageStream)
                    setProgressBar()
                    image_recycler_view.layoutManager = LinearLayoutManager(this)
                    imagesAdapter = ImagesAdapter(this)
                    image_recycler_view.adapter = imagesAdapter
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
        } else if (requestCode == CAPTURE_PHOTO && resultCode == RESULT_OK) {
                if (data != null) {
                    onCaptureImageResult(data)
                }
        }

//        if (requestCode == REQUEST_EXTERNAL_STORAGE && resultCode == RESULT_OK) {
//            val bitmaps: MutableList<Bitmap> = ArrayList()
//            val clipData = data!!.clipData
//
//            if (clipData != null) { //multiple images selected
//                for (i in 0 until clipData.itemCount) {
//                    val imageUri = clipData.getItemAt(i).uri
//                    Log.d("URI", imageUri.toString())
//                    try {
//                        val inputStream =
//                            contentResolver.openInputStream(imageUri)
//                        val bitmap = BitmapFactory.decodeStream(inputStream)
//                        bitmaps.add(bitmap)
//                    } catch (e: FileNotFoundException) {
//                        e.printStackTrace()
//                    }
//                }
//            } else { //single image selected
//                val imageUri = data.data
//                Log.d("URI", imageUri.toString())
//                try {
//                    val inputStream =
//                        contentResolver.openInputStream(imageUri!!)
//                    val bitmap = BitmapFactory.decodeStream(inputStream)
//                    bitmaps.add(bitmap)
//                } catch (e: FileNotFoundException) {
//                    e.printStackTrace()
//                }
//            }
//            Thread(Runnable {
//                for (b in bitmaps) {
//                    runOnUiThread { first_note_img.setImageBitmap(b) }
//                    try {
//                        Thread.sleep(3000)
//                    } catch (e: InterruptedException) {
//                        e.printStackTrace()
//                    }
//                }
//            }).start()
//        }
    }

    companion object {
        const val EXTRA_ID = "EXTRA_ID"
        const val EXTRA_REPLAY_TITLE = "REPLAY_TITLE"
        const val EXTRA_REPLAY_DESCRIPTION = "REPLAY_DESCRIPTION"
        const val EXTRA_REPLAY_COLOR = "REPLAY_COLOR"
        const val REQUEST_EXTERNAL_STORAGE = 100
        const val SELECT_PHOTO = 1
        const val CAPTURE_PHOTO = 2
        const val IMAGES_LOADER = 0
    }
}

