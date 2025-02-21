package com.antropia.sorolla.util

/**
 * This is the silliest class I could come up to remember how aspect ratio works.
 *
 * A reminder for future readers: aspectRatio = W / H
 * That means if we want to calculate the new width or height of a rect, we should use the following
 * formulas:
 * W' = H' * aspectRatio
 * H' = W' / aspectRatio
 */
class AspectRatio(private val width: Float, private val height: Float) {
  val ratio = width / height

  fun calculateHeight(width: Float) = width / ratio
  fun calculateWidth(height: Float) = height * ratio
}
