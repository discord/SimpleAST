package com.discord.simpleast.sample

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
import com.discord.simpleast.core.node.TextNode
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.markdown.MarkdownRules
import java.util.regex.Matcher

/**
 * Custom markdown rules to show potential of the framework if you have a bit of creativity.
 *
 * @see MarkdownRules.createMarkdownRules for the default setting
 */
object CustomMarkdownRules {

  /**
   * Searches for the pattern:
   *
   * `Some header title {capture this string}`
   */
  private val PATTERN_HEADING_CLASS = """^(.*) \{([\w ]+)\}\s*$""".toRegex().toPattern()

  fun <R> createMarkdownRules(context: Context,
                              @StyleRes headerStyles: List<Int>,
                              @StyleRes classStyles: List<Int>) =
      createHeaderRules<R>(context, headerStyles, classStyles) + MarkdownRules.ListItemRule {
        BulletSpan(24, Color.parseColor("#6E7B7F"))
      }

  private fun <R> createHeaderRules(context: Context,
                                    @StyleRes headerStyles: List<Int>,
                                    @StyleRes classStyles: List<Int>): List<Rule<R, Node<R>>> {
    fun spanProvider(header: Int): CharacterStyle =
        when (header) {
          0 -> TextAppearanceSpan(context, headerStyles[0])
          in 1..headerStyles.size -> TextAppearanceSpan(context, headerStyles[header - 1])
          else -> StyleSpan(Typeface.BOLD_ITALIC)
        }

    return listOf(
        MarkdownRules.HeaderRule(::spanProvider),
        HeaderLineClassedRule(::spanProvider) {className ->
          when (className) {
            "add" -> TextAppearanceSpan(context, classStyles[0])
            "remove" -> TextAppearanceSpan(context, classStyles[1])
            "fix" -> TextAppearanceSpan(context, classStyles[2])
            else -> null
          }
        }
    )
  }

  /**
   * Allow lined headers that specify custom styles via markdown.
   *
   * @see createClassedSuffixedRule
   */
  class HeaderLineClassedRule<R>(styleSpanProvider: (Int) -> CharacterStyle, classSpanProvider: (String) -> CharacterStyle?) :
      MarkdownRules.HeaderLineRule<R>(styleSpanProvider) {

    private val innerRules = listOf<Rule<R, Node<R>>>(
        createClassedSuffixedRule(classSpanProvider),
        SimpleMarkdownRules.createTextRule()
    )

    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>, isNested: Boolean): ParseSpec<R, Node<R>> {
      return if (isNested) {
        ParseSpec.createTerminal(TextNode(matcher.group()))
      } else {
        val children = parser.parse(matcher.group(1), true, innerRules)
        val node: Node<R> = if (children.size == 1) {
          children.first() as Node<R>
        } else {
          createHeaderStyleNode(matcher).apply {
            for (child in children) {
              addChild(child as Node<R>)
            }
          }
        }

        ParseSpec.createTerminal(node)
      }
    }

    private fun <T> createClassedSuffixedRule(classSpanProvider: (String) -> CharacterStyle?): Rule<T, Node<T>> =
        object : Rule<T, Node<T>>(PATTERN_HEADING_CLASS, true) {
          override fun parse(matcher: Matcher, parser: Parser<T, in Node<T>>, isNested: Boolean): ParseSpec<T, Node<T>> {
            val classes = matcher.group(2).split(' ')
            val classSpans = classes.mapNotNull(classSpanProvider)

            val node = StyleNode<T>(classSpans)
            return ParseSpec.createNonterminal(node, matcher.start(1), matcher.end(1))
          }
        }

  }
}