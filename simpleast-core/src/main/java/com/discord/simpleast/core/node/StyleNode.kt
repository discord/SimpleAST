package com.discord.simpleast.core.node

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.CharacterStyle

open class StyleNode<R>(val styles: List<CharacterStyle>) : Node<R>() {

  override fun render(builder: SpannableStringBuilder, renderContext: R) {
    val startIndex = builder.length

    // First render all child nodes, as these are the nodes we want to apply the styles to.
    getChildren()?.forEach { it.render(builder, renderContext) }

    styles.forEach { builder.setSpan(it, startIndex, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
  }

  companion object {

    /**
     * Convenience method for creating a [StyleNode] when we already know what
     * the text content will be.
     */
    @JvmStatic
    fun <R> createWithText(content: String, styles: List<CharacterStyle>): StyleNode<R> {
      val styleNode = StyleNode<R>(styles)
      styleNode.addChild(TextNode(content))
      return styleNode
    }
  }
}
