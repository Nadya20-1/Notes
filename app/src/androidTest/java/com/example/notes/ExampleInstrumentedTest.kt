@file:Suppress("DEPRECATION")

package com.example.notes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notes.data.NotesDao
import com.example.notes.data.NotesDatabase
import com.example.notes.model.Notes
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private lateinit var db: NotesDatabase
    private var notesDao: NotesDao? = null
    private val note: Notes
        get() {
            TODO()}

    @Before
    @Throws(Exception::class)
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Room.inMemoryDatabaseBuilder(
            context,
            NotesDatabase::class.java)
            .build()
        notesDao = db.notesDao()
    }

    @After
    @Throws(Exception::class)
    fun closeDb() {
        db.close()
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Throws(InterruptedException::class)
    fun <T> LiveData<T>.getValueBlocking(): T? {
        var value: T? = null
        val latch = CountDownLatch(1)
        val innerObserver = Observer <T>{
            value = it
            latch.countDown()
        }
        observeForever(innerObserver)
        latch.await(2, TimeUnit.SECONDS)
        return value
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.notes", appContext.packageName)
    }

    @Test
    fun insertNoteTest() {
        val note = Notes(id = 1, title = "Test Notes" , description = "A", color = 1, date_time = "r", date_time_changed ="r" , content = "r" )
        db.notesDao().insert(note)
        val noteFromDb = note
        assertEquals(noteFromDb.title, note.title)
    }


    @Test
    fun getUserAsLiveDataTest() {
        val note = Notes(id = 1, title = "Test Notes" , description = "A", color = 1, date_time = "r", date_time_changed ="r" , content = "r" )
        db.notesDao().insert(note)
        val noteWrappedInLiveData = db.notesDao().getNoteAsLiveDataForTest()
        val noteFromDb = noteWrappedInLiveData.getValueBlocking()
        assertEquals(noteFromDb!!.title, note.title)
    }

}
