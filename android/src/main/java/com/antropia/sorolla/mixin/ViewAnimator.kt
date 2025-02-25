package com.antropia.sorolla.mixin

import android.view.View

private const val ANIMATION_DURATION = 100L
private const val SLIDE_AMOUNT = 0.1f


interface ViewAnimator {
  fun View.replaceAnimated(newView: View) {
    hide()
    newView.show(delay = ANIMATION_DURATION / 2)
  }

  fun View.hide() {
    animate()
      .alpha(0f)
      .translationY(-height * SLIDE_AMOUNT)
      .setDuration(ANIMATION_DURATION)
      .withEndAction { visibility = View.INVISIBLE }
      .start()
  }

  fun View.show(delay: Long = 0L) {
    translationY = height * SLIDE_AMOUNT
    visibility = View.VISIBLE
    alpha = 0f

    post {
      animate()
        .alpha(1f)
        .translationY(0f)
        .setStartDelay(delay)
        .setDuration(ANIMATION_DURATION)
        .start()
    }
  }

  fun View.fadeIn() {
    visibility = View.VISIBLE
    alpha = 0f

    post {
      animate()
        .alpha(1f)
        .setDuration(ANIMATION_DURATION)
        .start()
    }
  }

  fun View.fadeOut() {
    visibility = View.VISIBLE
    alpha = 0f

    post {
      animate()
        .alpha(0f)
        .setDuration(ANIMATION_DURATION)
        .start()
    }
  }
}
