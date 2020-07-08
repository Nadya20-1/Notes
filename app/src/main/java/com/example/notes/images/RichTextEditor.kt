package com.example.notes.images

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.bumptech.glide.Glide
import com.example.notes.R
import java.util.*
import kotlin.math.roundToInt

internal class RichTextEditor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {
    private val activity: Activity = context as Activity
    private var viewTagIndex = 1
    private val allLayout: LinearLayout
    private val inflater: LayoutInflater
    private val keyListener: OnKeyListener
    private val btnListener: OnClickListener
    private val focusListener: OnFocusChangeListener
    private var lastFocusEdit: EditText
    private val transition: LayoutTransition? = null
    private val editNormalPadding = 0
    private var disappearingImageIndex = 0
    private val imagePaths: ArrayList<String?>
    private val onDeleteImageListener: OnDeleteImageListener?

    private fun dip2px(context: Context, dipValue: Float): Int {
        val m = context.resources.displayMetrics.density
        return (dipValue * m + 0.5f).toInt()
    }

    private fun onBackspacePress(editTxt: EditText) {
        val startSelection = editTxt.selectionStart
        if (startSelection == 0) {
            val editIndex = allLayout.indexOfChild(editTxt)
            val preView = allLayout.getChildAt(editIndex - 1)
            if (null != preView) {
                if (preView is RelativeLayout) {
                    onImageCloseClick(preView)
                } else if (preView is EditText) {
                    val str1 = editTxt.text.toString()
                    val str2 = preView.text.toString()
                    allLayout.removeView(editTxt)
                    preView.setText(str2 + str1)
                    preView.requestFocus()
                    preView.setSelection(str2.length, str2.length)
                    lastFocusEdit = preView
                }
            }
        }
    }

    interface OnDeleteImageListener {
        fun onDeleteImage(imagePath: String?)
    }

    private fun onImageCloseClick(view: View) {
        disappearingImageIndex = allLayout.indexOfChild(view)
        val dataList = buildEditData()
        val editData = dataList[disappearingImageIndex]
        if (editData.imagePath != null) {
            onDeleteImageListener?.onDeleteImage(editData.imagePath)
            //SDCardUtil.deleteFile(editData.imagePath);
            imagePaths.remove(editData.imagePath)
        }
        allLayout.removeView(view)
        mergeEditText()
    }

    fun clearAllLayout() {
        allLayout.removeAllViews()
    }

    val lastIndex: Int
        get() = allLayout.childCount

    @SuppressLint("InflateParams")
    fun createEditText(hint: String?, paddingTop: Int): EditText {
        val editText = inflater.inflate(R.layout.rich_edittext, null) as EditText
        //	EditText editText= findViewById(R.id.deletable_edit_text);
        editText.setOnKeyListener(keyListener)
        editText.tag = viewTagIndex++
        editText.setPadding(editNormalPadding, paddingTop, editNormalPadding, paddingTop)
        editText.hint = hint
        editText.onFocusChangeListener = focusListener
//        editText.post {
//            val inputMethodManager =
//                ContextCompat.getSystemService<Any>(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
//            inputMethodManager!!.toggleSoftInputFromWindow(
//                editText.applicationWindowToken,
//                InputMethodManager.SHOW_IMPLICIT,
//                0
//            )
//            editText.requestFocus()
//        }
        return editText
    }

    private fun createImageLayout(): RelativeLayout {
        val view = activity.findViewById<View>(R.id.et_new_content)
        val inflater = LayoutInflater.from(context)
        val addLayout = inflater.inflate(R.layout.edit_imageview, null)
        addLayout.tag = viewTagIndex++
        val imageView = addLayout.findViewById<View>(R.id.edit_imageView)
        imageView.setOnClickListener(btnListener)
        return addLayout as RelativeLayout
    }

    fun dpToPx(dps: Int): Int {
        return (resources.displayMetrics.density * dps).roundToInt()
    }

    fun insertImage(imagePath: String?, width: Int) {
        val bmp = getScaledBitmap(imagePath, width)
        insertImage(bmp, imagePath)
    }

    private fun insertImage(bitmap: Bitmap?, imagePath: String?) {
        val lastEditStr = lastFocusEdit.text.toString()
        val cursorIndex = lastFocusEdit.selectionStart
        val editStr1 =
            lastEditStr.substring(0, cursorIndex).trim { it <= ' ' }
        val editStr2 =
            lastEditStr.substring(cursorIndex).trim { it <= ' ' }
        val lastEditIndex = allLayout.indexOfChild(lastFocusEdit)
        when {
            lastEditStr.isEmpty() -> {
                addEditTextAtIndex(lastEditIndex + 1, "")
                addImageViewAtIndex(lastEditIndex + 1, bitmap, imagePath)
            }
            editStr1.isEmpty() -> {
                addImageViewAtIndex(lastEditIndex, bitmap, imagePath)
                addEditTextAtIndex(lastEditIndex + 1, "")
            }
            editStr2.isEmpty() -> {
                addEditTextAtIndex(lastEditIndex + 1, "")
                addImageViewAtIndex(lastEditIndex + 1, bitmap, imagePath)
            }
            else -> {
                lastFocusEdit.setText(editStr1)
                addEditTextAtIndex(lastEditIndex + 1, editStr2)
                addEditTextAtIndex(lastEditIndex + 1, "")
                addImageViewAtIndex(lastEditIndex + 1, bitmap, imagePath)
            }
        }
        hideKeyBoard()
    }

