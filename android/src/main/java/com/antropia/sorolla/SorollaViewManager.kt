package com.antropia.sorolla

import android.graphics.Color
import com.antropia.sorolla.util.Axis
import com.antropia.sorolla.util.Mode
import com.antropia.sorolla.view.SorollaView
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.SorollaViewManagerDelegate
import com.facebook.react.viewmanagers.SorollaViewManagerInterface


@ReactModule(name = SorollaViewManager.NAME)
class SorollaViewManager : SimpleViewManager<SorollaView>(),
  SorollaViewManagerInterface<SorollaView> {
  private val mDelegate: ViewManagerDelegate<SorollaView> = SorollaViewManagerDelegate(this)

  override fun getDelegate(): ViewManagerDelegate<SorollaView> {
    return mDelegate
  }

  override fun getName(): String {
    return NAME
  }

  public override fun createViewInstance(context: ThemedReactContext): SorollaView {
    return SorollaView(context)
  }

  @ReactProp(name = "uri")
  override fun setUri(view: SorollaView?, value: String?) {
    val rawImageUri = value ?: return

    view?.setImage(rawImageUri)
  }

  @ReactProp(name = "mode")
  override fun setMode(view: SorollaView?, value: String?) {
    val rawMode = value ?: return

    val mode = Mode.valueOf(rawMode.uppercase())
    view?.setMode(mode)
  }

  @ReactProp(name = "backgroundColor")
  override fun setBackgroundColor(view: SorollaView?, value: String?) {
    val rawBackgroundColor = value ?: return

    view?.setBackgroundColor(Color.parseColor(rawBackgroundColor))
  }

  override fun acceptEdition(view: SorollaView?) {
    view?.acceptEdition()
  }

  override fun mirrorHorizontally(view: SorollaView?) {
    view?.mirror(Axis.HORIZONTAL)
  }

  override fun mirrorVertically(view: SorollaView?) {
    view?.mirror(Axis.VERTICAL)
  }

  override fun rotateCcw(view: SorollaView?) {
    view?.rotateCcw()
  }

  override fun cancelTransform(view: SorollaView?) {
    view?.cancelTransform()
  }

  override fun getExportedCustomBubblingEventTypeConstants(): Map<String, Any> {
    val map: MutableMap<String, Any> = HashMap()
    val bubblingMap: MutableMap<String, Any> = HashMap()

    bubblingMap["phasedRegistrationNames"] = object : HashMap<String?, String?>() {
      init {
        put("bubbled", "onEditFinish")
        put("captured", "onEditFinishCapture")
      }
    }

    map["onEditFinish"] = bubblingMap
    return map
  }

  companion object {
    const val NAME = "SorollaView"
  }
}
