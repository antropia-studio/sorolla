package com.antropia.sorolla.mixin

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.view.View
import java.io.File
import java.io.FileOutputStream


interface ViewRenderer {
  fun View.renderToFile(clippingArea: RectF): String {
    val bitmap = toBitmap(clippingArea)

    val outputFile = File(context.cacheDir, "edited_" + System.currentTimeMillis() + ".jpg")
    val fos = FileOutputStream(outputFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
    fos.close()

    return "file://" + outputFile.absolutePath
  }

  private fun View.toBitmap(clippingArea: RectF): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    canvas.clipRect(clippingArea)
    draw(canvas)

    return bitmap
  }
}
