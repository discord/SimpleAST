package com.discord.simpleast.core.simple

import android.graphics.Typeface
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.node.TextNode
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

object SimpleMarkdownRules {

  val PATTERN_BOLD = Pattern.compile("^\\*\\*([\\s\\S]+?)\\*\\*(?!\\*)")
  val PATTERN_UNDERLINE = Pattern.compile("^__([\\s\\S]+?)__(?!_)")
  val PATTERN_STRIKETHRU = Pattern.compile("^~~(?=\\S)([\\s\\S]*?\\S)~~")
  val PATTERN_NEWLINE = Pattern.compile("""^(?:\n *)*\n""")
  val PATTERN_TEXT = Pattern.compile("^[\\s\\S]+?(?=[^0-9A-Za-z\\s\\u00c0-\\uffff]|\\n| {2,}\\n|\\w+:\\S|$)")
  val PATTERN_ESCAPE = Pattern.compile("^\\\\([^0-9A-Za-z\\s])")

  val PATTERN_ITALICS = Pattern.compile(
      // only match _s surrounding words.
      "^\\b_" + "((?:__|\\\\[\\s\\S]|[^\\\\_])+?)_" + "\\b" +
          "|" +
          // Or match *s that are followed by a non-space:
          "^\\*(?=\\S)(" +
          // Match any of:
          //  - `**`: so that bolds inside italics don't close the
          // italics
          //  - whitespace
          //  - non-whitespace, non-* characters
          "(?:\\*\\*|\\s+(?:[^*\\s]|\\*\\*)|[^\\s*])+?" +
          // followed by a non-space, non-* then *
          ")\\*(?!\\*)"
  )

  fun <R> createBoldRule(): Rule<R, Node<R>> =
      createSimpleStyleRule(PATTERN_BOLD) { listOf(StyleSpan(Typeface.BOLD)) }

  fun <R> createUnderlineRule(): Rule<R, Node<R>> =
      createSimpleStyleRule(PATTERN_UNDERLINE) { listOf(UnderlineSpan()) }

  fun <R> createStrikethruRule(): Rule<R, Node<R>> =
      createSimpleStyleRule(PATTERN_STRIKETHRU) { listOf(StrikethroughSpan()) }

  fun <R> createTextRule(): Rule<R, Node<R>> {
    return object : Rule<R, Node<R>>(PATTERN_TEXT) {
      override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>): ParseSpec<R, Node<R>> {
        val node = TextNode<R>(matcher.group())
        return ParseSpec.createTerminal(node)
      }
    }
  }
  fun <R> createNewlineRule(): Rule<R, Node<R>> {
    return object : Rule.BlockRule<R, Node<R>>(PATTERN_NEWLINE) {
      override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>): ParseSpec<R, Node<R>> {
        val node = TextNode<R>("\n")
        return ParseSpec.createTerminal(node)
      }
    }
  }

  fun <R> createEscapeRule(): Rule<R, Node<R>> {
    return object : Rule<R, Node<R>>(PATTERN_ESCAPE) {
      override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>): ParseSpec<R, Node<R>> {
        return ParseSpec.createTerminal(TextNode(matcher.group(1)))
      }
    }
  }

  fun <R> createItalicsRule(): Rule<R, Node<R>> {
    return object : Rule<R, Node<R>>(PATTERN_ITALICS) {
      override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>): ParseSpec<R, Node<R>> {
        val startIndex: Int
        val endIndex: Int
        val asteriskMatch = matcher.group(2)
        if (asteriskMatch != null && asteriskMatch.length > 0) {
          startIndex = matcher.start(2)
          endIndex = matcher.end(2)
        } else {
          startIndex = matcher.start(1)
          endIndex = matcher.end(1)
        }

        val styles = ArrayList<CharacterStyle>(1)
        styles.add(StyleSpan(Typeface.ITALIC))

        val node = StyleNode<R, CharacterStyle>(styles)
        return ParseSpec.createNonterminal(node, startIndex, endIndex)
      }
    }
  }

  @JvmOverloads @JvmStatic
  fun <R> createSimpleMarkdownRules(includeTextRule: Boolean = true): MutableList<Rule<R, Node<R>>> {
    val rules = ArrayList<Rule<R, Node<R>>>()
    rules.add(createEscapeRule())
    rules.add(createNewlineRule())
    rules.add(createBoldRule())
    rules.add(createUnderlineRule())
    rules.add(createItalicsRule())
    rules.add(createStrikethruRule())
    if (includeTextRule) {
      rules.add(createTextRule())
    }
    return rules
  }

  @JvmStatic
  fun <R> createSimpleStyleRule(pattern: Pattern, styleFactory: () -> List<CharacterStyle>) =
      object : Rule<R, Node<R>>(pattern) {
        override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>>): ParseSpec<R, Node<R>> {
          val node = StyleNode<R, CharacterStyle>(styleFactory())
          return ParseSpec.createNonterminal(node, matcher.start(1), matcher.end(1))
        }
      }
}

