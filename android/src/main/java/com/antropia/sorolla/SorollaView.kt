package com.antropia.sorolla

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import com.antropia.sorolla.util.RectAnchor
import com.antropia.sorolla.util.paddingHorizontal
import com.antropia.sorolla.util.paddingVertical
import com.antropia.sorolla.view.overlay.CroppingOverlayView

class SorollaView : LinearLayout {
  private val gestureExclusionRect = Rect()
  private val imageView: ImageView
  private val croppingOverlayView: CroppingOverlayView
  private var originalImageScale: Float = 1f
  private var originalMatrix: Matrix? = null
  private val handler = Handler(Looper.getMainLooper())
  private var pendingCropUpdate: Runnable? = null

  constructor(context: Context?) : super(context)
  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  init {
    LayoutInflater.from(context).inflate(R.layout.sorolla, this, true)
    imageView = findViewById(R.id.image_view)
    croppingOverlayView = findViewById(R.id.cropping_overlay)
    setupCropListener()
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    super.onLayout(changed, l, t, r, b)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      gestureExclusionRect.set(l, t, r, b)
      systemGestureExclusionRects = listOf(gestureExclusionRect)
    }
  }

  private fun setupCropListener() {
    croppingOverlayView.setOnCropChangeListener { scale, anchor, fromRect, toRect ->
      pendingCropUpdate?.let { handler.removeCallbacks(it) }

      pendingCropUpdate = Runnable {
        refitImageToCrop(scale, anchor, fromRect, toRect)
      }.also {
        handler.post(it)
      }
    }
  }

  fun setImage(uri: String) {
    val imageUri = Uri.parse(uri)
    imageView.setImageURI(imageUri)

    imageView.post {
      setupInitialImageScale()
    }
  }

  private fun setupInitialImageScale() {
    val drawable = imageView.drawable ?: return

    val viewWidth = imageView.width - imageView.paddingHorizontal
    val viewHeight = imageView.height - imageView.paddingVertical
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

    imageView.imageMatrix = matrix
    originalImageScale = scale
    originalMatrix = Matrix(matrix)

    croppingOverlayView.setImageView(imageView)
  }

  private fun refitImageToCrop(scale: Float, anchor: RectAnchor, fromRect: RectF, toRect: RectF) {
    val originalMatrix = originalMatrix ?: return

    val pivotLeft = fromRect.left - imageView.paddingLeft
    val pivotTop = fromRect.top - imageView.paddingTop
    val pivotRight = fromRect.right - imageView.paddingRight
    val pivotBottom = fromRect.bottom - imageView.paddingBottom
    val pivotCenter = PointF(
      (fromRect.left + fromRect.right) / 2f,
      (fromRect.top + fromRect.bottom) / 2f
    )

    val targetMatrix = Matrix(originalMatrix)

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

    this.originalMatrix = targetMatrix

    animateImageMatrix(targetMatrix)
  }

  private fun animateImageMatrix(targetMatrix: Matrix) {
    val currentMatrix = Matrix(imageView.imageMatrix)
    val startTime = System.currentTimeMillis()
    val duration = 500L
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

        // Interpolate between matrices
        val interpolatedValues = FloatArray(9)
        for (i in 0..8) {
          interpolatedValues[i] = currentValues[i] +
            (targetValues[i] - currentValues[i]) * interpolatedProgress
        }
        matrix.setValues(interpolatedValues)

        imageView.imageMatrix = matrix

        if (progress < 1f) {
          handler.postDelayed(this, 16)
        }
      }
    }

    handler.post(matrixEvaluator)
  }


  fun resetCrop() {
    originalMatrix?.let { matrix ->
      imageView.imageMatrix = matrix
      croppingOverlayView.setImageView(imageView)
    }
  }
}
