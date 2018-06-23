package com.discord.simpleast.markdown

import android.graphics.Color
import android.graphics.Typeface
import android.text.style.BulletSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
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

  private lateinit var parser: Parser<Any, Node<Any>>
  private lateinit var treeMatcher: TreeMatcher

  @Before
  fun setup() {
    parser = Parser()
    parser.addRules<Node<Any>>(listOf(
        MarkdownRules.HeaderRule { StyleSpan(Typeface.BOLD) },
        MarkdownRules.HeaderLineRule { StyleSpan(Typeface.ITALIC) },
        MarkdownRules.ListItemRule { BulletSpan(24, Color.parseColor("#6E7B7F")) }
    ))
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
  fun listItemsInvalidPrefix() {
    val ast = parser.parse("""
      List of stuff
      -* item 1
      =* item 2
    """.trimIndent())

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

  @Test
  fun headersInvalidPrefix() {
    val ast = parser.parse("""
      a# Header 1
      some content
      -## Header 2
      other content for listing # of items
      """.trimIndent())

    val styledNodes = ArrayList<StyleNode<*>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is StyleNode) {
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
      """.trimIndent())

    val styledNodes = ArrayList<StyleNode<*>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is StyleNode) {
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
      Some really long introduction text that goes on forever explaining something.

      Alt Header
      =======
      some content
      """.trimIndent())

    val styledNodes = ArrayList<StyleNode<*>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is StyleNode) {
        styledNodes.add(it)
      }
    }

    Assert.assertEquals(1, styledNodes.size)
    styledNodes[0].assertItemText("Alt Header")
  }

  @Test
  fun headerParseBlockPunctuations() {
    val ast = parser.parse("""
      Intro text

      Should Succeed. Alt Header
      ======
      some content
      """.trimIndent())

    val styledNodes = ArrayList<StyleNode<*>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is StyleNode) {
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
    val parser = Parser<Any, Node<Any>>()
    parser.addRules<Node<Any>>(listOf(
        MarkdownRules.HeaderRule { StyleSpan(Typeface.BOLD) },
        MarkdownRules.HeaderLineClassedRule(
            styleSpanProvider = { StyleSpan(Typeface.BOLD) },
            classSpanProvider = { className ->
              when (className) {
                "testClass" -> UnderlineSpan()
                else -> null
              }
            })
    ))
    parser.addRules(SimpleMarkdownRules.createSimpleMarkdownRules(includeTextRule = true))

    val ast = parser.parse("""
      *Alt*. Header {testClass unknown}
      ======
      some content
      """.trimIndent())

    val styledNodes = ArrayList<StyleNode<*>>()
    ASTUtils.traversePreOrder(ast) {
      if (it is StyleNode) {
        styledNodes.add(it)
      }
    }

    val lheaderNode = styledNodes[0]
    Assert.assertEquals(1, lheaderNode.styles.size)
    Assert.assertTrue(lheaderNode.styles[0] is UnderlineSpan)

    val headerChildren = lheaderNode.getChildren()?.toList()
    Assert.assertEquals(2, headerChildren!!.size)

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