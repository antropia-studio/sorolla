package com.antropia.sorolla

import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import com.antropia.sorolla.event.OnEditFinishEvent
import com.antropia.sorolla.mixin.RectHandler
import com.antropia.sorolla.mixin.ViewAnimator
import com.antropia.sorolla.util.RectAnchor
import com.antropia.sorolla.view.overlay.CroppingOverlayView
import com.antropia.sorolla.view.overlay.OnCropAreaChangeListener
import com.antropia.sorolla.view.overlay.TransformableImageView
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.UIManagerHelper


class SorollaView : LinearLayout, RectHandler, ViewAnimator {
  private val gestureExclusionRect = Rect()
  private val imageView: TransformableImageView
  private val croppingOverlayView: CroppingOverlayView
  private val editionToolsView: View
  private val transformToolsView: View
  private val transformButton: ImageButton
  private val cancelTransformButton: ImageButton
  private val acceptTransformButton: ImageButton
  private val confirmationButtons: List<View>

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
    editionToolsView = findViewById(R.id.edition_tools)
    transformToolsView = findViewById(R.id.transform_tools)
    transformButton = findViewById(R.id.transform_button)
    cancelTransformButton = findViewById(R.id.cancel_transform_button)
    acceptTransformButton = findViewById(R.id.accept_transform_button)
    confirmationButtons = listOf(cancelTransformButton, acceptTransformButton)

    setupCropListeners()
    setupButtonListeners()
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

  private fun setupCropListeners() {
    croppingOverlayView.setOnCropAreaChangeListener(object : OnCropAreaChangeListener {
      override fun onScale(scale: Float, anchor: RectAnchor, fromRect: RectF, toRect: RectF) {
        imageView.refitImageToCrop(scale, anchor, fromRect, toRect)
      }

      override fun onMove(dx: Float, dy: Float) {
        imageView.moveImage(dx, dy)
      }

      override fun onMoveFinish(croppingRect: RectF) {
        imageView.moveImageWithinBoundaries(croppingRect)
      }
    })
  }

  private fun setupButtonListeners() {
    transformButton.setOnClickListener {
      editionToolsView.replaceAnimated(transformToolsView)
      confirmationButtons.forEach { it.show() }
      croppingOverlayView.fadeIn()
    }

    findViewById<Button>(R.id.restore_button).setOnClickListener { resetTransforms() }

    cancelTransformButton.setOnClickListener {
      resetTransforms()
      transformToolsView.replaceAnimated(editionToolsView)
      confirmationButtons.forEach { it.hide() }
      croppingOverlayView.fadeOut()
    }
    acceptTransformButton.setOnClickListener {
      transformToolsView.replaceAnimated(editionToolsView)
      confirmationButtons.forEach { it.hide() }
      croppingOverlayView.fadeOut()
      finishEdition()
    }
  }

  private fun resetTransforms() {
    imageView.restoreTransforms()
    croppingOverlayView.restoreOverlay()
  }

  private fun finishEdition() {
    val reactContext = context as ReactContext
    val surfaceId = UIManagerHelper.getSurfaceId(reactContext)
    val eventDispatcher = UIManagerHelper.getEventDispatcherForReactTag(reactContext, id)

    val uri = imageView.saveToFile(clippingArea = croppingOverlayView.cropRect ?: RectF())
    val payload = Arguments.createMap()
    payload.putString("uri", uri)

    val event = OnEditFinishEvent(surfaceId, id, payload)
    eventDispatcher?.dispatchEvent(event)
  }
}
