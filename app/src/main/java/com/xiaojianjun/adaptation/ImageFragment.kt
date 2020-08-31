package com.xiaojianjun.adaptation

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.dialog_image.*

/**
 * Created by xiaojianjun on 2020/8/30.
 */
class ImageFragment : DialogFragment() {
    private var bitmap: Bitmap? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setOnClickListener { dismissAllowingStateLoss() }
        imageView.setImageBitmap(bitmap ?: return)
    }

    fun show(manager: FragmentManager, bitmap: Bitmap) {
        this.bitmap = bitmap
        try {
            super.show(manager, "ImageFragment")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}