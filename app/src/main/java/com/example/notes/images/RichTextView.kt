package com.example.notes.images

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.example.notes.R
import java.util.*

class RichTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {
    private val activity: Activity = context as Activity
    private var viewTagIndex = 1
    private val allLayout: LinearLayout = LinearLayout(context)
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val lastFocusText: TextView
    private val mTransitioner: LayoutTransition? = null
    private val editNormalPadding = 0 //
    private val disappearingImageIndex = 0

    private val btnListener: OnClickListener
    private val imagePaths: ArrayList<String> = ArrayList()

    private fun dip2px(context: Context, dipValue: Float): Int {
        val m = context.resources.displayMetrics.density
        return (dipValue * m + 0.5f).toInt()
    }

    fun clearAllLayout() {
        allLayout.removeAllViews()
    }

    val lastIndex: Int
        get() = allLayout.childCount

    @SuppressLint("InflateParams")
    private fun createTextView(hint: String?, paddingTop: Int): TextView {
        val textView = inflater.inflate(R.layout.rich_textview, null) as TextView
        textView.tag = viewTagIndex++
        textView.setPadding(editNormalPadding, paddingTop, editNormalPadding, paddingTop)
        textView.hint = hint
        return textView
    }

    @SuppressLint("InflateParams")
    private fun createImageLayout(): RelativeLayout {
        val layout = inflater.inflate(R.layout.edit_imageview, null) as RelativeLayout
        layout.tag = viewTagIndex++
        val imageView =
            layout.findViewById<View>(R.id.edit_imageView)
        imageView.setOnClickListener(btnListener)
        return layout
    }

    fun addTextViewAtIndex(index: Int, editStr: CharSequence?) {
        val textView = createTextView("", EDIT_PADDING)
        textView.text = editStr
        allLayout.addView(textView, index)
    }

    fun addImageViewAtIndex(index: Int, imagePath: String) {
        imagePaths.add(imagePath)
        val imageLayout = createImageLayout()
        val imageView =
            imageLayout.findViewById<View>(R.id.edit_imageView) as DataImageView
        imageView.absolutePath = imagePath
        if (imagePath.startsWith("http")) {
            Glide.with(context).load(imagePath).asBitmap().dontAnimate()
                .into(object : SimpleTarget<Bitmap?>(
                    Target.SIZE_ORIGINAL,
                    Target.SIZE_ORIGINAL
                ) {
                    override fun onResourceReady(
                        resource: Bitmap?,
                        glideAnimation: GlideAnimation<in Bitmap?>?
                    ) {
                        val imageHeight =
                            allLayout.width * resource!!.height / resource.width
                        val lp = RelativeLayout.LayoutParams(
                            LayoutParams.MATCH_PARENT, imageHeight)
                        lp.bottomMargin = 10
                        imageView.layoutParams = lp
                        Glide.with(context).load(imagePath).centerCrop()
                            .placeholder(R.drawable.img_load_fail).error(R.drawable.img_load_fail)
                            .override(
                                Target.SIZE_ORIGINAL,
                                Target.SIZE_ORIGINAL).into(imageView)
                    }
                })
        } else {
            val bmp = BitmapFactory.decodeFile(imagePath)
            val imageHeight = allLayout.width * bmp.height / bmp.width
            val lp = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, imageHeight)
            lp.bottomMargin = 10
            imageView.layoutParams = lp
            Glide.with(context).load(imagePath).centerCrop()
                .placeholder(R.drawable.img_load_fail).error(R.drawable.img_load_fail)
                .into(imageView)
               }
        allLayout.addView(imageLayout, index)
    }

    companion object {
        private const val EDIT_PADDING = 10
    }

    init {
        allLayout.orientation = LinearLayout.VERTICAL
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        allLayout.setPadding(50, 15, 50, 15)
        addView(allLayout, layoutParams)
        btnListener = OnClickListener {
        }
        val firstEditParam = LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT
        )
        val firstText = createTextView("Lack of content", dip2px(context, EDIT_PADDING.toFloat()))
        allLayout.addView(firstText, firstEditParam)
        lastFocusText = firstText
    }
}