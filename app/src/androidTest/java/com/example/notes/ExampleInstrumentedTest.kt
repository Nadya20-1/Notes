@file:Suppress("DEPRECATION")

package com.example.notes

import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.notes.data.NotesDao
import com.example.notes.data.NotesDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private var db: NotesDatabase? = null
    private var notesDao: NotesDao? = null

    @Before
    @Throws(Exception::class)
    fun createDb() {
        val context = InstrumentationRegistry.getTargetContext()
        db = Room.inMemoryDatabaseBuilder(
            context,
            NotesDatabase::class.java)
            .build()
        notesDao = db!!.notesDao()
    }

    @After
    @Throws(Exception::class)
    fun closeDb() {
        db!!.close()
    }


    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.notes", appContext.packageName)
    }
}
