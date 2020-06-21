package com.example.notes

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notes.model.Notes
import com.example.notes.utils.CommonUtil
import com.example.notes.utils.StringUtils
import kotlinx.android.synthetic.main.note_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class NoteMainAdapter(private val context: Context) :
    RecyclerView.Adapter<NoteMainAdapter.ViewHolder>(), Filterable {

    private var notesList = emptyList<Notes>()
    private var filterResultList = emptyList<Notes>()
    private var imgWidth = 0
    private var imgHeight:Int = 0

    internal fun setNotesList (notes: List<Notes>) {
        notesList = notes
        filterResultList = notesList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        imgWidth = CommonUtil.getScreenWidth(context)
        imgHeight = CommonUtil.getScreenHeight(context)
        val view = LayoutInflater.from(context).inflate(R.layout.note_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filterResultList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = filterResultList[position]
        holder.bin(note)

        val content: String = note.content
        val path = StringUtils.getFirstImgPathFromStringData(content)
        Glide.with(context).load(path)
            .crossFade().centerCrop()
            .into(holder.firstNoteImg)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var firstNoteImg: ImageView? = null
        init {
            firstNoteImg = itemView.findViewById<View>(R.id.first_note_img) as ImageView
        }

        fun bin(note: Notes) {
            itemView.textView_title.text = note.title
            itemView.textView_description.text = note.description
            itemView.DateTimeNote.text = note.date_time
            itemView.DateTimeNoteChanged.text = note.date_time_changed
            itemView.colorPicked.background =  ColorDrawable(note.color)

            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onItemClick(filterResultList[position])
                }
            }
        }
    }

    private var listener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(note: Notes)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun getNoteAt(position: Int): Notes {
        return filterResultList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charString: CharSequence?): FilterResults {

                val charSearch = charString.toString()

                filterResultList = if(charSearch.isEmpty()){
                    notesList
                }else{
                    val resultList = ArrayList<Notes>()
                    for(row in notesList){
                        if(row.title.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT)) ||
                            row.description.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))){
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filterResultList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                filterResultList = filterResults!!.values as List<Notes>
                notifyDataSetChanged()
            }
        }
    }
}



