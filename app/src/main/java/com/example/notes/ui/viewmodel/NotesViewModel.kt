package com.example.notes.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.notes.model.Notes
import com.example.notes.repository.NotesRepository
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotesRepository = NotesRepository(application)

    fun getAllNotes() : LiveData<List<Notes>>{
        return repository.loadAllNotes()
    }

    fun update(note: Notes)= viewModelScope.launch {
        repository.updateNote(note)
    }

    fun deleteNote(note: Notes) = viewModelScope.launch {
        repository.deleteNoteFromDatabase(note)
    }

    fun deleteAllNotes() = viewModelScope.launch {
        repository.deleteAllNotesFromDatabase()
    }

    fun insertNote(note: Notes) = viewModelScope.launch {
        repository.insertNoteInDatabase(note)
    }

}