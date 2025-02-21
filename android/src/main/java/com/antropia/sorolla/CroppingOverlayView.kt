package com.antropia.sorolla

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import com.facebook.react.uimanager.PixelUtil.dpToPx
import kotlin.math.abs
import kotlin.math.min

const val GRID_COLOR = Color.WHITE
const val BG_COLOR = 0x66000000
val OUTER_LINES_STROKE_WIDTH = 1.dpToPx()
val INNER_LINES_STROKE_WIDTH = 0.5.dpToPx()
val CORNER_STROKE_WIDTH = 4.dpToPx()
val CORNER_STROKE_HALF_WIDTH = CORNER_STROKE_WIDTH / 2f
val CORNER_LENGTH = 25.dpToPx()
val TOUCH_AREA = 48.dpToPx()

sealed interface AnimationState {
  data object Idle : AnimationState
  data class Running(var rect: RectF) : AnimationState
}

class CroppingOverlayView : View {
  private val paint = Paint().apply {
    isAntiAlias = true
    style = Paint.Style.STROKE
  }
  private var imageRect: RectF? = null
  private var cropRect: RectF? = null
  private var activeCorner: Corner? = null
  private var lastTouchX = 0f
  private var lastTouchY = 0f
  private var animationState: AnimationState = AnimationState.Idle
  private var onCropChangeListener: ((RectF) -> Unit)? = null
  private val cropUpdateHandler = Handler(Looper.getMainLooper())
  private var pendingCropUpdate: Runnable? = null

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
      (imageView.width - imageView.paddingLeft - imageView.paddingRight).toFloat()
    val availableHeight =
      (imageView.height - imageView.paddingTop - imageView.paddingBottom).toFloat()

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

      // Check each corner
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
  ): Boolean {
    return abs(touchX - cornerX) <= touchArea && abs(touchY - cornerY) <= touchArea
  }

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
    val animatedRect = when (val state = animationState) {
      is AnimationState.Idle -> cRect
      is AnimationState.Running -> state.rect
    }

    drawBackground(animatedRect, canvas)
    drawHorizontalLines(animatedRect, canvas)
    drawVerticalLines(animatedRect, canvas)
    drawCorners(animatedRect, canvas)
  }

  private fun drawCorners(
    rect: RectF,
    canvas: Canvas
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

    val topLeftCornerPath = Path().apply {
      moveTo(left, top + CORNER_LENGTH)
      lineTo(left, top)
      lineTo(left + CORNER_LENGTH, top)
    }
    canvas.drawPath(topLeftCornerPath, paint)

    val topRightCornerPath = Path().apply {
      moveTo(right, top + CORNER_LENGTH)
      lineTo(right, top)
      lineTo(right - CORNER_LENGTH, top)
    }
    canvas.drawPath(topRightCornerPath, paint)

    val bottomLeftCornerPath = Path().apply {
      moveTo(left, bottom - CORNER_LENGTH)
      lineTo(left, bottom)
      lineTo(left + CORNER_LENGTH, bottom)
    }
    canvas.drawPath(bottomLeftCornerPath, paint)

    val bottomRightCornerPath = Path().apply {
      moveTo(right, bottom - CORNER_LENGTH)
      lineTo(right, bottom)
      lineTo(right - CORNER_LENGTH, bottom)
    }
    canvas.drawPath(bottomRightCornerPath, paint)
  }

  private fun drawVerticalLines(
    rect: RectF,
    canvas: Canvas
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
    rect: RectF,
    canvas: Canvas
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
    rect: RectF,
    canvas: Canvas
  ) {
    paint.style = Paint.Style.FILL
    paint.color = BG_COLOR

    canvas.drawRect(0f, 0f, right.toFloat(), rect.top, paint)
    canvas.drawRect(0f, rect.top, rect.left, rect.bottom, paint)
    canvas.drawRect(rect.right, rect.top, right.toFloat(), rect.bottom, paint)
    canvas.drawRect(0f, rect.bottom, right.toFloat(), bottom.toFloat(), paint)
  }

  private fun notifyCropChange() {
    pendingCropUpdate?.let { cropUpdateHandler.removeCallbacks(it) }

    pendingCropUpdate = Runnable {
      val cropRect = getCropRect() ?: return@Runnable

      animateOverlayScale()
      onCropChangeListener?.invoke(cropRect)
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

  private fun animateOverlayScale() {
    val cRect = cropRect ?: return
    val iRect = imageRect ?: return

    cropRect = RectF(imageRect)
    /**
     * [aspectRatio = W / H]
     * W' = H' * aspectRatio
     * H' = W' / aspectRatio
     */
    val aspectRatio = cRect.width() / cRect.height()

    /**
     * The new overlay has a wider aspectRatio if the new height transformed fits
     * inside the image rect
     */
    val isWider = cRect.height() * (iRect.width() / cRect.width()) < iRect.height()

    val midX = iRect.centerX()
    val midY = iRect.centerY()

    val adaptedLeft = midX - (iRect.height() * aspectRatio) / 2f
    val adaptedRight = midX + (iRect.height() * aspectRatio) / 2f
    val adaptedTop = midY - (iRect.width() / aspectRatio) / 2f
    val adaptedBottom = midY + (iRect.width() / aspectRatio) / 2f

    val targetRect =
      if (isWider)
        RectF(iRect.left, adaptedTop, iRect.right, adaptedBottom)
      else
        RectF(adaptedLeft, iRect.top, adaptedRight, iRect.bottom)

    val scaleAnimator = ValueAnimator.ofFloat(0f, 1f)
    scaleAnimator.addUpdateListener {
      val value = it.animatedValue as Float

      when (val state = animationState) {
        is AnimationState.Running ->
          state.rect = RectF(
            lerp(value, cRect.left, targetRect.left),
            lerp(value, cRect.top, targetRect.top),
            lerp(value, cRect.right, targetRect.right),
            lerp(value, cRect.bottom, targetRect.bottom),
          )

        else -> {}
      }

      postInvalidateOnAnimation()
    }

    scaleAnimator.doOnEnd {
      cropRect = targetRect
      animationState = AnimationState.Idle
    }
    scaleAnimator.interpolator = AccelerateDecelerateInterpolator()
    scaleAnimator.duration = 500
    scaleAnimator.start()

    animationState = AnimationState.Running(rect = RectF(cRect))
  }

  private fun lerp(value: Float, start: Float, end: Float) =
    start + (end - start) * value
}
