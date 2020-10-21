package com.discord.simpleast

import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.TextNode
import com.discord.simpleast.core.utils.ASTUtils
import org.junit.Assert


fun Node<*>.assertItemText(expectedText: String) {
  val content = asContentText()
  Assert.assertEquals(expectedText, content)
}

/***
 * Recursively tries to build a string out of the node contents
 */
fun Node<*>.asContentText(): String? = if (this is TextNode) {
  this.content
} else {
  getChildren()?.joinToString("") { it.asContentText().orEmpty() }
}

inline fun <reified T : Node<*>> List<Node<*>>.assertNodeContents(vararg nodeContent: String) {
  val listItemNodes = ArrayList<T>()
  ASTUtils.traversePreOrder(this) {
    if (it is T) {
      listItemNodes.add(it)
    }
  }
  Assert.assertTrue("No instances of ${T::class} found in $this", listItemNodes.isNotEmpty())

  nodeContent.forEachIndexed { index, expected ->
    listItemNodes[index].assertItemText(expected)
  }

  Assert.assertEquals(
      "Differing node counts:\n  Expected: ${nodeContent.joinToString()}\n\n  Actual: $this",
      nodeContent.size, listItemNodes.size)
}