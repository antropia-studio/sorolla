package com.antropia.sorolla.util

import android.graphics.PointF
import android.graphics.RectF

enum class Axis {
  HORIZONTAL,
  VERTICAL
}

interface RectHandler {
  fun RectF.getAnchorPoint(anchor: RectAnchor): PointF = when (anchor) {
    RectAnchor.TOP_LEFT -> PointF(left, top)
    RectAnchor.TOP_RIGHT -> PointF(right, top)
    RectAnchor.BOTTOM_LEFT -> PointF(left, bottom)
    RectAnchor.BOTTOM_RIGHT -> PointF(right, bottom)
  }

  /**
   * Enlarges the Rect to fit a working area.
   * The resulting rect preserves the aspect ratio of the operated rect.
   */
  fun RectF.zoomToWorkingRect(workingRect: RectF): RectF {
    val aspectRatio = AspectRatio(width(), height())

    val leadingAxis = getLeadingAxisForZoom(workingRect)

    val midX = workingRect.centerX()
    val midY = workingRect.centerY()

    val zoomedWidth = aspectRatio.calculateWidth(workingRect.height())
    val zoomedHeight = aspectRatio.calculateHeight(workingRect.width())

    val zoomedLeft = midX - zoomedWidth / 2f
    val zoomedRight = midX + zoomedWidth / 2f
    val zoomedTop = midY - zoomedHeight / 2f
    val zoomedBottom = midY + zoomedHeight / 2f

    return when (leadingAxis) {
      Axis.HORIZONTAL -> RectF(workingRect.left, zoomedTop, workingRect.right, zoomedBottom)
      Axis.VERTICAL -> RectF(zoomedLeft, workingRect.top, zoomedRight, workingRect.bottom)
    }
  }

  /**
   * Returns the leading axis if zoomed in.
   * The leading axis is the one that will restrict how much we can zoom in the target rect to fit
   * inside the working area.
   */
  fun RectF.getLeadingAxisForZoom(workingRect: RectF): Axis {
    /**
     * Here we decide what's the "leading" axis. We calculate the horizontal zooming ratio
     * and calculate what would the new height be if we zoom in that much. If the new height fits
     * inside the working area then the leading axis is X, otherwise is Y
     */
    val horizontalZoomRatio = workingRect.width() / this.width()

    return if (this.height() * horizontalZoomRatio < workingRect.height())
      Axis.HORIZONTAL
    else
      Axis.VERTICAL
  }
}
