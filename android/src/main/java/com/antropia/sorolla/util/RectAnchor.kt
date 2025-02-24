package com.antropia.sorolla.util

enum class RectAnchor {
  // Edges
  LEFT, TOP, RIGHT, BOTTOM,

  // Corners
  TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;

  val opposite: RectAnchor
    get() = when (this) {
      LEFT -> RIGHT
      TOP -> BOTTOM
      RIGHT -> LEFT
      BOTTOM -> TOP
      TOP_LEFT -> BOTTOM_RIGHT
      TOP_RIGHT -> BOTTOM_LEFT
      BOTTOM_LEFT -> TOP_RIGHT
      BOTTOM_RIGHT -> TOP_LEFT
    }
}
