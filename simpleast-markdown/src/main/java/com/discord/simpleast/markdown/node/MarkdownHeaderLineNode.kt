package com.discord.simpleast.markdown.node

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.CharacterStyle
import com.discord.simpleast.core.node.StyleNode

/**
 */
class MarkdownHeaderLineNode<R>(styles: List<CharacterStyle>, private val indexHeaderPrefixEnd: Int) : StyleNode<R>(styles) {
  override fun render(builder: SpannableStringBuilder, renderContext: R) {
    val startIndex = builder.length

    // First render all child nodes, as these are the nodes we want to apply the styles to.
    getChildren()?.forEach { it.render(builder, renderContext) }

    styles.forEach { builder.setSpan(it, startIndex + indexHeaderPrefixEnd, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
  }
}