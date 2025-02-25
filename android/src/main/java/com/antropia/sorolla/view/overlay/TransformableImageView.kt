package com.antropia.sorolla.view.overlay

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import com.antropia.sorolla.mixin.RectHandler
import com.antropia.sorolla.mixin.ViewRenderer
import com.antropia.sorolla.util.RectAnchor
import com.antropia.sorolla.util.paddingHorizontal
import com.antropia.sorolla.util.paddingVertical

class TransformableImageView : AppCompatImageView, RectHandler, ViewRenderer {
  private var originalImageMatrix: Matrix = imageMatrix
  private var imageScale: Float = 1f

  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  fun setImage(uri: String, onFinish: () -> Unit) {
    val imageUri = Uri.parse(uri)
    setImageURI(imageUri)

    post { setupInitialImageScale(onFinish) }
  }

  fun saveToFile(clippingArea: RectF) = renderToFile(clippingArea)

  /**
   * Moves the image the given distances in the two axis.
   * This method translates the image matrix according to the given vector.
   */
  fun moveImage(dx: Float, dy: Float) {
    val targetMatrix = Matrix(imageMatrix)
    targetMatrix.postTranslate(dx, dy)

    imageMatrix = targetMatrix
  }


  /**
   * Moves the image within the boundaries of the cropping rect.
   * It inverts the image matrix and calculates the points of the bounding rect within the image.
   * If any of the points are out of the image (<0 or >width/height) then we move the image
   * accordingly to place it back within the given rectangle.
   */
  fun moveImageWithinBoundaries(croppingRect: RectF) {
    val drawableWidth = drawable.intrinsicWidth
    val drawableHeight = drawable.intrinsicHeight

    val inverseMatrix = Matrix()
    imageMatrix.invert(inverseMatrix)
    val targetMatrix = Matrix(imageMatrix)

    val normalizedCroppingRect = croppingRect.removePadding(this)
    inverseMatrix.mapRect(normalizedCroppingRect)

    if (normalizedCroppingRect.left < 0) {
      targetMatrix.postTranslate(imageScale * normalizedCroppingRect.left, 0f)
    }

    if (normalizedCroppingRect.top < 0) {
      targetMatrix.postTranslate(0f, imageScale * normalizedCroppingRect.top)
    }

    if (normalizedCroppingRect.right > drawableWidth) {
      targetMatrix.postTranslate(
        imageScale * (normalizedCroppingRect.right - drawableWidth),
        0f
      )
    }

    if (normalizedCroppingRect.bottom > drawableHeight) {
      targetMatrix.postTranslate(
        0f,
        imageScale * (normalizedCroppingRect.bottom - drawableHeight)
      )
    }

    animateImageMatrix(targetMatrix, duration = 200L)
  }

  fun refitImageToCrop(scale: Float, anchor: RectAnchor, fromRect: RectF, toRect: RectF) {
    val pivotLeft = fromRect.left - paddingLeft
    val pivotTop = fromRect.top - paddingTop
    val pivotRight = fromRect.right - paddingRight
    val pivotBottom = fromRect.bottom - paddingBottom
    val pivotCenter = PointF(
      (fromRect.left + fromRect.right) / 2f,
      (fromRect.top + fromRect.bottom) / 2f
    )

    val targetMatrix = Matrix(imageMatrix)

    val (pivot, translation) = when (anchor) {
      RectAnchor.LEFT -> PointF(
        pivotLeft,
        pivotCenter.y
      ) to PointF(
        toRect.left - fromRect.left,
        0f
      )

      RectAnchor.TOP -> PointF(
        pivotCenter.x,
        pivotTop
      ) to PointF(
        0f,
        toRect.top - fromRect.top
      )

      RectAnchor.RIGHT -> PointF(
        pivotRight,
        pivotCenter.y
      ) to PointF(
        toRect.right - fromRect.right,
        0f
      )

      RectAnchor.BOTTOM -> PointF(
        pivotCenter.x,
        pivotBottom
      ) to PointF(
        0f,
        toRect.bottom - fromRect.bottom
      )

      RectAnchor.TOP_LEFT -> PointF(
        pivotLeft,
        pivotTop
      ) to PointF(
        toRect.left - fromRect.left,
        toRect.top - fromRect.top
      )

      RectAnchor.TOP_RIGHT -> PointF(
        pivotRight,
        pivotTop
      ) to PointF(
        toRect.right - fromRect.right,
        toRect.top - fromRect.top
      )

      RectAnchor.BOTTOM_LEFT -> PointF(
        pivotLeft,
        pivotBottom
      ) to PointF(
        toRect.left - fromRect.left,
        toRect.bottom - fromRect.bottom
      )

      RectAnchor.BOTTOM_RIGHT -> PointF(
        pivotRight,
        pivotBottom
      ) to PointF(
        toRect.right - fromRect.right,
        toRect.bottom - fromRect.bottom
      )
    }

    targetMatrix.postScale(scale, scale, pivot.x, pivot.y)
    targetMatrix.postTranslate(translation.x, translation.y)

    this.imageScale *= scale
    animateImageMatrix(targetMatrix)
  }

  fun restoreTransforms() {
    animateImageMatrix(originalImageMatrix, duration = 500L)
  }

  /**
   * Positions the image inside the view by scaling it to the right size and centering it.
   */
  private fun setupInitialImageScale(onFinish: () -> Unit) {
    val viewWidth = width - paddingHorizontal
    val viewHeight = height - paddingVertical
    val drawableWidth = drawable.intrinsicWidth
    val drawableHeight = drawable.intrinsicHeight

    // Scale the original image to the ImageView size, respecting paddings
    val scale = minOf(
      viewWidth.toFloat() / drawableWidth,
      viewHeight.toFloat() / drawableHeight
    )

    // After the scale, move the image to center it in the image
    val dx = (viewWidth - drawableWidth * scale) / 2f
    val dy = (viewHeight - drawableHeight * scale) / 2f

    val matrix = Matrix()
    matrix.postScale(scale, scale)
    matrix.postTranslate(dx, dy)

    originalImageMatrix = matrix
    imageMatrix = matrix
    imageScale = scale

    onFinish()
  }

  private fun animateImageMatrix(targetMatrix: Matrix, duration: Long = 500L) {
    val currentMatrix = Matrix(imageMatrix)
    val startTime = System.currentTimeMillis()
    val interpolator = AccelerateDecelerateInterpolator()

    val matrixEvaluator = object : Runnable {
      override fun run() {
        val elapsed = System.currentTimeMillis() - startTime
        val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
        val interpolatedProgress = interpolator.getInterpolation(progress)

        val matrix = Matrix()
        val currentValues = FloatArray(9)
        val targetValues = FloatArray(9)
        currentMatrix.getValues(currentValues)
        targetMatrix.getValues(targetValues)

        val interpolatedValues = FloatArray(9)
        for (i in 0..8) {
          interpolatedValues[i] = currentValues[i] +
            (targetValues[i] - currentValues[i]) * interpolatedProgress
        }
        matrix.setValues(interpolatedValues)

        imageMatrix = matrix

        if (progress < 1f) {
          handler.postDelayed(this, 16)
        }
      }
    }

    handler.post(matrixEvaluator)
  }
}
