package com.discord.simpleast.markdown

import android.graphics.Color
import android.text.style.BulletSpan
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import com.discord.simpleast.markdown.node.MarkdownListItemNode
import java.util.regex.Matcher

/**
 * Support for full markdown representation of:
 * - headers
 * - list
 *
 * @see com.discord.simpleast.core.simple.SimpleMarkdownRules
 */
object MarkdownRules {
  val LIST_ITEM = """^\*[ \t](.*)(?=\n|$)""".toPattern()

  class ListItem<R> : Rule<R, Node<R>>(LIST_ITEM, false) {
    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>, isNested: Boolean)
        : ParseSpec<R, Node<R>> {
      val node = MarkdownListItemNode<R> {
        BulletSpan(24, Color.parseColor("#3F51B5"))
      }
      return ParseSpec.createNonterminal(node, matcher.start(1), matcher.end(1))
    }
  }

  @JvmStatic
  fun <R> createExtremeBRSTXXrdMarkdownRules() = listOf(ListItem<R>())
}