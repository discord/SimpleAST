package com.discord.simpleast.markdown.node

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BulletSpan
import com.discord.simpleast.core.node.Node


open class MarkdownListItemNode<R>(val bulletSpanProvider: () -> BulletSpan) : Node<R>() {

  override fun render(builder: SpannableStringBuilder, renderContext: R) {
    val startIndex = builder.length

    // First render all child nodes, as these are the nodes we want to apply the styles to.
    getChildren()?.forEach { it.render(builder, renderContext) }

    builder.setSpan(bulletSpanProvider(), startIndex, startIndex + 1,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
  }
}
