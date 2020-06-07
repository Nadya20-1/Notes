package com.example.notes.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.notes.model.Notes

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes_table")
    fun loadAll(): LiveData<List<Notes>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
     fun insert(note: Notes)

    @Update
     fun updateNote(note: Notes)

    @Delete
     fun delete(note: Notes)

    @Query("DELETE FROM notes_table")
     fun deleteAll()
}