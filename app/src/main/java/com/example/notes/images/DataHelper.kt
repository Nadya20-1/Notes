package com.example.notes.images

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log

class DataHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    var context: Context? = null
    private var db: SQLiteDatabase = this.writableDatabase
    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_IMAGE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_NAME + " BLOB NOT NULL " + " );"
        db.execSQL(SQL_CREATE_IMAGE_TABLE)
        Log.d(TAG, "Database Created Successfully")
    }

    fun addToDb(image: ByteArray?) {
        val cv = ContentValues()
        cv.put(COLUMN_NAME, image)
        db.insert(TABLE_NAME, null, cv)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        private val TAG = DataHelper::class.java.simpleName
        private const val DATABASE_NAME = "image.database"
        private const val DATABASE_VERSION = 1
        const val COLUMN_NAME = "image_name"
        const val TABLE_NAME = "image_table"
    }

}