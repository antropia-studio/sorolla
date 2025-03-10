package com.antropia.sorolla.view

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.antropia.sorolla.R
import com.antropia.sorolla.event.OnEditFinishEvent
import com.antropia.sorolla.mixin.Geometer
import com.antropia.sorolla.mixin.ViewAnimator
import com.antropia.sorolla.util.Axis
import com.antropia.sorolla.util.ImageSettings
import com.antropia.sorolla.util.Mode
import com.antropia.sorolla.util.RectAnchor
import com.antropia.sorolla.view.overlay.CroppingOverlayView
import com.antropia.sorolla.view.overlay.OnCropAreaChangeListener
import com.antropia.sorolla.view.overlay.TransformableImageView
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.UIManagerHelper


class SorollaView : RelativeLayout, Geometer, ViewAnimator {
  private val gestureExclusionRect = Rect()
  private val imageView: TransformableImageView
  private val croppingOverlayView: CroppingOverlayView
  private var mode: Mode = Mode.NONE

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

    setupCropListeners()
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    super.onLayout(changed, l, t, r, b)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      gestureExclusionRect.set(l, t, r, b)
      systemGestureExclusionRects = listOf(gestureExclusionRect)
    }
  }

  fun setImage(uri: String) {
    imageView.setImage(uri) { croppingOverlayView.setImageView(imageView) }
  }

  fun setMode(mode: Mode) {
    this.mode = mode
    onModeChange(mode)
  }

  fun setImageSettings(settings: ImageSettings) {
    val brightness = settings.brightness.toFloat() * 255f // Ranging from -255f to 255f
    val contrast = settings.contrast.toFloat() + 1f // Ranging from 0f to 2f
    val saturation = settings.saturation.toFloat() + 1f // Ranging from 0f to 2f

    println("$brightness, $contrast, $saturation")
    val colorMatrix = ColorMatrix(
      floatArrayOf(
        contrast, 0f, 0f, 0f, brightness,
        0f, contrast, 0f, 0f, brightness,
        0f, 0f, contrast, 0f, brightness,
        0f, 0f, 0f, 1f, 0f
      )
    )
    val saturationMatrix = ColorMatrix().apply { setSaturation(saturation) }
    colorMatrix.postConcat(saturationMatrix)

    imageView.colorFilter = ColorMatrixColorFilter(ColorMatrix(colorMatrix))
  }

  fun acceptEdition() {
    val reactContext = context as ReactContext
    val surfaceId = UIManagerHelper.getSurfaceId(reactContext)
    val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(reactContext, id)

    val uri = imageView.saveToFile(clippingArea = croppingOverlayView.cropRect ?: RectF())
    val payload = Arguments.createMap()
    payload.putString("uri", uri)

    val event = OnEditFinishEvent(surfaceId, id, payload)
    eventDispatcher?.dispatchEvent(event)
  }

  fun rotateCcw() {
    val result = croppingOverlayView.rotateCcw()
    result?.let {
      imageView.rotateCcw(result.scale, result.fromRect)
    }
  }

  fun mirror(axis: Axis) {
    val cropRect = croppingOverlayView.cropRect ?: return

    imageView.mirror(axis, cropRect)
  }

  override fun setBackgroundColor(color: Int) {
    super.setBackgroundColor(color)
    croppingOverlayView.overlayColor = color
  }

  fun cancelTransform() {
    imageView.restoreTransforms()
    croppingOverlayView.restoreOverlay()
  }

  private fun onModeChange(mode: Mode) {
    when (mode) {
      Mode.NONE, Mode.SETTINGS -> {
        croppingOverlayView.fadeOut()
      }

      Mode.TRANSFORM -> {
        croppingOverlayView.fadeIn()
      }
    }
  }

  private fun setupCropListeners() {
    croppingOverlayView.setOnCropAreaChangeListener(object : OnCropAreaChangeListener {
      override fun onScale(scale: Float, anchor: RectAnchor, fromRect: RectF, toRect: RectF) {
        if (mode != Mode.TRANSFORM) return

        imageView.refitImageToCrop(scale, anchor, fromRect, toRect)
      }

      override fun onMove(dx: Float, dy: Float) {
        if (mode != Mode.TRANSFORM) return

        imageView.moveImage(dx, dy)
      }

      override fun onMoveFinish(croppingRect: RectF) {
        if (mode != Mode.TRANSFORM) return

        imageView.moveImageWithinBoundaries(croppingRect)
      }
    })
  }
}
