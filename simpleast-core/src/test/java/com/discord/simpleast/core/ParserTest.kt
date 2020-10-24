package com.discord.simpleast.core

import android.graphics.Typeface
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.node.TextNode
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.core.utils.TreeMatcher
import org.junit.Assert
import org.junit.Before
import org.junit.Test


class ParserTest {
  private lateinit var parser: Parser<Any, Node<Any>, Any?>
  private lateinit var treeMatcher: TreeMatcher

  @Before
  fun setup() {
    parser = Parser<Any, Node<Any>, Any?>()
        .addRules(SimpleMarkdownRules.createSimpleMarkdownRules())
    treeMatcher = TreeMatcher()
    treeMatcher.registerDefaultMatchers()
  }

  @Test(expected = Parser.ParseException::class)
  fun testNoRuleMatch() {
    val badParser = Parser<Any, Node<Any>, Any?>()
        .addRules(SimpleMarkdownRules.createSimpleMarkdownRules(includeTextRule = false))
    badParser.parse("unmatched text", null)
  }

  @Test
  fun testEmptyParse() {
    val ast = parser.parse("", null)
    Assert.assertTrue(ast.isEmpty())
  }

  @Test
  fun testParseFormattedText() {
    val ast = parser.parse("**bold**", null)
    val model = listOf<Node<Any>>(
        StyleNode.wrapText("bold", listOf(StyleSpan(Typeface.BOLD) as CharacterStyle)),
    )
    Assert.assertTrue(treeMatcher.matches(model, ast))
  }

  @Test
  fun testParseLeadingFormatting() {
    val ast = parser.parse("**bold** and not bold", null)
    val model = listOf<Node<Any>>(
        StyleNode.wrapText("bold", listOf(StyleSpan(Typeface.BOLD) as CharacterStyle)),
        TextNode(" and not bold")
    )
    Assert.assertTrue(treeMatcher.matches(model, ast))
  }

  @Test
  fun testParseTrailingFormatting() {
    val ast = parser.parse("not bold **and bold**", null)
    val model = listOf<Node<Any>>(
        TextNode("not bold "),
        StyleNode.wrapText("and bold", listOf(StyleSpan(Typeface.BOLD) as CharacterStyle))
    )
    Assert.assertTrue(treeMatcher.matches(model, ast))
  }

  @Test
  fun testNestedFormatting() {
    val ast = parser.parse("**bold *and italics* and more bold**", null)

    val boldNode: StyleNode<Any, *> = StyleNode(listOf(StyleSpan(Typeface.BOLD) as CharacterStyle))
    boldNode.addChild(TextNode("bold "))
    boldNode.addChild(StyleNode.wrapText("and italics", listOf(StyleSpan(Typeface.ITALIC) as CharacterStyle)))
    boldNode.addChild(TextNode(" and more bold"))

    Assert.assertTrue(treeMatcher.matches(listOf(boldNode), ast))
  }

  @Test
  fun testNewlineRule() {
    val ast = parser.parse("Some text\n\n\n  \n\n\nnewline above", null)
    val model: List<Node<*>?> = listOf(
        TextNode("Some text"),
        TextNode("\n"),
        TextNode("\n"),
        TextNode<Any>("newline above"))
    Assert.assertTrue("actual $ast", treeMatcher.matches(model, ast))
  }
}