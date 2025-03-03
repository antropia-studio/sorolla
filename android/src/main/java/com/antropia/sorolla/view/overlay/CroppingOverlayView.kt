package com.antropia.sorolla.view.overlay

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.view.marginLeft
import androidx.core.view.marginTop
import com.antropia.sorolla.mixin.Geometer
import com.antropia.sorolla.mixin.Interpolator
import com.antropia.sorolla.util.RectAnchor
import com.antropia.sorolla.util.marginHorizontal
import com.antropia.sorolla.util.marginVertical
import com.antropia.sorolla.util.paddingHorizontal
import com.antropia.sorolla.util.paddingVertical
import com.facebook.react.uimanager.PixelUtil.dpToPx
import kotlin.math.abs
import kotlin.math.min


private val CORNER_LENGTH = 25.dpToPx()
private val CROP_TOUCH_AREA = 48.dpToPx()
private val MOVE_TOUCH_AREA = 128.dpToPx()

sealed interface AnimationState {
  data object Idle : AnimationState
  data class Running(var rect: RectF) : AnimationState
}

interface OnCropAreaChangeListener {
  fun onScale(scale: Float, anchor: RectAnchor, fromRect: RectF, toRect: RectF)
  fun onMove(dx: Float, dy: Float)
  fun onMoveFinish(croppingRect: RectF)
}

data class RotateResult(val scale: Float, val fromRect: RectF, val toRect: RectF)

class CroppingOverlayView : View, Geometer, Interpolator {
  private var workingRect: RectF? = null
  private var imageRect: RectF? = null
  private var originalRect: RectF? = null
  private var activeAnchor: RectAnchor? = null
  private var activeMove: Boolean = false
  private var lastTouchX = 0f
  private var lastTouchY = 0f
  private var animationState: AnimationState = AnimationState.Idle
  private var onCropAreaChangeListener: OnCropAreaChangeListener? = null
  private val cropUpdateHandler = Handler(Looper.getMainLooper())
  private var pendingCropUpdate: Runnable? = null
  private val renderer = Renderer(this)
  var overlayColor: Int = 0x66000000
  var cropRect: RectF? = null

  constructor(context: Context?) : super(context)
  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  fun setOnCropAreaChangeListener(listener: OnCropAreaChangeListener) {
    onCropAreaChangeListener = listener
  }

  fun setImageView(imageView: ImageView) {
    if (imageView.drawable == null) return

    val imageWidth = imageView.drawable.intrinsicWidth.toFloat()
    val imageHeight = imageView.drawable.intrinsicHeight.toFloat()

    val availableWidth =
      (imageView.width - imageView.paddingHorizontal - imageView.marginHorizontal).toFloat()
    val availableHeight =
      (imageView.height - imageView.paddingVertical - imageView.marginVertical).toFloat()

    val scale = min(
      availableWidth / imageWidth,
      availableHeight / imageHeight
    )

    val scaledWidth = imageWidth * scale
    val scaledHeight = imageHeight * scale

    val left = imageView.marginLeft + imageView.paddingLeft + (availableWidth - scaledWidth) / 2
    val top = imageView.marginTop + imageView.paddingTop + (availableHeight - scaledHeight) / 2

    workingRect = RectF(
      imageView.marginLeft + imageView.paddingLeft.toFloat(),
      imageView.marginTop + imageView.paddingTop.toFloat(),
      availableWidth + imageView.marginLeft + imageView.paddingLeft.toFloat(),
      availableHeight + imageView.marginTop + imageView.paddingTop.toFloat()
    )
    imageRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
    cropRect = RectF(imageRect)
    originalRect = RectF(imageRect)

    invalidate()
  }

  fun rotateCcw(): RotateResult? {
    val cRect = cropRect ?: return null
    val wRect = workingRect ?: return null

    val targetRect = cRect.swapAxis().zoomToWorkingRect(wRect)
    animateCropRect(cRect, targetRect)

    return RotateResult(
      scale = targetRect.width() / cRect.height(),
      fromRect = cRect,
      toRect = targetRect
    )
  }

  fun restoreOverlay() {
    val cRect = cropRect ?: return
    val oRect = originalRect ?: return

    animateCropRect(cRect, oRect)
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (cropRect == null) return false

    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        lastTouchX = event.x
        lastTouchY = event.y
        activeAnchor = detectTouchedAnchor(event.x, event.y)
        activeMove = detectMoveAction(event.x, event.y)

        return activeAnchor != null || activeMove
      }

      MotionEvent.ACTION_MOVE -> {
        val dx = event.x - lastTouchX
        val dy = event.y - lastTouchY

        if (activeAnchor != null) {
          scaleCropRect(dx, dy)
          lastTouchX = event.x
          lastTouchY = event.y
          invalidate()
          return true
        } else if (activeMove) {
          notify({ onCropAreaChangeListener?.onMove(dx, dy) })
          lastTouchX = event.x
          lastTouchY = event.y
          return true
        }
      }

      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        activeAnchor?.let {
          notifyCropChange(anchor = it)
        }

        if (activeMove) {
          notify({ onCropAreaChangeListener?.onMoveFinish(cropRect ?: RectF()) })
        }

