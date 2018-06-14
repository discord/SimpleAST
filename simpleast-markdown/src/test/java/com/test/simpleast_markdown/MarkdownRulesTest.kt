package com.test.simpleast_markdown

import android.graphics.Typeface
import android.text.style.StyleSpan
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.node.TextNode
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.core.utils.ASTUtils
import com.discord.simpleast.core.utils.TreeMatcher
import com.discord.simpleast.markdown.MarkdownRules
import com.discord.simpleast.markdown.node.MarkdownListItemNode
import junit.framework.Assert
import org.junit.Before
import org.junit.Test


class MarkdownRulesTest {

  private lateinit var parser: Parser<Any, Node<Any>>
  private lateinit var treeMatcher: TreeMatcher

  @Before
  fun setup() {
    parser = Parser()
    parser.addRules<Node<Any>>(listOf(
        SimpleMarkdownRules.createEscapeRule(),
        MarkdownRules.ListItemRule(),
        MarkdownRules.HeaderRuleBase.HeaderRule(StyleSpan(Typeface.BOLD)),
        MarkdownRules.HeaderRuleBase.HeaderRuleAlt(StyleSpan(Typeface.ITALIC)),
        SimpleMarkdownRules.createTextRule()
    ))
    treeMatcher = TreeMatcher()
    treeMatcher.registerDefaultMatchers()
  }

  @Test
  fun listItems() {
    val ast = parser.parse("""
      List of stuff
      * item 1
      * item 2

      End
    """.trimIndent())

    val listItemNodes = ArrayList<MarkdownListItemNode<*>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is MarkdownListItemNode) {
        listItemNodes.add(it)
      }
    }

    Assert.assertEquals(2, listItemNodes.size)
    listItemNodes[0].assertItemText("item 1")
    listItemNodes[1].assertItemText("item 2")
  }

  @Test
  fun listItemsEnd() {
    val ast = parser.parse("""
      List of stuff
      * item 1
      * item 2""".trimIndent())

    val listItemNodes = ArrayList<MarkdownListItemNode<*>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is MarkdownListItemNode) {
        listItemNodes.add(it)
      }
    }

    Assert.assertEquals(2, listItemNodes.size)
    listItemNodes[0].assertItemText("item 1")
    listItemNodes[1].assertItemText("item 2")
  }

  @Test
  fun headers() {
    val ast = parser.parse("""
      Title
      # Header 1
      some content
      ## Header 2
      ### Header 3
      """.trimIndent())

    val styledNodes = ArrayList<StyleNode<*>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is StyleNode) {
        styledNodes.add(it)
      }
    }

    Assert.assertEquals(3, styledNodes.size)
    styledNodes[0].assertItemText("Header 1")
    styledNodes[1].assertItemText("Header 2")
    styledNodes[2].assertItemText("Header 3")
  }


  private fun Node<*>.assertItemText(expectedText: String) {
    val textNode = getChildren()?.first() as? TextNode
    Assert.assertEquals(expectedText, textNode?.content)
  }

}