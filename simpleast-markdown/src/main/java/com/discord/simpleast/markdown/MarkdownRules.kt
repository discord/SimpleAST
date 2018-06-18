package com.discord.simpleast.markdown

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.annotation.StyleRes
import android.text.style.BulletSpan
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import com.discord.simpleast.markdown.node.MarkdownListItemNode
import java.util.regex.Matcher

/**
 * Support for full markdown representations.
 *
 * @see com.discord.simpleast.core.simple.SimpleMarkdownRules
 */
object MarkdownRules {
  /**
   * Handles markdown list syntax. Must have a whitespace after list itemcharacter `*`
   * Example:
   * ```
   * * item 1
   * * item 2
   * ```
   */
  val LIST_ITEM = """^\*[ \t](.*)(?=\n|$)""".toPattern()
  /**
   * Handles markdown header syntax. Must have a whitespace after header character `#`
   * Example:
   * ```
   * # Header 1
   * ## Header 2
   * ### Header 3
   * ```
   */
  val HEADER_ITEM = """^ *(#+)[ \t](.*) *(?=\n|$)""".toPattern()
  /**
   * Handles alternate version of headers. Must have 3+ `=` characters.
   * Example:
   * ```
   * Alternative Header 1
   * ====================
   *
   * Alternative Header 2
   * ----------
   * ```
   */
  val HEADER_ITEM_ALT = """^(?: \t)*(.+)\n *(=|-){3,} *(?=\n|$)""".toPattern()

  class ListItemRule<R>(private val bulletSpanProvider: () -> BulletSpan) :
      Rule.BlockRule<R, Node<R>>(LIST_ITEM.matcher(""), false) {

    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>, isNested: Boolean)
        : ParseSpec<R, Node<R>> {
      val node = MarkdownListItemNode<R>(bulletSpanProvider)
      return ParseSpec.createNonterminal(node, matcher.start(1), matcher.end(1))
    }
  }

  class HeaderRule<R>(private val styleSpanProvider: (Int) -> CharacterStyle) :
      Rule.BlockRule<R, Node<R>>(HEADER_ITEM.matcher(""), false) {

    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>, isNested: Boolean)
        : ParseSpec<R, Node<R>> {
      val firstGroup = matcher.group(1)
      val numHeaderIndicators = firstGroup.length
      val node = StyleNode<R>(listOf(styleSpanProvider(numHeaderIndicators)))
      return ParseSpec.createNonterminal(node, matcher.start(2), matcher.end(2))
    }
  }

  class HeaderLineRule<R>(private val styleSpanProvider: (Int) -> CharacterStyle) :
      Rule.BlockRule<R, Node<R>>(HEADER_ITEM_ALT.matcher(""), false) {

    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>, isNested: Boolean)
        : ParseSpec<R, Node<R>> {
      val headerStyleGroup = matcher.group(2)
      val headerIndicator = when (headerStyleGroup) {
        "=" -> 1
        else -> 2
      }
      val node = StyleNode<R>(listOf(styleSpanProvider(headerIndicator)))
      return ParseSpec.createNonterminal(node, matcher.start(1), matcher.end(1))
    }
  }

  @JvmStatic
  fun <R> createHeaderRules(context: Context, @StyleRes headerStyles: List<Int>): List<Rule<R, Node<R>>> {
    fun spanProvider(header: Int): CharacterStyle =
        when (header) {
          0 -> TextAppearanceSpan(context, headerStyles[0])
          in 1..headerStyles.size -> TextAppearanceSpan(context, headerStyles[header - 1])
          else -> StyleSpan(Typeface.BOLD_ITALIC)
        }

    return listOf(
        HeaderRule(::spanProvider),
        HeaderLineRule(::spanProvider)
    )
  }

  @JvmStatic
  fun <R> createMarkdownRules(context: Context, @StyleRes headerStyles: List<Int>) =
      createHeaderRules<R>(context, headerStyles) + ListItemRule {
        BulletSpan(24, Color.parseColor("#6E7B7F"))
      }
}