        activeAnchor = null
        activeMove = false
      }
    }

    return super.onTouchEvent(event)
  }

  private fun detectTouchedAnchor(x: Float, y: Float): RectAnchor? {
    val cRect = cropRect ?: return null

    val touchArea = CROP_TOUCH_AREA / 2
    val center = PointF((cRect.left + cRect.right) / 2f, (cRect.top + cRect.bottom) / 2f)

    return when {
      isInTouchArea(x, y, cRect.left, center.y, touchArea) -> RectAnchor.LEFT
      isInTouchArea(x, y, center.x, cRect.top, touchArea) -> RectAnchor.TOP
      isInTouchArea(x, y, cRect.right, center.y, touchArea) -> RectAnchor.RIGHT
      isInTouchArea(x, y, center.x, cRect.bottom, touchArea) -> RectAnchor.BOTTOM
      isInTouchArea(x, y, cRect.left, cRect.top, touchArea) -> RectAnchor.TOP_LEFT
      isInTouchArea(x, y, cRect.right, cRect.top, touchArea) -> RectAnchor.TOP_RIGHT
      isInTouchArea(x, y, cRect.left, cRect.bottom, touchArea) -> RectAnchor.BOTTOM_LEFT
      isInTouchArea(x, y, cRect.right, cRect.bottom, touchArea) -> RectAnchor.BOTTOM_RIGHT
      else -> null
    }
  }

  private fun detectMoveAction(x: Float, y: Float): Boolean {
    val cRect = cropRect ?: return false

    val touchArea = MOVE_TOUCH_AREA / 2
    val center = PointF((cRect.left + cRect.right) / 2f, (cRect.top + cRect.bottom) / 2f)

    return isInTouchArea(x, y, center.x, center.y, touchArea)
  }

  private fun isInTouchArea(
    touchX: Float,
    touchY: Float,
    anchorX: Float,
    anchorY: Float,
    touchArea: Float
  ): Boolean = abs(touchX - anchorX) <= touchArea && abs(touchY - anchorY) <= touchArea

  private fun scaleCropRect(dx: Float, dy: Float) {
    val cRect = cropRect ?: return
    val iRect = imageRect ?: return

    when (activeAnchor) {
      RectAnchor.LEFT -> {
        cRect.left = (cRect.left + dx).coerceIn(iRect.left, cRect.right - CORNER_LENGTH)
      }

      RectAnchor.TOP -> {
        cRect.top = (cRect.top + dy).coerceIn(iRect.top, cRect.bottom - CORNER_LENGTH)
      }

      RectAnchor.RIGHT -> {
        cRect.right = (cRect.right + dx).coerceIn(cRect.left + CORNER_LENGTH, iRect.right)
      }

      RectAnchor.BOTTOM -> {
        cRect.bottom = (cRect.bottom + dy).coerceIn(cRect.top + CORNER_LENGTH, iRect.bottom)
      }

      RectAnchor.TOP_LEFT -> {
        cRect.left = (cRect.left + dx).coerceIn(iRect.left, cRect.right - CORNER_LENGTH)
        cRect.top = (cRect.top + dy).coerceIn(iRect.top, cRect.bottom - CORNER_LENGTH)
      }

      RectAnchor.TOP_RIGHT -> {
        cRect.right = (cRect.right + dx).coerceIn(cRect.left + CORNER_LENGTH, iRect.right)
        cRect.top = (cRect.top + dy).coerceIn(iRect.top, cRect.bottom - CORNER_LENGTH)
      }

      RectAnchor.BOTTOM_LEFT -> {
        cRect.left = (cRect.left + dx).coerceIn(iRect.left, cRect.right - CORNER_LENGTH)
        cRect.bottom = (cRect.bottom + dy).coerceIn(cRect.top + CORNER_LENGTH, iRect.bottom)
      }

      RectAnchor.BOTTOM_RIGHT -> {
        cRect.right = (cRect.right + dx).coerceIn(cRect.left + CORNER_LENGTH, iRect.right)
        cRect.bottom = (cRect.bottom + dy).coerceIn(cRect.top + CORNER_LENGTH, iRect.bottom)
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

    renderer.render(canvas, rect, overlayColor)
  }

  private fun notifyCropChange(anchor: RectAnchor) {
    notify({
      val wRect = workingRect ?: return@notify
      val cRect = cropRect ?: return@notify

      val targetRect = cRect.zoomToWorkingRect(wRect)
      val scale = minOf(wRect.width() / cRect.width(), wRect.height() / cRect.height())

      onCropAreaChangeListener?.onScale(
        scale,
        anchor = anchor.opposite,
        fromRect = cRect,
        toRect = targetRect
      )

      animateCropRect(startingRect = cRect, targetRect = targetRect)
    }, delay = 100L)
  }

  private fun animateCropRect(startingRect: RectF, targetRect: RectF) {
    cropRect = RectF(targetRect)

    val animator = ValueAnimator.ofFloat(0f, 1f)
    animator.addUpdateListener {
      val value = it.animatedValue as Float

      when (val state = animationState) {
        is AnimationState.Running ->
          state.rect = RectF(
            value.lerp(startingRect.left, targetRect.left),
            value.lerp(startingRect.top, targetRect.top),
            value.lerp(startingRect.right, targetRect.right),
            value.lerp(startingRect.bottom, targetRect.bottom),
          )

        else -> {}
      }

      postInvalidateOnAnimation()
    }

    animator.doOnEnd { animationState = AnimationState.Idle }
    animator.interpolator = AccelerateDecelerateInterpolator()
    animator.duration = 500
    animator.start()

    animationState = AnimationState.Running(rect = RectF(startingRect))
  }

  private fun notify(block: () -> Unit, delay: Long = 0L) {
    pendingCropUpdate?.let { cropUpdateHandler.removeCallbacks(it) }

    pendingCropUpdate = Runnable { block() }

    if (delay > 0) {
      cropUpdateHandler.postDelayed(pendingCropUpdate!!, delay)
    } else {
      cropUpdateHandler.post(pendingCropUpdate!!)
    }
  }

}
