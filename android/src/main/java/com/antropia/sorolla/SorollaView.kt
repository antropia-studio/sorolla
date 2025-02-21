package com.antropia.sorolla

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import com.antropia.sorolla.util.paddingHorizontal
import com.antropia.sorolla.util.paddingVertical
import com.antropia.sorolla.view.overlay.CroppingOverlayView
import kotlin.math.min

class SorollaView : LinearLayout {
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

  private fun setupCropListener() {
    croppingOverlayView.setOnCropChangeListener { cropRect ->
      // Cancel any pending updates
      pendingCropUpdate?.let { handler.removeCallbacks(it) }

      // Schedule new update with delay
      pendingCropUpdate = Runnable {
        animateImageToCrop(cropRect)
      }.also {
        handler.postDelayed(it, 500) // 500ms delay
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
    val scale = min(
      viewWidth.toFloat() / drawableWidth,
      viewHeight.toFloat() / drawableHeight
    )

    // After the scale, move the image to center it in the image
    val dx = (viewWidth - drawableWidth * scale) / 2f
    val dy = (viewHeight - drawableHeight * scale) / 2f

    val matrix = Matrix()
    matrix.setScale(scale, scale)
    matrix.postTranslate(dx, dy)

    imageView.imageMatrix = matrix
    originalImageScale = scale
    originalMatrix = Matrix(matrix)

    croppingOverlayView.setImageView(imageView)
  }

  private fun animateImageToCrop(cropRect: RectF) {
    val drawable = imageView.drawable ?: return
    val originalMatrix = originalMatrix ?: return

    val viewWidth = imageView.width - imageView.paddingHorizontal
    val viewHeight = imageView.height - imageView.paddingVertical
    val drawableWidth = drawable.intrinsicWidth.toFloat()
    val drawableHeight = drawable.intrinsicHeight.toFloat()

    // Calculate the target matrix
    val targetMatrix = Matrix(originalMatrix)

    // Calculate scale to fit the cropped area
    val scaleX = 1 / cropRect.width()
    val scaleY = 1 / cropRect.height()
    val scale = min(scaleX, scaleY)

//    val tx =
//      -cropRect.left * scaledWidth + (viewWidth - scaledWidth * cropRect.width()) / 2f + imageView.paddingLeft
//    val ty =
//      -cropRect.top * scaledHeight + (viewHeight - scaledHeight * cropRect.height()) / 2f + imageView.paddingTop
    val tx = viewWidth * cropRect.left
    val ty = viewHeight * cropRect.top

    targetMatrix.postScale(scale, scale)
    targetMatrix.postTranslate(-tx, -ty)

    // Animate between current and target matrix
    val currentMatrix = Matrix(imageView.imageMatrix)
    val startTime = System.currentTimeMillis()
    val duration = 500L // Animation duration in milliseconds
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
