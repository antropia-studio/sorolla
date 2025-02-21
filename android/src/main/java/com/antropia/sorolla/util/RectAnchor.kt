package com.antropia.sorolla.util

enum class RectAnchor {
  TOP_LEFT,
  TOP_RIGHT,
  BOTTOM_LEFT,
  BOTTOM_RIGHT;

  val opposite: RectAnchor
    get() = when (this) {
      TOP_LEFT -> BOTTOM_RIGHT
      TOP_RIGHT -> BOTTOM_LEFT
      BOTTOM_LEFT -> TOP_RIGHT
      BOTTOM_RIGHT -> TOP_LEFT
    }
}
