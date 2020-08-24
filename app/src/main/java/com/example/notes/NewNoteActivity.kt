@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION")

package com.example.notes

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.notes.images.RichTextEditor
import com.example.notes.ui.base.BaseActivity
import com.example.notes.utils.*
import com.zhihu.matisse.Matisse
import dev.sasikanth.colorsheet.ColorSheet
import kotlinx.android.synthetic.main.activity_new_note.*
import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.*
import java.util.*


@Suppress("DEPRECATION", "DEPRECATED_IDENTITY_EQUALS",
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class NewNoteActivity : BaseActivity(), RichTextEditor.OnDeleteImageListener{

    private var selectedColor: Int = ColorSheet.NO_COLOR
    private var insertDialog: ProgressDialog? = null

    override fun getViewID(): Int = R.layout.activity_new_note

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolBar2)
        iniView()

        if (intent.hasExtra(Constant.EXTRA_ID)) {
            supportActionBar?.title  = getText(R.string.edit_note)
            this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            editText_new_title.setText(intent.getStringExtra(Constant.EXTRA_REPLAY_TITLE))
            editText_new_description.setText(intent.getStringExtra(Constant.EXTRA_REPLAY_DESCRIPTION))

            et_new_content.post {
                et_new_content.clearAllLayout()
                showData(intent.getStringExtra(Constant.EXTRA_REPLAY_CONTENT)) }

        } else {
            supportActionBar?.title  = getText(R.string.add_note)
        }
    }

    private fun iniView() {
        insertDialog = ProgressDialog(this)
        insertDialog!!.setMessage(getText(R.string.loading))
        insertDialog!!.setCanceledOnTouchOutside(false)
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
            }
            R.id.button_image ->
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "You have already granted this permission!", Toast.LENGTH_SHORT).show()
                    DeviceUtils.callGallery(this)
                } else {
                    requestStoragePermission()
                }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun requestStoragePermission() {
            ActivityCompat.requestPermissions(
                this@NewNoteActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                Constant.STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        if (requestCode == Constant.STORAGE_PERMISSION_CODE)  {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show()
                DeviceUtils.callGallery(this)
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun saveNote() {
        val replayIntent = Intent()
        val noteContent = getEditData()
        if(TextUtils.isEmpty(editText_new_title.text) || TextUtils.isEmpty(editText_new_description.text)) {
            setResult(Activity.RESULT_CANCELED, replayIntent)
        }else {
            replayIntent.putExtra(Constant.EXTRA_REPLAY_TITLE, editText_new_title.text.toString())
            replayIntent.putExtra(Constant.EXTRA_REPLAY_DESCRIPTION, editText_new_description.text.toString())
            replayIntent.putExtra(Constant.EXTRA_REPLAY_COLOR, selectedColor.toString())
            replayIntent.putExtra(Constant.EXTRA_REPLAY_CONTENT, noteContent)
            setResult(Activity.RESULT_OK, replayIntent)
        }

        if (editText_new_title.text.toString().trim().isBlank() || editText_new_description.text.toString().trim().isBlank()) {
            Toast.makeText(this, R.string.empty_note, Toast.LENGTH_SHORT).show()
            return
        }

        val data = Intent().apply {
            putExtra(Constant.EXTRA_REPLAY_TITLE, editText_new_title.text.toString())
            putExtra(Constant.EXTRA_REPLAY_DESCRIPTION, editText_new_description.text.toString())
            putExtra(Constant.EXTRA_REPLAY_COLOR, selectedColor.toString())
            putExtra(Constant.EXTRA_REPLAY_CONTENT, noteContent)
            if (intent.getIntExtra(Constant.EXTRA_ID, -1) != -1) {
                putExtra(Constant.EXTRA_ID, intent.getIntExtra(Constant.EXTRA_ID, -1))
            }
        }
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun showData(html: String) {
        val loadingDialog: ProgressDialog? = null
        Observable.create<String> { subscriber -> showEditData(subscriber, html) }
            .onBackpressureBuffer()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String> {
                override fun onCompleted() {
                    loadingDialog?.dismiss()
                    et_new_content.addEditTextAtIndex(et_new_content.lastIndex, "")
                }

                override fun onError(e: Throwable) {
                    loadingDialog?.dismiss()
                    (this@NewNoteActivity).showToast("Picture is destroyed or unavailable")
                }

                override fun onNext(text: String) {
                    if (text.contains("<img") && text.contains("src=")) {
                        val imagePath = StringUtils.getImgSrc(text)
                        et_new_content.addEditTextAtIndex(et_new_content.lastIndex, "")
                        et_new_content.addImageViewAtIndex(et_new_content.lastIndex, imagePath)
                    } else {
                        et_new_content.addEditTextAtIndex(et_new_content.lastIndex, text)
                    }
                }
            })
    }

    private fun showEditData(subscriber: Subscriber<in String?>, html: String?) {
        try {
            val textList = StringUtils.cutStringByImgTag(html!!)
            for (i in textList.indices) {
                val text = textList[i]
                subscriber.onNext(text)
            }
            subscriber.onCompleted()
        } catch (e: Exception) {
            e.printStackTrace()
            subscriber.onError(e)
        }
    }

    private fun getEditData(): String? {
        val editList: List<RichTextEditor.EditData> = et_new_content.buildEditData()
        val content = StringBuffer()
        for (itemData in editList) {
            if (itemData.inputStr != null) {
                content.append(itemData.inputStr)
            } else if (itemData.imagePath != null) {
                content.append("<img src=\"").append(itemData.imagePath).append("\"/>")
            }
        }
        return content.toString()
    }

    private fun insertImagesASync(data: Intent) {
        insertDialog!!.show()
        Observable.create<String> { subscriber ->
            try {
                et_new_content.measure(0, 0)
                val width: Int = CommonUtil.getScreenWidth(baseContext)
                val height: Int = CommonUtil.getScreenHeight(baseContext)
                val photos = Matisse.obtainResult(data) as ArrayList<Uri>
                for (imageUri in photos) {
                    var bitmap: Bitmap? = getBitmapFromUri(imageUri)
                    val file = getFile(bitmap!!)
                    var imagePath = file!!.path
                    bitmap = ImageUtils.getSmallBitmap(imagePath, width, height)
                    file.delete()
                    imagePath = SDCardUtil.saveToSdCard(bitmap!!)
                    subscriber.onNext(imagePath)
                }
                subscriber.onCompleted()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                subscriber.onError(e)
            }
        }
            .onBackpressureBuffer()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String?> {
                override fun onCompleted() {
                    insertDialog!!.dismiss()
                    et_new_content.addEditTextAtIndex(et_new_content.lastIndex, " ")
                    Toast.makeText(this@NewNoteActivity, R.string.image_successfully_inserted, Toast.LENGTH_SHORT).show()
                }

                override fun onError(e: Throwable) {
                    insertDialog!!.dismiss()
                    Toast.makeText(
                        this@NewNoteActivity,
                        "Fail to Insert Image:" + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onNext(imagePath: String?) {
                    et_new_content.insertImage(imagePath, et_new_content.measuredWidth)
                }
            })
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        val inputStream: InputStream?
        var bitmap: Bitmap? = null
        try {
            inputStream = this.contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this@NewNoteActivity, R.string.get_image_failed, Toast.LENGTH_SHORT).show()
        }
        return bitmap
    }

    private fun getFile(bitmap: Bitmap): File? {
        var pictureDir: String? = null
        var fos: FileOutputStream? = null
        var bos: BufferedOutputStream? = null
        var baos: ByteArrayOutputStream? = null
        var file: File? = null
        try {
            baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val byteArray = baos.toByteArray()
            val saveDir = Environment.getExternalStorageDirectory()
                .toString() + "/notes"
            val dir = File(saveDir)
            if (!dir.exists()) {
                dir.mkdir()
            }
            file = File(
                saveDir,
                Calendar.getInstance().time.toString() + ".jpg"
            )
            file.delete()
            if (!file.exists()) {
                file.createNewFile()
            }
            fos = FileOutputStream(file)
            bos = BufferedOutputStream(fos)
            bos.write(byteArray)
            pictureDir = file.path
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            if (baos != null) {
                try {
                    baos.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            if (bos != null) {
                try {
                    bos.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        return file
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
          if (resultCode == RESULT_OK && data != null && requestCode == Constant.REQUEST_CODE_CHOOSE) {
              insertImagesASync(data)
           }
    }

    override fun onDeleteImage(imagePath: String?) {
        val isOK = SDCardUtil.deleteFile(imagePath)
        if (isOK) {
            Toast.makeText(this@NewNoteActivity, "Successfully deleted：$imagePath", Toast.LENGTH_SHORT).show()
        }
    }
}

