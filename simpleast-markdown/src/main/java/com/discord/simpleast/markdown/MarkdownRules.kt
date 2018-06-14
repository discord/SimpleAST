package com.discord.simpleast.markdown

import android.content.Context
import android.graphics.Color
import android.support.annotation.StyleRes
import android.text.style.BulletSpan
import android.text.style.CharacterStyle
import android.text.style.TextAppearanceSpan
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import com.discord.simpleast.markdown.node.MarkdownListItemNode
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Support for full markdown representations.
 *
 * @see com.discord.simpleast.core.simple.SimpleMarkdownRules
 */
object MarkdownRules {
  val LIST_ITEM = """^\*[ \t](.*)(?=\n|$)""".toPattern()
  val HEADER_ITEM = """^(#+)[ \t](.*)(?=\n|$)""".toPattern()
  val HEADER_ITEM_ALT = """^(.*)(?=(?:={3,})[\n$])""".toPattern()

  class ListItemRule<R> : Rule<R, Node<R>>(LIST_ITEM, false) {
    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>, isNested: Boolean)
        : ParseSpec<R, Node<R>> {
      val node = MarkdownListItemNode<R> {
        BulletSpan(24, Color.parseColor("#3F51B5"))
      }
      return ParseSpec.createNonterminal(node, matcher.start(1), matcher.end(1))
    }
  }

  sealed class HeaderRuleBase<R>(pattern: Pattern, private val groupIndex: Int, private val styleSpan: CharacterStyle) :
      Rule<R, Node<R>>(pattern, false) {

    class HeaderRule<R>(styleSpan: CharacterStyle) : HeaderRuleBase<R>(HEADER_ITEM, 2, styleSpan)
    class HeaderRuleAlt<R>(styleSpan: CharacterStyle) : HeaderRuleBase<R>(HEADER_ITEM_ALT, 1, styleSpan)

    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>, isNested: Boolean)
        : ParseSpec<R, Node<R>> {
      val node = StyleNode<R>(listOf(styleSpan))
      return ParseSpec.createNonterminal(node, matcher.start(groupIndex), matcher.end(groupIndex))
    }
  }

  @JvmStatic
  fun <R> createHeaderRules(context: Context, @StyleRes headerStyles: List<Int>) = listOf<HeaderRuleBase<R>>(
      HeaderRuleBase.HeaderRule(TextAppearanceSpan(context,headerStyles.first())),
      HeaderRuleBase.HeaderRuleAlt(TextAppearanceSpan(context,headerStyles.first()))
  )

  @JvmStatic
  fun <R> createExtremeBRSTXXrdMarkdownRules(context: Context, @StyleRes headerStyles: List<Int>) =
      createHeaderRules<R>(context, headerStyles) + ListItemRule<R>()
}