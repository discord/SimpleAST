package com.discord.simpleast.core.parser

import android.util.Log
import com.discord.simpleast.core.node.Node
import java.util.*


/**
 * @param R The render context, can be any object that holds what's required for rendering.
 *          See [Node.render]
 * @param T The type of nodes that are handled.
 * @param S The state of the current parser.
 */
open class Parser<R, T : Node<R>, S> @JvmOverloads constructor(private val enableDebugging: Boolean = false) {

  private val rules = ArrayList<Rule<R, out T, S>>()

  @Suppress("unused")
  fun addRule(rule: Rule<R, out T, S>) =
      this.apply { rules.add(rule) }

  fun addRules(vararg newRules: Rule<R, out T, S>) =
      this.addRules(newRules.asList())

  fun addRules(newRules: Collection<Rule<R, out T, S>>) =
      this.apply { rules.addAll(newRules) }

  /**
   * Transforms the [source] to a AST of [Node]s using the provided [rules].
   *
   * @param rules Ordered [List] of rules to use to convert the source to nodes.
   *    If not set, the parser will use its global list of [Parser.rules].
   *
   * @throws ParseException for certain specific error flows.
   */
  @JvmOverloads
  fun parse(
      source: CharSequence,
      initialState: S,
      rules: List<Rule<R, out T, S>> = this.rules
  ): MutableList<T> {
    val remainingParses = Stack<ParseSpec<R, S>>()
    val topLevelRootNode = Node<R>()

    var lastCapture: String? = null

    if (source.isNotEmpty()) {
      remainingParses.add(ParseSpec(topLevelRootNode, initialState, 0, source.length))
    }

    while (!remainingParses.isEmpty()) {
      val builder = remainingParses.pop()

      if (builder.startIndex >= builder.endIndex) {
        break
      }

      val inspectionSource = source.subSequence(builder.startIndex, builder.endIndex)
      val offset = builder.startIndex

      val (rule, matcher) = rules
          .firstMapOrNull { rule ->
            val matcher = rule.match(inspectionSource, lastCapture, builder.state)
            if (matcher == null) {
              logMiss(rule, inspectionSource)
              null
            } else {
              logMatch(rule, inspectionSource)
              rule to matcher
            }
          }
          ?: throw ParseException("failed to find rule to match source", source)

      val matcherSourceEnd = matcher.end() + offset
      val newBuilder = rule.parse(matcher, this, builder.state)

      val parent = builder.root
      parent.addChild(newBuilder.root)

      // In case the last match didn't consume the rest of the source for this subtree,
      // make sure the rest of the source is consumed.
      if (matcherSourceEnd != builder.endIndex) {
        remainingParses.push(ParseSpec.createNonterminal(parent, builder.state, matcherSourceEnd, builder.endIndex))
      }

      // We want to speak in terms of indices within the source string,
      // but the Rules only see the matchers in the context of the substring
      // being examined. Adding this offset addresses that issue.
      if (!newBuilder.isTerminal) {
        newBuilder.applyOffset(offset)
        remainingParses.push(newBuilder)
      }

      try {
        lastCapture = matcher.group(0)
      } catch (throwable: Throwable) {
        throw ParseException(message = "matcher found no matches", source = source, cause = throwable)
      }
    }

    @Suppress("UNCHECKED_CAST")  // Guaranteed by the rule's generic T
    val ast = topLevelRootNode.getChildren()?.toMutableList() as? MutableList<T>
    return ast ?: arrayListOf()
  }

  private fun <R, T: Node<R>, S> logMatch(rule: Rule<R, T, S>, source: CharSequence) {
    if (enableDebugging) {
      Log.i(TAG, "MATCH: with rule with pattern: " + rule.matcher.pattern().toString() + " to source: " + source)
    }
  }

  private fun <R, T: Node<R>, S> logMiss(rule: Rule<R, T, S>, source: CharSequence) {
    if (enableDebugging) {
      Log.i(TAG, "MISS: with rule with pattern: " + rule.matcher.pattern().toString() + " to source: " + source)
    }
  }

  companion object {

    private const val TAG = "Parser"
  }

  class ParseException(message: String, source: CharSequence?, cause: Throwable? = null)
    : RuntimeException("Error while parsing: $message \n Source: $source", cause)
}

private inline fun <T, V> List<T>.firstMapOrNull(predicate: (T) -> V?): V? {
  for (element in this) {
    @Suppress("UnnecessaryVariable")  // wants to inline, but it's unreadable that way
    val found = predicate(element) ?: continue
    return found
  }
  return null
}
