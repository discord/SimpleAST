package com.discord.simpleast.core.parser

import com.discord.simpleast.core.node.Node
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @param R The render context, can be any object that holds what's required for rendering.
 *          See [Node.render]
 * @param T The type of nodes that are handled.
 */
abstract class Rule<R, T : Node<R>>(val matcher: Matcher) {

  constructor(pattern: Pattern) : this(pattern.matcher(""))

  /**
   * Used to determine if the [Rule] applies to the [inspectionSource].
   *
   * @param inspectionSource Source string to apply the rule
   * @param lastCapture Last captured source occuring before [inspectionSource]
   *
   * @return a [Matcher] if the rule applies, else null
   */
  open fun match(inspectionSource: CharSequence, lastCapture: String?, state: Map<String, Any>): Matcher? {
    matcher.reset(inspectionSource)
    return if (matcher.find()) matcher else null
  }

  abstract fun parse(matcher: Matcher, parser: Parser<R, in T>, state: Map<String, Any>): ParseSpec<R, T>

  /**
   * A [Rule] that ensures that the [matcher] is only executed if the preceding capture was a newline.
   * e.g. this ensures that the regex parses from a newline.
   */
  abstract class BlockRule<R, T : Node<R>>(pattern: Pattern) : Rule<R, T>(pattern) {

    override fun match(inspectionSource: CharSequence, lastCapture: String?, state: Map<String, Any>): Matcher? {
      if (lastCapture?.endsWith('\n') != false) {
        return super.match(inspectionSource, lastCapture, state)
      }
      return null
    }
  }
}

