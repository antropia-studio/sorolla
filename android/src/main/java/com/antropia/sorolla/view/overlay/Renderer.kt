package com.antropia.sorolla.view.overlay

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View
import com.facebook.react.uimanager.PixelUtil.dpToPx

private const val GRID_COLOR = Color.WHITE
private const val BG_COLOR = 0x66000000
private val OUTER_LINES_STROKE_WIDTH = 1.dpToPx()
private val INNER_LINES_STROKE_WIDTH = 0.5.dpToPx()
private val CORNER_STROKE_WIDTH = 4.dpToPx()
private val CORNER_STROKE_HALF_WIDTH = CORNER_STROKE_WIDTH / 2f
private val CORNER_LENGTH = 25.dpToPx()
private val EDGE_LENGTH = 25.dpToPx()

class Renderer(private val view: View) {
  private val paint = Paint().apply {
    isAntiAlias = true
    style = Paint.Style.STROKE
  }

  fun render(canvas: Canvas, rect: RectF) {
    drawBackground(canvas, rect)
    drawHorizontalLines(canvas, rect)
    drawVerticalLines(canvas, rect)
    drawAnchors(canvas, rect)
  }

  private fun drawAnchors(
    canvas: Canvas,
    rect: RectF,
  ) {
    paint.style = Paint.Style.STROKE
    paint.color = GRID_COLOR
    paint.strokeWidth = CORNER_STROKE_WIDTH

    /**
     * We account for the stroke width so that the lines drawn here are always
     * out of the image boundaries
     */
    val left = rect.left - CORNER_STROKE_HALF_WIDTH
    val top = rect.top - CORNER_STROKE_HALF_WIDTH
    val right = rect.right + CORNER_STROKE_HALF_WIDTH
    val bottom = rect.bottom + CORNER_STROKE_HALF_WIDTH

    val cornersPath = Path().apply {
      // Top-left corner: ⌜
      moveTo(left, top + CORNER_LENGTH)
      lineTo(left, top)
      lineTo(left + CORNER_LENGTH, top)

      // Top-right corner: ⌝
      moveTo(right, top + CORNER_LENGTH)
      lineTo(right, top)
      lineTo(right - CORNER_LENGTH, top)

      // Bottom-left corner: ⌞
      moveTo(left, bottom - CORNER_LENGTH)
      lineTo(left, bottom)
      lineTo(left + CORNER_LENGTH, bottom)

      // Bottom-right corner: ⌟
      moveTo(right, bottom - CORNER_LENGTH)
      lineTo(right, bottom)
      lineTo(right - CORNER_LENGTH, bottom)
    }

    canvas.drawPath(cornersPath, paint)

    val midX = (rect.left + rect.right) / 2f
    val midY = (rect.top + rect.bottom) / 2f

    val edgesPath = Path().apply {
      // Left edge
      moveTo(left, midY - EDGE_LENGTH / 2)
      lineTo(left, midY + EDGE_LENGTH / 2)

      // Top edge
      moveTo(midX - EDGE_LENGTH / 2, top)
      lineTo(midX + EDGE_LENGTH / 2, top)

      // Right edge
      moveTo(right, midY - EDGE_LENGTH / 2)
      lineTo(right, midY + EDGE_LENGTH / 2)

      // Bottom edge
      moveTo(midX - EDGE_LENGTH / 2, bottom)
      lineTo(midX + EDGE_LENGTH / 2, bottom)
    }

    canvas.drawPath(edgesPath, paint)
  }

  private fun drawVerticalLines(
    canvas: Canvas,
    rect: RectF
  ) {
    paint.style = Paint.Style.STROKE
    paint.color = GRID_COLOR

    val slotWidth = rect.width() / 3
    for (i in 0..3) {
      paint.strokeWidth =
        if (i == 0 || i == 3) OUTER_LINES_STROKE_WIDTH else INNER_LINES_STROKE_WIDTH

      val x = rect.left + (slotWidth * i)
      canvas.drawLine(x, rect.top, x, rect.bottom, paint)
    }
  }

  private fun drawHorizontalLines(
    canvas: Canvas,
    rect: RectF
  ) {
    paint.style = Paint.Style.STROKE
    paint.color = GRID_COLOR

    val slotHeight = rect.height() / 3
    for (i in 0..3) {
      paint.strokeWidth =
        if (i == 0 || i == 3) OUTER_LINES_STROKE_WIDTH else INNER_LINES_STROKE_WIDTH

      val y = rect.top + (slotHeight * i)
      canvas.drawLine(rect.left, y, rect.right, y, paint)
    }
  }

  private fun drawBackground(
    canvas: Canvas,
    rect: RectF
  ) {
    paint.style = Paint.Style.FILL
    paint.color = BG_COLOR

    canvas.drawRect(0f, 0f, view.right.toFloat(), rect.top, paint)
    canvas.drawRect(0f, rect.top, rect.left, rect.bottom, paint)
    canvas.drawRect(rect.right, rect.top, view.right.toFloat(), rect.bottom, paint)
    canvas.drawRect(0f, rect.bottom, view.right.toFloat(), view.bottom.toFloat(), paint)
  }
}
