package com.discord.simpleast.core.node


import android.text.SpannableStringBuilder

/**
 * Node representing simple text.
 */
open class TextNode<R> (val content: String) : Node<R>() {
  override fun render(builder: SpannableStringBuilder, renderContext: R) {
    builder.append(content)
  }
}
