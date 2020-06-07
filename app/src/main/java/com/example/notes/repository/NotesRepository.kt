package com.example.notes.repository

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.example.notes.data.NotesDao
import com.example.notes.data.NotesDatabase
import com.example.notes.model.Notes

class NotesRepository(application: Application) {

    private val notesDao: NotesDao = NotesDatabase.getDatabase(application).notesDao()

    private val notesList: LiveData<List<Notes>>

    init {
        notesList = notesDao.loadAll()
    }

    fun loadAllNotes() : LiveData<List<Notes>>{
        return notesList
    }

    fun updateNote(note: Notes) {
        UpdateNoteAsyncTask(notesDao).execute(note)
    }

    fun deleteNoteFromDatabase(note: Notes) {
        DeleteNoteAsyncTask(notesDao).execute(note)
    }

    fun insertNoteInDatabase(note: Notes) {
        InsertNoteAsyncTask(notesDao).execute(note)
    }

    fun deleteAllNotesFromDatabase() {
        DeleteAllNotesAsyncTask(
            notesDao
        ).execute()
    }
    companion object {
        private class InsertNoteAsyncTask(val noteDao: NotesDao) : AsyncTask<Notes, Unit, Unit>() {

            override fun doInBackground(vararg p0: Notes?) {
                noteDao.insert(p0[0]!!)
            }
        }

        private class UpdateNoteAsyncTask(val noteDao: NotesDao) : AsyncTask<Notes, Unit, Unit>() {

            override fun doInBackground(vararg p0: Notes?) {
                noteDao.updateNote(p0[0]!!)
            }
        }

        private class DeleteNoteAsyncTask(val noteDao: NotesDao) : AsyncTask<Notes, Unit, Unit>() {

            override fun doInBackground(vararg p0: Notes?) {
                noteDao.delete(p0[0]!!)
            }
        }

        private class DeleteAllNotesAsyncTask(val noteDao: NotesDao) : AsyncTask<Unit, Unit, Unit>() {

            override fun doInBackground(vararg p0: Unit?) {
                noteDao.deleteAll()
            }
        }
    }
}