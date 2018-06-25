package com.discord.simpleast.sample.spans

import android.graphics.Paint
import android.support.annotation.Dimension
import android.text.style.LineHeightSpan


/**
 * Simple [Span] that adds pixels to a span vertically.
 */
class VerticalMarginSpan(@Dimension private val topPx: Int,
                         @Dimension private val bottomPx: Int) : LineHeightSpan {

  override fun chooseHeight(text: CharSequence, start: Int, end: Int, spanstartv: Int, v: Int,
                   fm: Paint.FontMetricsInt) {
    fm.ascent -= topPx
    fm.descent += bottomPx
  }
}