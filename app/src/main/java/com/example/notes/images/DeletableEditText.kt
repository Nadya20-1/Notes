package com.example.notes.images

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.widget.EditText

internal class DeletableEditText @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : EditText(context, attrs, defStyle) {
    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        return DeleteInputConnection(
            super.onCreateInputConnection(outAttrs),
            true
        )
    }

    private inner class DeleteInputConnection(target: InputConnection?, mutable: Boolean) : InputConnectionWrapper(target, mutable) {

        override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
            return if (beforeLength == 1 && afterLength == 0) {
                (sendKeyEvent(KeyEvent( KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DEL)
                )
                        && sendKeyEvent(KeyEvent( KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_DEL
                    )
                ))
            } else super.deleteSurroundingText(beforeLength, afterLength)
        }
    }
}