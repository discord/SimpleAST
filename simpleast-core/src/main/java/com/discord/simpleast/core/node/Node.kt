package com.discord.simpleast.core.node

import android.text.SpannableStringBuilder

/**
 * Represents a single node in an Abstract Syntax Tree. It can (but does not need to) have children.
 */
open class Node<R> {

  private var children: MutableCollection<Node<R>>? = null

  fun getChildren(): Collection<Node<R>>? = children

  fun hasChildren(): Boolean = children?.isNotEmpty() == true

  fun addChild(child: Node<R>) {
    children = (children ?: ArrayList()).apply {
      add(child)
    }
  }

  open fun render(builder: SpannableStringBuilder, renderContext: R) {}
}
