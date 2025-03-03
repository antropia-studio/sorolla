package com.antropia.sorolla.mixin

import android.graphics.PointF
import android.graphics.RectF
import android.view.View
import com.antropia.sorolla.util.AspectRatio
import com.antropia.sorolla.util.Axis
import com.antropia.sorolla.util.RectAnchor

interface Geometer {
  operator fun PointF.timesAssign(scale: Float) {
    this.x *= scale
    this.y *= scale
  }

  operator fun PointF.timesAssign(other: PointF) {
    this.x *= other.x
    this.y *= other.y
  }

  val PointF.swapped: PointF get() = PointF(y, x)

  fun RectF.getAnchorPoint(anchor: RectAnchor): PointF {
    val centerX = (left + right) / 2f
    val centerY = (top + bottom) / 2f

    return when (anchor) {
      RectAnchor.LEFT -> PointF(left, centerY)
      RectAnchor.TOP -> PointF(centerX, top)
      RectAnchor.RIGHT -> PointF(right, centerY)
      RectAnchor.BOTTOM -> PointF(centerX, bottom)
      RectAnchor.TOP_LEFT -> PointF(left, top)
      RectAnchor.TOP_RIGHT -> PointF(right, top)
      RectAnchor.BOTTOM_LEFT -> PointF(left, bottom)
      RectAnchor.BOTTOM_RIGHT -> PointF(right, bottom)
    }
  }

  /**
   * Enlarges the Rect to fit a working area.
   * The resulting rect preserves the aspect ratio of the operated rect.
   */
  fun RectF.zoomToWorkingRect(workingRect: RectF): RectF {
    val aspectRatio = AspectRatio(width(), height())

    val leadingAxis = getLeadingAxisForZoom(workingRect)

    val center = PointF(workingRect.centerX(), workingRect.centerY())

    val zoomedWidth = aspectRatio.calculateWidth(workingRect.height())
    val zoomedHeight = aspectRatio.calculateHeight(workingRect.width())

    val zoomedLeft = center.x - zoomedWidth / 2f
    val zoomedRight = center.x + zoomedWidth / 2f
    val zoomedTop = center.y - zoomedHeight / 2f
    val zoomedBottom = center.y + zoomedHeight / 2f

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

  /**
   * Removes the padding from the given view and returns a new RectF.
   * This function does not mutate "this".
   */
  fun RectF.removePadding(view: View): RectF =
    RectF(this).apply {
      left -= view.paddingLeft
      top -= view.paddingTop
      right -= view.paddingRight
      bottom -= view.paddingBottom
    }

  /**
   * Interchanges vertex positions so that the rectangle changes its orientation (landscape <> portrait)
   */
  fun RectF.swapAxis(): RectF = RectF(top, left, bottom, right)
}
