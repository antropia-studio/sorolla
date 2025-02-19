package com.antropia.sorolla

import android.graphics.Color
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.SorollaViewManagerInterface
import com.facebook.react.viewmanagers.SorollaViewManagerDelegate

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

  @ReactProp(name = "source")
  override fun setSource(view: SorollaView?, value: Int) {
    view?.setBackgroundColor(Color.parseColor("#FF9529"))
  }

  companion object {
    const val NAME = "SorollaView"
  }
}
