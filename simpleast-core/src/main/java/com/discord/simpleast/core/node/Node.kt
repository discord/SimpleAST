package com.discord.simpleast.core.node

import android.text.SpannableStringBuilder

/**
 * Represents a single node in an Abstract Syntax Tree. It can (but does not need to) have children.
 *
 * @param R The render context, can be any object that holds what's required for rendering. See [render].
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
