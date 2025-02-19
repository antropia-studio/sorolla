package com.antropia.sorolla

import android.graphics.Color
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
    view?.setBackgroundColor(Color.parseColor("#FF9529"))

    value?.let {
      view?.setImage(value)
    }
  }

  companion object {
    const val NAME = "SorollaView"
  }
}
