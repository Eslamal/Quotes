package com.example.qoutes.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.drawToBitmap
import java.lang.Exception

object ShareUtils {
    fun share(view: ViewGroup, context: Context) {

        view.background = null
        val viewBmp = view.drawToBitmap()

        val padding = 100
        val finalBmp = Bitmap.createBitmap(
            viewBmp.width + (padding * 2),
            viewBmp.height + (padding * 2),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(finalBmp)

        canvas.drawColor(Color.parseColor("#0F172A"))

        canvas.drawBitmap(viewBmp, padding.toFloat(), padding.toFloat(), null)

        val bmpPath = MediaStore.Images.Media.insertImage(
            context.contentResolver,
            finalBmp,
            "quote_${System.currentTimeMillis()}",
            null
        )
        val uri = Uri.parse(bmpPath)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpeg"

        try {
            intent.putExtra(Intent.EXTRA_STREAM, uri)

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Failed to share!\nPlease try again.",
                Toast.LENGTH_SHORT
            ).show()
        }

        context.startActivity(Intent.createChooser(intent, "Share via:"))
        viewBmp.recycle()
    }
}