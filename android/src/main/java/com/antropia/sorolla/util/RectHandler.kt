package com.antropia.sorolla.util

import android.graphics.RectF


interface RectHandler {
  /**
   * Enlarges the Rect to fit a working area.
   * The resulting rect preserves the aspect ratio of the operated rect.
   */
  fun RectF.zoomToWorkingRect(workingRect: RectF): RectF {
    /**
     * Just a reminder for future readers: aspectRatio = W / H
     * That means if we want to calculate the new width or height of a rect, use the following
     * formulas:
     * W' = H' * aspectRatio
     * H' = W' / aspectRatio
     */
    val aspectRatio = this.width() / this.height()

    /**
     * Here we decide what's the "leading" axis. We calculate the horizontal zooming ratio
     * and calculate what would the new height be if we zoom in that much. If the new height fits
     * inside the working area then the leading axis is X, otherwise is Y
     */
    val horizontalZoomRatio = workingRect.width() / this.width()
    val isLeadingHorizontalAxis = this.height() * horizontalZoomRatio < workingRect.height()

    val midX = workingRect.centerX()
    val midY = workingRect.centerY()

    val adaptedLeft = midX - (workingRect.height() * aspectRatio) / 2f
    val adaptedRight = midX + (workingRect.height() * aspectRatio) / 2f
    val adaptedTop = midY - (workingRect.width() / aspectRatio) / 2f
    val adaptedBottom = midY + (workingRect.width() / aspectRatio) / 2f

    return if (isLeadingHorizontalAxis)
      RectF(workingRect.left, adaptedTop, workingRect.right, adaptedBottom)
    else
      RectF(adaptedLeft, workingRect.top, adaptedRight, workingRect.bottom)
  }
}
