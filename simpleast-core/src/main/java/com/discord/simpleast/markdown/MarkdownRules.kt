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
  val PATTERN_HEADER_ITEM = """^\s*(#+)[ \t](.*) *(?=\n|$)""".toPattern()
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
  val PATTERN_HEADER_ITEM_ALT = """^\s*(.+)\n *(=|-){3,} *(?=\n|$)""".toPattern()
  /**
   * Similar to [PATTERN_HEADER_ITEM_ALT] but allows specifying a class type annotation for styling
   * at the end of the line.
   * Example:
   * ```
   * Alternative Header 1 {red large}
   * ====================
   * ```
   */
  val PATTERN_HEADER_ITEM_ALT_CLASSED =
      """^\s*(?:(?:(.+)(?: +\{([\w ]*)\}))|(.*))[ \t]*\n *([=\-]){3,}[ \t]*(?=\n|$)""".toRegex().toPattern()

  class ListItemRule<R>(private val bulletSpanProvider: () -> BulletSpan) :
      Rule.BlockRule<R, Node<R>>(PATTERN_LIST_ITEM) {

    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>, state: Map<String, Any>)
        : ParseSpec<R, Node<R>> {
      val node = MarkdownListItemNode<R>(bulletSpanProvider)
      return ParseSpec.createNonterminal(node, state, matcher.start(1), matcher.end(1))
    }
  }

  open class HeaderRule<R>(pattern: Pattern,
                           protected val styleSpanProvider: (Int) -> CharacterStyle) :
      Rule.BlockRule<R, Node<R>>(pattern) {

    constructor(styleSpanProvider: (Int) -> CharacterStyle) : this(PATTERN_HEADER_ITEM, styleSpanProvider)

    protected open fun createHeaderStyleNode(headerStyleGroup: String): StyleNode<R, CharacterStyle> {
      val numHeaderIndicators = headerStyleGroup.length
      return StyleNode(listOf(styleSpanProvider(numHeaderIndicators)))
    }

    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>, state: Map<String, Any>): ParseSpec<R, Node<R>> =
        ParseSpec.createNonterminal(
                createHeaderStyleNode(matcher.group(1)),
                state, matcher.start(2), matcher.end(2))
  }

  open class HeaderLineRule<R>(pattern: Pattern = PATTERN_HEADER_ITEM_ALT, styleSpanProvider: (Int) -> CharacterStyle) :
      HeaderRule<R>(pattern, styleSpanProvider) {

    override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>, state: Map<String, Any>)
        : ParseSpec<R, Node<R>> = ParseSpec.createNonterminal(
            createHeaderStyleNode(matcher.group(2)), state, matcher.start(1), matcher.end(1))

    override fun createHeaderStyleNode(headerStyleGroup: String): StyleNode<R, CharacterStyle> {
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
   * @param RC RenderContext
   * @param T type of span applied for classes
   * @see PATTERN_HEADER_ITEM_ALT_CLASSED
   */
  open class HeaderLineClassedRule<RC, T : Any>(styleSpanProvider: (Int) -> CharacterStyle,
                                               @Suppress("MemberVisibilityCanBePrivate")
                                               val classSpanProvider: (String) -> T?,
                                               @Suppress("MemberVisibilityCanBePrivate")
                                               protected val innerRules: List<Rule<RC, Node<RC>>>) :
      MarkdownRules.HeaderLineRule<RC>(PATTERN_HEADER_ITEM_ALT_CLASSED, styleSpanProvider) {

    constructor(styleSpanProvider: (Int) -> CharacterStyle, classSpanProvider: (String) -> T?) :
        this(styleSpanProvider, classSpanProvider,
            SimpleMarkdownRules.createSimpleMarkdownRules<RC>(false)
                + SimpleMarkdownRules.createTextRule())

    override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>>, state: Map<String, Any>): ParseSpec<RC, Node<RC>> {
      val defaultStyleNode = createHeaderStyleNode(matcher.group(4))
      val headerBody = matcher.group(1) ?: matcher.group(3)
      val children = parser.parse(headerBody, innerRules)
      @Suppress("UNCHECKED_CAST")
      children.forEach { defaultStyleNode.addChild(it as Node<RC>) }

      val classes = matcher.group(2)?.trim()?.split(' ')
      val classSpans = classes?.mapNotNull { classSpanProvider(it) } ?: emptyList()

      val headerNode = if (classSpans.isNotEmpty()) {
        // Apply class stylings last
        StyleNode<RC, T>(classSpans).apply { addChild(defaultStyleNode) }
      } else {
        defaultStyleNode
      }

      return ParseSpec.createTerminal(headerNode, state)
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
        HeaderLineRule(styleSpanProvider = ::spanProvider)
    )
  }

  @JvmStatic
  fun <R> createMarkdownRules(context: Context, @StyleRes headerStyles: List<Int>) =
      createHeaderRules<R>(context, headerStyles) + ListItemRule {
        BulletSpan(24, Color.parseColor("#6E7B7F"))
      }
}