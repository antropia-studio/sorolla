package com.antropia.sorolla.mixin

interface Interpolator {
  fun Float.lerp(start: Float, end: Float) = start + (end - start) * this
}
