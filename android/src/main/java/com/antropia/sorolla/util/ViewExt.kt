package com.antropia.sorolla.util

import android.view.View

val View.paddingHorizontal
  get() = paddingLeft + paddingRight

val View.paddingVertical
  get() = paddingTop + paddingBottom
