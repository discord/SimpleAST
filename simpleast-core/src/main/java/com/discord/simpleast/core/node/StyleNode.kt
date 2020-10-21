package com.discord.simpleast.core.node

import android.text.SpannableStringBuilder
import android.text.Spanned
import org.jetbrains.annotations.TestOnly


/**
 * @param RC RenderContext
 * @param T Type of Span to apply
 */
open class StyleNode<RC, T>(val styles: List<T>) : Node.Parent<RC>() {

  override fun render(builder: SpannableStringBuilder, renderContext: RC) {
    val startIndex = builder.length

    // First render all child nodes, as these are the nodes we want to apply the styles to.
    super.render(builder, renderContext)

    styles.forEach { builder.setSpan(it, startIndex, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
  }

  fun interface SpanProvider<RC> {
    fun get(renderContext: RC) : Iterable<*>
  }

  /**
   * A slightly optimized version of styling text in a terminal fashion.
   * Use this if you know the node is terminal and will have no children as it will not render them.
   *
   * @see TextNode
   */
  class Text<RC>(content: String, val stylesProvider: SpanProvider<RC>) : TextNode<RC>(content) {
    override fun render(builder: SpannableStringBuilder, renderContext: RC) {
      val startIndex = builder.length
      super.render(builder, renderContext)

      stylesProvider.get(renderContext).forEach {
        builder.setSpan(it, startIndex, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
    }
  }

  companion object {
    /**
     * Convenience method for creating a [StyleNode] when we already know what
     * the text content will be.
     */
    @JvmStatic
    @TestOnly
    fun <RC, T> wrapText(content: String, styles: List<T>): StyleNode<RC, T> {
      val styleNode = StyleNode<RC, T>(styles)
      styleNode.addChild(TextNode(content))
      return styleNode
    }
  }
}
