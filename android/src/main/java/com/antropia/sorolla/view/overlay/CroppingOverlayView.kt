package com.antropia.sorolla.view.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import com.antropia.sorolla.util.Interpolator
import com.antropia.sorolla.util.RectHandler
import com.antropia.sorolla.util.paddingHorizontal
import com.antropia.sorolla.util.paddingVertical
import com.facebook.react.uimanager.PixelUtil.dpToPx
import kotlin.math.abs
import kotlin.math.min

private val CORNER_LENGTH = 25.dpToPx()
private val TOUCH_AREA = 48.dpToPx()

sealed interface AnimationState {
  data object Idle : AnimationState
  data class Running(var rect: RectF) : AnimationState
}

class CroppingOverlayView : View, RectHandler, Interpolator {
  private var imageRect: RectF? = null
  private var cropRect: RectF? = null
  private var activeCorner: Corner? = null
  private var lastTouchX = 0f
  private var lastTouchY = 0f
  private var animationState: AnimationState = AnimationState.Idle
  private var onCropChangeListener: ((RectF) -> Unit)? = null
  private val cropUpdateHandler = Handler(Looper.getMainLooper())
  private var pendingCropUpdate: Runnable? = null
  private val renderer = Renderer(this)

  enum class Corner {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
  }

  constructor(context: Context?) : super(context)

  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  fun setOnCropChangeListener(listener: (RectF) -> Unit) {
    onCropChangeListener = listener
  }

  fun setImageView(imageView: ImageView) {
    if (imageView.drawable == null) return

    val imageWidth = imageView.drawable.intrinsicWidth.toFloat()
    val imageHeight = imageView.drawable.intrinsicHeight.toFloat()

    val availableWidth =
      (imageView.width - imageView.paddingHorizontal).toFloat()
    val availableHeight =
      (imageView.height - imageView.paddingVertical).toFloat()

    val scale = min(
      availableWidth / imageWidth,
      availableHeight / imageHeight
    )

    val scaledWidth = imageWidth * scale
    val scaledHeight = imageHeight * scale

    val left = imageView.paddingLeft + (availableWidth - scaledWidth) / 2
    val top = imageView.paddingTop + (availableHeight - scaledHeight) / 2

    imageRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
    cropRect = RectF(imageRect)
    invalidate()
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (cropRect == null) return false

    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        lastTouchX = event.x
        lastTouchY = event.y
        activeCorner = detectTouchedCorner(event.x, event.y)
        return activeCorner != null
      }

      MotionEvent.ACTION_MOVE -> {
        if (activeCorner != null) {
          val dx = event.x - lastTouchX
          val dy = event.y - lastTouchY
          updateCropRect(dx, dy)
          lastTouchX = event.x
          lastTouchY = event.y
          invalidate()
          notifyCropChange()
          return true
        }
      }

      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        activeCorner = null
      }
    }

    return super.onTouchEvent(event)
  }

  private fun detectTouchedCorner(x: Float, y: Float): Corner? {
    cropRect?.let { rect ->
      val touchArea = TOUCH_AREA / 2

      return when {
        isInTouchArea(x, y, rect.left, rect.top, touchArea) -> Corner.TOP_LEFT
        isInTouchArea(x, y, rect.right, rect.top, touchArea) -> Corner.TOP_RIGHT
        isInTouchArea(x, y, rect.left, rect.bottom, touchArea) -> Corner.BOTTOM_LEFT
        isInTouchArea(x, y, rect.right, rect.bottom, touchArea) -> Corner.BOTTOM_RIGHT
        else -> null
      }
    }
    return null
  }

  private fun isInTouchArea(
    touchX: Float,
    touchY: Float,
    cornerX: Float,
    cornerY: Float,
    touchArea: Float
  ): Boolean = abs(touchX - cornerX) <= touchArea && abs(touchY - cornerY) <= touchArea

  private fun updateCropRect(dx: Float, dy: Float) {
    val rect = cropRect ?: return
    val imageR = imageRect ?: return

    when (activeCorner) {
      Corner.TOP_LEFT -> {
        rect.left = (rect.left + dx).coerceIn(imageR.left, rect.right - CORNER_LENGTH)
        rect.top = (rect.top + dy).coerceIn(imageR.top, rect.bottom - CORNER_LENGTH)
      }

      Corner.TOP_RIGHT -> {
        rect.right = (rect.right + dx).coerceIn(rect.left + CORNER_LENGTH, imageR.right)
        rect.top = (rect.top + dy).coerceIn(imageR.top, rect.bottom - CORNER_LENGTH)
      }

      Corner.BOTTOM_LEFT -> {
        rect.left = (rect.left + dx).coerceIn(imageR.left, rect.right - CORNER_LENGTH)
        rect.bottom = (rect.bottom + dy).coerceIn(rect.top + CORNER_LENGTH, imageR.bottom)
      }

      Corner.BOTTOM_RIGHT -> {
        rect.right = (rect.right + dx).coerceIn(rect.left + CORNER_LENGTH, imageR.right)
        rect.bottom = (rect.bottom + dy).coerceIn(rect.top + CORNER_LENGTH, imageR.bottom)
      }

      null -> {}
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    val cRect = cropRect ?: return
    val rect = when (val state = animationState) {
      is AnimationState.Idle -> cRect
      is AnimationState.Running -> state.rect
    }

    renderer.render(canvas, rect)
  }

  private fun notifyCropChange() {
    pendingCropUpdate?.let { cropUpdateHandler.removeCallbacks(it) }

    pendingCropUpdate = Runnable {
      val cropRect = getCropRect() ?: return@Runnable

      onCropChangeListener?.invoke(cropRect)
      animateCropRect()
    }

    cropUpdateHandler.postDelayed(pendingCropUpdate!!, 500)
  }

  /**
   * Returns the cropping rectangle in normalized values (ranging from 0 to 1) from the original
   * image rectangle. A cropping rectangle that fits the original image would be:
   * { left: 0, top: 0, right: 1, bottom: 1 }
   */
  private fun getCropRect(): RectF? {
    val cRect = cropRect ?: return null
    val iRect = imageRect ?: return null

    return RectF(
      (cRect.left - iRect.left) / iRect.width(),
      (cRect.top - iRect.top) / iRect.height(),
      (cRect.right - iRect.left) / iRect.width(),
      (cRect.bottom - iRect.top) / iRect.height()
    )
  }

  private fun animateCropRect() {
    val cRect = cropRect ?: return
    val iRect = imageRect ?: return

    val targetRect = cRect.zoomToWorkingRect(iRect)
    cropRect = RectF(targetRect)

    val animator = ValueAnimator.ofFloat(0f, 1f)
    animator.addUpdateListener {
      val value = it.animatedValue as Float

      when (val state = animationState) {
        is AnimationState.Running ->
          state.rect = RectF(
            value.lerp(cRect.left, targetRect.left),
            value.lerp(cRect.top, targetRect.top),
            value.lerp(cRect.right, targetRect.right),
            value.lerp(cRect.bottom, targetRect.bottom),
          )

        else -> {}
      }

      postInvalidateOnAnimation()
    }

    animator.doOnEnd { animationState = AnimationState.Idle }
    animator.interpolator = AccelerateDecelerateInterpolator()
    animator.duration = 500
    animator.start()

    animationState = AnimationState.Running(rect = RectF(cRect))
  }

}
