package com.antropia.sorolla.util

interface Interpolator {
  fun Float.lerp(start: Float, end: Float) = start + (end - start) * this
}
