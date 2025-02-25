package com.antropia.sorolla.event

import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.Event

class OnEditFinishEvent(surfaceId: Int, viewId: Int, val payload: WritableMap) :
  Event<OnEditFinishEvent>(surfaceId, viewId) {

  override fun getEventName(): String {
    return "onEditFinish"
  }

  override fun getEventData(): WritableMap {
    return payload
  }
}
