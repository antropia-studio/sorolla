package com.antropia.sorolla

import android.content.Context
import android.graphics.Matrix
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
        animateImageToCrop(scale, anchor, fromRect, toRect)
      }.also {
        handler.postDelayed(it, 0)
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

  private fun animateImageToCrop(scale: Float, anchor: RectAnchor, fromRect: RectF, toRect: RectF) {
    val drawable = imageView.drawable ?: return
    val originalMatrix = originalMatrix ?: return

    val viewWidth = imageView.width - imageView.paddingHorizontal
    val viewHeight = imageView.height - imageView.paddingVertical
    val drawableWidth = drawable.intrinsicWidth
    val drawableHeight = drawable.intrinsicHeight
    val drawableScale =
      minOf(viewWidth.toFloat() / drawableWidth, viewHeight.toFloat() / drawableHeight)

    val marginHorizontal = viewWidth - drawableWidth * drawableScale
    val marginVertical = viewHeight - drawableHeight * drawableScale

    val pivotLeft = marginHorizontal / 2f
    val pivotRight = viewWidth - marginHorizontal / 2f
    val pivotTop = marginVertical / 2f
    val pivotBottom = viewHeight - marginVertical / 2f

    val targetMatrix = Matrix(originalMatrix)

    when (anchor) {
      RectAnchor.TOP_LEFT -> {
        targetMatrix.postScale(scale, scale, pivotLeft, pivotTop)
        targetMatrix.postTranslate(toRect.left - fromRect.left, toRect.top - fromRect.top)
      }

      RectAnchor.TOP_RIGHT -> {
        targetMatrix.postScale(scale, scale, pivotRight, pivotTop)
        targetMatrix.postTranslate(toRect.right - fromRect.right, toRect.top - fromRect.top)
      }

      RectAnchor.BOTTOM_LEFT -> {
        targetMatrix.postScale(scale, scale, pivotLeft, pivotBottom)
        targetMatrix.postTranslate(toRect.left - fromRect.left, toRect.bottom - fromRect.bottom)
      }

      RectAnchor.BOTTOM_RIGHT -> {
        targetMatrix.postScale(scale, scale, pivotRight, pivotBottom)
        targetMatrix.postTranslate(toRect.right - fromRect.right, toRect.bottom - fromRect.bottom)
      }
    }

    this.originalMatrix = targetMatrix

    // Animate between current and target matrix
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
