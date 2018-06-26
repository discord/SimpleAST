package com.discord.simpleast.core.parser

import android.util.Log
import com.discord.simpleast.core.node.Node
import java.util.*


/**
 * @param R The render context, can be any object that holds what's required for rendering.
 *          See [Node.render]
 * @param T The type of nodes that are handled.
 */
open class Parser<R, T : Node<R>> @JvmOverloads constructor(private val enableDebugging: Boolean = false) {

  private val rules = ArrayList<Rule<R, out T>>()

  fun <C : T> addRule(rule: Rule<R, C>): Parser<R, T> {
    rules.add(rule)
    return this
  }

  fun <C: T> addRules(rules: Collection<Rule<R, C>>): Parser<R, T> {
    for (rule in rules) {
      addRule(rule)
    }
    return this
  }

  /**
   * Transforms the [source] to a AST of [Node]s using the provided [rules].
   *
   * @param rules Ordered [List] of rules to use to convert the source to nodes.
   *    If not set, the parser will use its global list of [Parser.rules].
   */
  @JvmOverloads
  fun parse(source: CharSequence?, rules: List<Rule<R, out T>> = this.rules): MutableList<T> {
    val remainingParses = Stack<ParseSpec<R, out T>>()
    val topLevelNodes = ArrayList<T>()

    var lastCapture: String? = null

    if (source != null && !source.isEmpty()) {
      remainingParses.add(ParseSpec(null, 0, source.length))
    }

    while (!remainingParses.isEmpty()) {
      val builder = remainingParses.pop()

      if (builder.startIndex >= builder.endIndex) {
        break
      }

      val inspectionSource = source?.subSequence(builder.startIndex, builder.endIndex) ?: continue
      val offset = builder.startIndex

      var foundRule = false
      for (rule in rules) {
        val matcher = rule.match(inspectionSource, lastCapture)
        if (matcher != null) {
          logMatch(rule, inspectionSource)
          val matcherSourceEnd = matcher.end() + offset
          foundRule = true

          val newBuilder = rule.parse(matcher, this)
          val parent = builder.root

          newBuilder.root?.let {
            parent?.addChild(it) ?: topLevelNodes.add(it)
          }

          // In case the last match didn't consume the rest of the source for this subtree,
          // make sure the rest of the source is consumed.
          if (matcherSourceEnd != builder.endIndex) {
            remainingParses.push(ParseSpec.createNonterminal(parent, matcherSourceEnd, builder.endIndex))
          }

          // We want to speak in terms of indices within the source string,
          // but the Rules only see the matchers in the context of the substring
          // being examined. Adding this offset addresses that issue.
          if (!newBuilder.isTerminal) {
            newBuilder.applyOffset(offset)
            remainingParses.push(newBuilder)
          }

          lastCapture = matcher.group(0)
//          println("source: $inspectionSource -- depth: ${remainingParses.size}")

          break
        } else {
          logMiss(rule, inspectionSource)
        }
      }

      if (!foundRule) {
        throw RuntimeException("failed to find rule to match source: \"$inspectionSource\"")
      }
    }

    return topLevelNodes
  }

  private fun <R, T: Node<R>> logMatch(rule: Rule<R, T>, source: CharSequence) {
    if (enableDebugging) {
      Log.i(TAG, "MATCH: with rule with pattern: " + rule.matcher.pattern().toString() + " to source: " + source)
    }
  }

  private fun <R, T: Node<R>> logMiss(rule: Rule<R, T>, source: CharSequence) {
    if (enableDebugging) {
      Log.i(TAG, "MISS: with rule with pattern: " + rule.matcher.pattern().toString() + " to source: " + source)
    }
  }

  companion object {

    private val TAG = "Parser"
  }
}
