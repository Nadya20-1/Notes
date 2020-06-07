package com.example.notes

import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notes.model.Notes
import com.example.notes.ui.base.BaseActivity
import com.example.notes.ui.viewmodel.NotesViewModel
import kotlinx.android.synthetic.main.activity_main.*

@Suppress("DEPRECATION")
class MainActivity : BaseActivity() {

    private lateinit var viewModel: NotesViewModel
    private lateinit var adapter: NoteMainAdapter
    private lateinit var itemSearch: SearchView
    private var image: Drawable? = null
    private var background: ColorDrawable? = null
    private val addNoteRequestCode = 1
    private val editNoteRequestCode = 2

    override fun getViewID(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        supportActionBar?.title = getText(R.string.notes)

        image = getDrawable(R.drawable.ic_delete_another)

        background = ColorDrawable(resources.getColor(R.color.delete))

        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)

        adapter = NoteMainAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.getAllNotes().observe(this,
            Observer {
                adapter.setNotesList(it)
            }
        )

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteConfirmation(adapter.getNoteAt(viewHolder.adapterPosition))
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

                val itemView = viewHolder.itemView
                val backgroundCornerOffset = 20 //background is behind the rounded corners of itemView

                val iconMargin = (itemView.height - image!!.intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - image!!.intrinsicHeight) / 2
                val iconBottom = iconTop + image!!.intrinsicHeight

                when {
                    dX > 0 -> { // Swiping to the right
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = iconLeft + image!!.intrinsicWidth
                        image!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                        background!!.setBounds(
                            itemView.left, itemView.top,
                            itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom
                        )
                    }
                    dX < 0 -> { // Swiping to the left
                        val iconLeft = itemView.right - iconMargin - image!!.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        image!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                        background!!.setBounds(
                            itemView.right + dX.toInt() - backgroundCornerOffset,
                            itemView.top, itemView.right, itemView.bottom
                        )
                    }
                    else ->
                        background!!.setBounds(0, 0, 0, 0)
                }
                background!!.draw(c)
                image!!.draw(c)
            }
        }
        ).attachToRecyclerView(recyclerView)

        adapter.setOnItemClickListener(object : NoteMainAdapter.OnItemClickListener {
            override fun onItemClick(note: Notes) {
                val intent = Intent(baseContext, NewNoteActivity::class.java)
                intent.putExtra(NewNoteActivity.EXTRA_ID, note.id)
                intent.putExtra(NewNoteActivity.EXTRA_REPLAY_TITLE, note.title)
                intent.putExtra(NewNoteActivity.EXTRA_REPLAY_DESCRIPTION, note.description)
                startActivityForResult(intent, editNoteRequestCode)
                viewModel.update(note)
            }
        })
    }

    override fun onResume() {
        super.onResume()

        fab.setOnClickListener {
            val intent = Intent(this, NewNoteActivity::class.java)
            startActivityForResult(intent, addNoteRequestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == addNoteRequestCode && resultCode == Activity.RESULT_OK) {
            val title = data?.getStringExtra(NewNoteActivity.EXTRA_REPLAY_TITLE).toString()
            val description = data?.getStringExtra(NewNoteActivity.EXTRA_REPLAY_DESCRIPTION).toString()
            val color = data?.getStringExtra(NewNoteActivity.EXTRA_REPLAY_COLOR).toString().toInt()
            val note = Notes(
                0,
                title,
                description,
                color,
                getDateTime()
            )
            viewModel.insertNote(note)
            Log.d("TAG", "$title - $description")
        } else if (requestCode == editNoteRequestCode && resultCode == Activity.RESULT_OK) {

            val id = data?.getIntExtra(NewNoteActivity.EXTRA_ID, -1)

            if (id == -1) {
                Toast.makeText(this, R.string.could_not_update, Toast.LENGTH_SHORT).show()
            }

            val title = data?.getStringExtra(NewNoteActivity.EXTRA_REPLAY_TITLE).toString()
            val description = data?.getStringExtra(NewNoteActivity.EXTRA_REPLAY_DESCRIPTION).toString()
            val color = data?.getStringExtra(NewNoteActivity.EXTRA_REPLAY_COLOR).toString().toInt()
            val note = Notes(
                0,
                title,
                description,
                color,
                getDateTime()
            )
            note.id = data!!.getIntExtra(NewNoteActivity.EXTRA_ID, -1)
            viewModel.update(note)

        } else {
            Toast.makeText(this, R.string.note_not_saved, Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteConfirmation(note: Notes) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.confirm_delete)
        alertDialog.setMessage(R.string.delete_this)
        alertDialog.setIcon(R.drawable.ic_delete)
        alertDialog.setPositiveButton(R.string.yes) { _ , _ ->
            viewModel.deleteNote(note)
            Toast.makeText(baseContext, R.string.note_deleted, Toast.LENGTH_SHORT).show()
        }

        alertDialog.setNegativeButton(R.string.no) { dialog, _ ->
            viewModel.update(note)
            dialog.cancel()
        }
        alertDialog.show()
    }

    private fun deleteAllConfirmation() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(R.string.confirm_delete)
        alertDialog.setMessage(R.string.delete_all)
        alertDialog.setIcon(R.drawable.ic_delete)
        alertDialog.setPositiveButton(R.string.yes) { _ , _ ->
            viewModel.deleteAllNotes()
            Toast.makeText(this, R.string.all_entries_deleted, Toast.LENGTH_SHORT).show()
        }

        alertDialog.setNegativeButton(R.string.no) { dialog, _ ->
            dialog.cancel()
        }
        alertDialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val item = menu.findItem(R.id.searchItem)
        itemSearch = item.actionView as SearchView
        itemSearch.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        itemSearch.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    adapter.filter.filter(query)
                    return false
                }
                override fun onQueryTextChange(query: String?): Boolean {
                    adapter.filter.filter(query)
                    return false
                }
            }
        )
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.searchItem -> {
                return true
            }
            R.id.deleteAll -> {
                deleteAllConfirmation()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
