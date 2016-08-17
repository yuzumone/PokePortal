package net.yuzumone.pokeportal.util

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat

class ResourceUtil {

    companion object {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap {
            val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                    vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
            return bitmap
        }

        private fun getBitmap(vectorDrawable: VectorDrawableCompat): Bitmap {
            val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                    vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
            return bitmap
        }

        fun getBitmap(context: Context, @DrawableRes drawableResId: Int): Bitmap {
            val drawable = ContextCompat.getDrawable(context, drawableResId)
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            } else if (drawable is VectorDrawableCompat) {
                return getBitmap(drawable)
            } else if (drawable is VectorDrawable) {
                return getBitmap(drawable)
            } else {
                throw IllegalArgumentException("Unsupported drawable type")
            }
        }
    }
}