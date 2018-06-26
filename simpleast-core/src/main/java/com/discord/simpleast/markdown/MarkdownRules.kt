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
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.markdown.MarkdownRules.HeaderLineClassedRule.Companion.createClassedSuffixedRule
import java.util.regex.Matcher
import java.util.regex.Pattern

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
  val PATTERN_LIST_ITEM = """^\*[ \t](.*)(?=\n|$)""".toPattern()
  /**
   * Handles markdown header syntax. Must have a whitespace after header character `#`
   * Example:
   * ```
   * # Header 1
   * ## Header 2
   * ### Header 3
   * ```
   */
  val PATTERN_HEADER_ITEM = """^ *(#+)[ \t](.*) *(?=\n|$)""".toPattern()
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
  val PATTERN_HEADER_ITEM_ALT = """^(?: \t)*(.+)\n *(=|-){3,} *(?=\n|$)""".toPattern()

  /**
   * Searches for the pattern:
   *
   * `Some header title {capture this string}`
   */
  private val PATTERN_HEADING_CLASS = """^(.*) \{([\w ]+)\}\s*$""".toRegex().toPattern()

  class ListItemRule<R>(private val bulletSpanProvider: () -> BulletSpan) :
      Rule.BlockRule<R, Node<R>>(PATTERN_LIST_ITEM) {

    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>)
        : ParseSpec<R, Node<R>> {
      val node = MarkdownListItemNode<R>(bulletSpanProvider)
      return ParseSpec.createNonterminal(node, matcher.start(1), matcher.end(1))
    }
  }

  open class HeaderRule<R>(pattern: Pattern,
                           protected val styleSpanProvider: (Int) -> CharacterStyle) :
      Rule.BlockRule<R, Node<R>>(pattern) {

    constructor(styleSpanProvider: (Int) -> CharacterStyle) : this(PATTERN_HEADER_ITEM, styleSpanProvider)

    protected open fun createHeaderStyleNode(matcher: Matcher): StyleNode<R, CharacterStyle> {
      val firstGroup = matcher.group(1)
      val numHeaderIndicators = firstGroup.length
      return StyleNode(listOf(styleSpanProvider(numHeaderIndicators)))
    }

    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>): ParseSpec<R, Node<R>> =
        ParseSpec.createNonterminal(
            createHeaderStyleNode(matcher),
            matcher.start(2), matcher.end(2))
  }

  open class HeaderLineRule<R>(styleSpanProvider: (Int) -> CharacterStyle) :
      HeaderRule<R>(PATTERN_HEADER_ITEM_ALT, styleSpanProvider) {

    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>)
        : ParseSpec<R, Node<R>> = ParseSpec.createNonterminal(
        createHeaderStyleNode(matcher), matcher.start(1), matcher.end(1))

    override fun createHeaderStyleNode(matcher: Matcher): StyleNode<R, CharacterStyle> {
      val headerStyleGroup = matcher.group(2)
      val headerIndicator = when (headerStyleGroup) {
        "=" -> 1
        else -> 2
      }
      return StyleNode(listOf(styleSpanProvider(headerIndicator)))
    }
  }

  /**
   * Allow [HeaderLineRule]'s to specify custom styles via markdown.
   *
   * This is not part of the markdown specification but is useful for flexible headers.
   *
   * Example:
   * ```
   * My Line Header in Red {red}
   * ==========
   * ```
   *
   * @param R RenderContext
   * @param T type of span applied for classes
   * @see createClassedSuffixedRule
   */
  open class HeaderLineClassedRule<R, T : Any>(styleSpanProvider: (Int) -> CharacterStyle,
                                               @Suppress("MemberVisibilityCanBePrivate")
                                               protected val innerRules: List<Rule<R, Node<R>>>) :
      MarkdownRules.HeaderLineRule<R>(styleSpanProvider) {

    constructor(styleSpanProvider: (Int) -> CharacterStyle, classSpanProvider: (String) -> T?) :
        this(styleSpanProvider,
            listOf(createClassedSuffixedRule<R, T>(classSpanProvider))
                + SimpleMarkdownRules.createSimpleMarkdownRules<R>(false)
                + SimpleMarkdownRules.createTextRule())

    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>): ParseSpec<R, Node<R>> {
      // Allow the classSuffix rule to apply first, then the normal parsers
      val children = parser.parse(matcher.group(1), innerRules)

      @Suppress("UNCHECKED_CAST")
      val node: Node<R> = if (children.size == 1) {
        children.first() as Node<R>
      } else {
        createHeaderStyleNode(matcher).apply {
          for (child in children) {
            addChild(child as Node<R>)
          }
        }
      }
      return ParseSpec.createTerminal(node)
    }

    companion object {
      @JvmStatic
      private fun <RC, T : Any> createClassedSuffixedRule(classSpanProvider: (String) -> T?): Rule<RC, Node<RC>> =
          object : Rule<RC, Node<RC>>(PATTERN_HEADING_CLASS) {
            override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>>)
                : ParseSpec<RC, Node<RC>> {
              val classes = matcher.group(2).split(' ')
              val classSpans = classes.mapNotNull { classSpanProvider(it) }

              val node = StyleNode<RC, T>(classSpans)
              return ParseSpec.createNonterminal(node, matcher.start(1), matcher.end(1))
            }
          }
    }
  }

  @Suppress("MemberVisibilityCanBePrivate")
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