package com.example.notes.utils

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.inputmethod.InputMethodManager
import com.example.notes.Constant
import com.example.notes.R
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.internal.entity.CaptureStrategy

object DeviceUtils {
    fun hideSoftKeyboard(activity: Activity) {
        val view = activity.currentFocus
        if (view != null) {
            val inputManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }

    fun callGallery(context: Context) {
        Matisse.from(context as Activity)
            .choose(MimeType.allOf())
            .countable(true)
            .captureStrategy(CaptureStrategy(true, "notes"))
            .maxSelectable(5)
            .gridExpectedSize(context.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
            .thumbnailScale(0.85f)
            .imageEngine(GlideEngine())
            .theme(R.style.Matisse_Zhihu)
            .forResult(Constant.REQUEST_CODE_CHOOSE)
    }
}