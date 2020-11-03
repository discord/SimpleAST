package com.discord.simpleast.markdown

import android.graphics.Color
import android.graphics.Typeface
import android.text.style.BulletSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.node.TextNode
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.core.utils.ASTUtils
import com.discord.simpleast.core.utils.TreeMatcher
import junit.framework.Assert
import org.junit.Before
import org.junit.Test


class MarkdownRulesTest {

  private lateinit var parser: Parser<Any, Node<Any>, Any?>
  private lateinit var treeMatcher: TreeMatcher

  @Before
  fun setup() {
    parser = Parser()
    parser.addRules(
        MarkdownRules.HeaderRule { StyleSpan(Typeface.BOLD) },
        MarkdownRules.HeaderLineClassedRule(styleSpanProvider = { StyleSpan(Typeface.ITALIC) }) { className ->
          when (className) {
            "strike" -> StrikethroughSpan()
            else -> null
          }
        },
        MarkdownRules.ListItemRule { BulletSpan(24, Color.parseColor("#6E7B7F")) }
    )
    parser.addRules(SimpleMarkdownRules.createSimpleMarkdownRules(includeTextRule = true))
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
    """.trimIndent(), Unit)

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
  fun listItemsInvalidPrefix() {
    val ast = parser.parse("""
      List of stuff
      -* item 1
      =* item 2
    """.trimIndent(), Unit)

    val listItemNodes = ArrayList<MarkdownListItemNode<*>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is MarkdownListItemNode) {
        listItemNodes.add(it)
      }
    }

    Assert.assertEquals(0, listItemNodes.size)
  }

  @Test
  fun listItemsEnd() {
    val ast = parser.parse("""
      List of stuff
      * item 1
      * item 2
    """.trimIndent(), Unit)

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
    """.trimIndent(), Unit)

    val styledNodes = ArrayList<StyleNode<*, *>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is StyleNode<*, *>) {
        styledNodes.add(it)
      }
    }

    Assert.assertEquals(3, styledNodes.size)
    styledNodes[0].assertItemText("Header 1")
    styledNodes[1].assertItemText("Header 2")
    styledNodes[2].assertItemText("Header 3")
  }

  @Test
  fun headersConsumeNewlines() {
    val ast = parser.parse("""
      Title
      # Header 1


      # Header 2
    """.trimIndent(), Unit)

    val expected = listOf<Node<Any>>(
        TextNode("Title"),
        TextNode("\n"),
        StyleNode.wrapText(
            "Header 1",
            listOf(StyleSpan(Typeface.BOLD))),
        TextNode("\n"),
        StyleNode.wrapText(
            "Header 2",
            listOf(StyleSpan(Typeface.BOLD)))
    )
    Assert.assertEquals("newlines consumed", expected.toString(), ast.toString())
  }

  @Test
  fun headersInvalidPrefix() {
    val ast = parser.parse("""
      a# Header 1
      some content
      -## Header 2
      other content for listing # of items
    """.trimIndent(), Unit)

    val styledNodes = ArrayList<StyleNode<*, *>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is StyleNode<*, *>) {
        styledNodes.add(it)
      }
    }
    Assert.assertEquals(0, styledNodes.size)
  }

  @Test
  fun headerAlt() {
    val ast = parser.parse("""
      __Alt__ Header
      ======
      some content
    """.trimIndent(), Unit)

    val styledNodes = ArrayList<StyleNode<*, *>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is StyleNode<*, *>) {
        styledNodes.add(it)
      }
    }

    Assert.assertEquals(2, styledNodes.size)
    val headerChildren = styledNodes[0].getChildren()?.toList()
    Assert.assertEquals(2, headerChildren!!.size)

    val italicWord = headerChildren[0]
    val remainingWords = headerChildren[1]

    italicWord.assertItemText("Alt")
    Assert.assertEquals(" Header", (remainingWords as TextNode).content)
  }

  @Test
  fun headerAltAfterParagraph() {
    val ast = parser.parse("""
      Some introduction text.


      Alt Header
      =======
      some content
    """.trimIndent(), Unit)

    val expected = listOf<Node<Any>>(
        TextNode("Some introduction text"),
        TextNode("."),
        TextNode("\n"),
        StyleNode.wrapText(
            "Alt Header",
            listOf(StyleSpan(Typeface.BOLD))),
        TextNode("\nsome content")
    )
    Assert.assertEquals("newlines consumed", expected.toString(), ast.toString())
  }

  @Test
  fun headerParseBlockPunctuations() {
    val ast = parser.parse("""
      Intro text

      Should Succeed. Alt Header
      ======
      some content
    """.trimIndent(), Unit)

    val styledNodes = ArrayList<StyleNode<*, *>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is StyleNode<*, *>) {
        styledNodes.add(it)
      }
    }

    Assert.assertEquals(1, styledNodes.size)
    val headerChildren = styledNodes[0].getChildren()
        ?.mapNotNull { it as? TextNode }
        ?.joinToString("") { it.content }

    Assert.assertEquals("Should Succeed. Alt Header", headerChildren)
  }

  @Test
  fun headerAltClassed() {
    val ast = parser.parse("""
      *Alt*. Header {strike unknown}
      ======
      some content
    """.trimIndent(), Unit)

    val styledNodes = ArrayList<StyleNode<*, *>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is StyleNode<*, *>) {
        styledNodes.add(it)
      }
    }

    val lheaderNode = styledNodes[0] as StyleNode
    Assert.assertEquals("unrecognized class not styled", 1, lheaderNode.styles.size)
    Assert.assertTrue("classed style applied", lheaderNode.styles.first() is StrikethroughSpan)
    val defaultHeaderStyleNode = lheaderNode.getChildren()?.firstOrNull() as StyleNode<*, *>
    Assert.assertTrue("default style applied", defaultHeaderStyleNode.styles.firstOrNull() is StyleSpan)

    val headerChildren = defaultHeaderStyleNode.getChildren()!!.toList()
    val italicWord = headerChildren[0]
    val remainingWords = headerChildren[1]

    italicWord.assertItemText("Alt")
    Assert.assertEquals(". Header", (remainingWords as TextNode).content)
  }


  private fun Node<*>.assertItemText(expectedText: String) {
    val textNode = getChildren()?.first() as? TextNode
    Assert.assertEquals(expectedText, textNode?.content)
  }

}