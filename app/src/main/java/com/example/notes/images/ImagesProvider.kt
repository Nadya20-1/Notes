package com.example.notes.images

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns

@Suppress("NAME_SHADOWING")
class ImagesProvider : ContentProvider() {
    companion object {
        //val LOG_TAG = ImagesProvider::class.java.simpleName
        private const val PICTURES = 100
        private const val PICTURES_ID = 101
        private const val CONTENT_AUTHORITY = "com.example.notes"
        private val BASE_CONTENT_URI = Uri.parse("content://$CONTENT_AUTHORITY")
        private const val PATH_IMAGES = "image-path"
        val CONTENT_URI = Uri.withAppendedPath(
            BASE_CONTENT_URI,
            PATH_IMAGES)!!

        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            sUriMatcher.addURI(
                CONTENT_AUTHORITY,
                PATH_IMAGES,
                PICTURES
            )
            sUriMatcher.addURI(
                CONTENT_AUTHORITY,
                "$PATH_IMAGES/#",
                PICTURES_ID
            )
        }
    }

    private var dbHelper: DataHelper? = null

    override fun onCreate(): Boolean {
        dbHelper = DataHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        var selection = selection
        var selectionArgs = selectionArgs
        val database = dbHelper!!.readableDatabase

        val cursor: Cursor

        when (sUriMatcher.match(uri)) {
            PICTURES -> cursor = database.query(
                DataHelper.TABLE_NAME, projection, selection, selectionArgs,
                null, null, sortOrder
            )
            PICTURES_ID -> {
                selection = BaseColumns._ID + "=?"
                selectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                cursor = database.query(
                    DataHelper.TABLE_NAME, projection, selection, selectionArgs,
                    null, null, sortOrder
                )
            }
            else -> throw IllegalArgumentException("Cannot query unknown URI $uri")
        }
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(
        uri: Uri,
        contentValues: ContentValues?
    ): Uri? {
        return when (sUriMatcher.match(uri)) {
            PICTURES -> insertCart(uri, contentValues)
            else -> throw IllegalArgumentException("Insertion is not supported for $uri")
        }
    }

    private fun insertCart(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(
        uri: Uri,
        s: String?,
        strings: Array<String>?
    ): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        contentValues: ContentValues?,
        s: String?,
        strings: Array<String>?
    ): Int {
        return 0
    }
}