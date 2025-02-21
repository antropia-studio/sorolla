package com.antropia.sorolla.util

import android.view.View
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop

val View.paddingHorizontal
  get() = paddingLeft + paddingRight

val View.paddingVertical
  get() = paddingTop + paddingBottom

val View.marginHorizontal
  get() = marginLeft + marginRight

val View.marginVertical
  get() = marginTop + marginBottom