    private fun hideKeyBoard() {
        val imm = context
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(lastFocusEdit.windowToken, 0)
    }

    fun addEditTextAtIndex(index: Int, editStr: CharSequence?) {
        val editText2 = createEditText("Content Here", EDIT_PADDING)
        if (editStr != null && editStr.isNotEmpty()) {
            editText2.setText(editStr)
        }
        editText2.onFocusChangeListener = focusListener
        allLayout.layoutTransition = null
        allLayout.addView(editText2, index)
        allLayout.layoutTransition = transition
        lastFocusEdit = editText2
        lastFocusEdit.requestFocus()
        lastFocusEdit.setSelection(editStr!!.length, editStr.length)
    }

    private fun addImageViewAtIndex(index: Int, bmp: Bitmap?, imagePath: String?) {
        imagePaths.add(imagePath)
        val imageLayout = createImageLayout()
        val imageView = imageLayout.findViewById<View>(R.id.edit_imageView) as DataImageView
        Glide.with(context).load(imagePath).crossFade().centerCrop().into(imageView)
        imageView.absolutePath = imagePath
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        val lp = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 500)
        lp.bottomMargin = 10
        imageView.layoutParams = lp
        allLayout.addView(imageLayout, index)
    }

    fun addImageViewAtIndex(index: Int, imagePath: String?) {
        imagePaths.add(imagePath)
        val imageLayout = createImageLayout()
        val imageView = imageLayout.findViewById<View>(R.id.edit_imageView) as DataImageView
        imageView.absolutePath = imagePath
        val lp = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 500)
        lp.bottomMargin = 10
        imageView.layoutParams = lp
        Glide.with(context).load(imagePath).crossFade().centerCrop()
            .placeholder(R.drawable.img_load_fail).error(R.drawable.img_load_fail).into(imageView)
        allLayout.addView(imageLayout, index)
    }

    private fun getScaledBitmap(filePath: String?, width: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(filePath, options)
        val sampleSize = if (options.outWidth > width) options.outWidth / width + 1
        else 1
        options.inJustDecodeBounds = false
        options.inSampleSize = sampleSize
        return BitmapFactory.decodeFile(filePath, options)
    }

    private fun mergeEditText() {
        val preView = allLayout.getChildAt(disappearingImageIndex - 1)
        val nextView = allLayout.getChildAt(disappearingImageIndex)
        if (preView != null && preView is EditText && null != nextView && nextView is EditText) {
            val str1 = preView.text.toString()
            val str2 = nextView.text.toString()
            var mergeText = ""
            mergeText = if (str2.isNotEmpty()) {
                str1 + "\n" + str2
            } else {
                str1
            }
            allLayout.layoutTransition = null
            allLayout.removeView(nextView)
            preView.setText(mergeText)
            preView.requestFocus()
            preView.setSelection(str1.length, str1.length)
            allLayout.layoutTransition = transition
        }
    }

    fun buildEditData(): List<EditData> {
        val dataList: MutableList<EditData> = ArrayList()
        val num = allLayout.childCount
        for (index in 0 until num) {
            val itemView = allLayout.getChildAt(index)
            val itemData = EditData()
            if (itemView is EditText) {
                itemData.inputStr = itemView.text.toString()
            } else if (itemView is RelativeLayout) {
                val item =
                    itemView.findViewById<View>(R.id.edit_imageView) as DataImageView
                itemData.imagePath = item.absolutePath
            }
            dataList.add(itemData)
        }
        return dataList
    }

    inner class EditData {
        var inputStr: String? = null
        var imagePath: String? = null
    }

    companion object {
        private const val EDIT_PADDING = 10
    }

    init {
        onDeleteImageListener = context as OnDeleteImageListener
        imagePaths = ArrayList()
        inflater = LayoutInflater.from(context)
        allLayout = LinearLayout(context)
        allLayout.orientation = LinearLayout.VERTICAL
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        allLayout.setPadding(50, 15, 50, 15)
        addView(allLayout, layoutParams)
        keyListener = OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN
                && event.keyCode == KeyEvent.KEYCODE_DEL
            ) {
                val edit = v as EditText
                onBackspacePress(edit)
            }
            false
        }
        btnListener = OnClickListener { v ->
            if (v is ImageView) {
                val parentView = v.getParent() as RelativeLayout
                onImageCloseClick(parentView)
            }
        }
        focusListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                lastFocusEdit = v as EditText
            }
        }
        val firstEditParam = LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
        )
        val firstEdit = createEditText(
            "Content",
            dip2px(context, EDIT_PADDING.toFloat())
        )
        allLayout.addView(firstEdit, firstEditParam)
        lastFocusEdit = firstEdit
    }
}