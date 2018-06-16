package com.discord.simpleast.core.parser

import com.discord.simpleast.core.node.Node
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @param R The render context, can be any object that holds what's required for rendering.
 *          See [Node.render]
 * @param T The type of nodes that are handled.
 */
abstract class Rule<R, T : Node<R>>(val matcher: Matcher,
                                    val applyOnNestedParse: Boolean = false) {

  @JvmOverloads
  constructor(pattern: Pattern, applyOnNestedParse: Boolean = false) :
      this(pattern.matcher(""), applyOnNestedParse)

  open fun match(inspectionSource: CharSequence, lastCapture: String?): Matcher? {
    matcher.reset(inspectionSource)
    return if (matcher.find()) matcher else null
  }

  abstract fun parse(matcher: Matcher, parser: Parser<R, in T>, isNested: Boolean): ParseSpec<R, T>
}

