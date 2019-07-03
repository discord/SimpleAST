package com.discord.simpleast.core.parser

import com.discord.simpleast.core.node.Node

/**
 * Facilitates fast parsing of the source text.
 *
 *
 * For nonterminal subtrees, the provided root will be added to the main, and text between
 * startIndex (inclusive) and endIndex (exclusive) will continue to be parsed into Nodes and
 * added as children under this root.
 *
 *
 * For terminal subtrees, the root will simply be added to the tree and no additional parsing will
 * take place on the text.
 */
class ParseSpec<R, T : Node<R>> {
  val root: T?
  val isTerminal: Boolean
  val state: Map<String, Any>
  var startIndex: Int = 0
  var endIndex: Int = 0

  constructor(root: T?, state: Map<String, Any>, startIndex: Int, endIndex: Int) {
    this.root = root
    this.state = state
    this.isTerminal = false
    this.startIndex = startIndex
    this.endIndex = endIndex
  }

  constructor(root: T?, state: Map<String, Any>) {
    this.root = root
    this.state = state
    this.isTerminal = true
  }

  fun applyOffset(offset: Int) {
    startIndex += offset
    endIndex += offset
  }

  companion object {

    @JvmStatic
    fun <R, T : Node<R>> createNonterminal(node: T?, state: Map<String, Any>, startIndex: Int, endIndex: Int): ParseSpec<R, T> {
      return ParseSpec(node, state, startIndex, endIndex)
    }

    @JvmStatic
    fun <R, T : Node<R>> createTerminal(node: T?, state: Map<String, Any>): ParseSpec<R, T> {
      return ParseSpec(node, state)
    }
  }
}

