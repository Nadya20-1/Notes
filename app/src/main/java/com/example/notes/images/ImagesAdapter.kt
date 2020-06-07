package com.example.notes.images

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.R
import com.example.notes.images.ImagesAdapter.ImagesViewHolder

class ImagesAdapter (private val mContext: Context) : RecyclerView.Adapter<ImagesViewHolder>() {
    private var mCursor: Cursor? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.image_item, parent, false)
        return ImagesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val fragranceName = mCursor!!.getColumnIndex(DataHelper.COLUMN_NAME)
        mCursor!!.moveToPosition(position)
        val image = mCursor!!.getBlob(fragranceName)
//        val bmp = BitmapFactory.decodeByteArray(image, 0, image.size)
//        holder.image.setImageBitmap(Bitmap.createScaledBitmap(bmp, 200, 200, false))
    }

    override fun getItemCount(): Int {
        return if (mCursor == null) { 0
        } else mCursor!!.count
    }

    fun swapCursor(c: Cursor?): Cursor? {
        if (mCursor === c) {
            return null
        }
        val temp = mCursor
        mCursor = c
        if (c != null) {
            notifyDataSetChanged()
        }
        return temp
    }

    inner class ImagesViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var image: ImageView = itemView.findViewById<View>(R.id.image_item) as ImageView
    }

}