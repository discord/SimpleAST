/*
 * ActionScript grammar adapted from highlight.js (src/languages/actionscript.js).
 * Copyright (c) 2006, Ivan Sagalaev and highlight.js contributors.
 * Licensed under the BSD 3-Clause License.
 */

package com.discord.simpleast.code

import com.discord.simpleast.code.CodeRules.toMatchGroupRule
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * ActionScript 3 (AS3) syntax highlighting rules.
 */
object ActionScript {

  val KEYWORDS: Array<String> = arrayOf(
      "as|break|case|catch|class|const|continue|default|delete|do|dynamic|each|else",
      "extends|final|finally|for|function|get|if|implements|import|in|include",
      "instanceof|interface|internal|is|namespace|native|new|override|package|private",
      "protected|public|return|set|static|super|switch|this|throw|try|typeof|use",
      "var|void|while|with",
      "true|false|null|undefined"
  )

  /**
   * Matches package declarations (e.g. `package com.discord.sample`).
   */
  class PackageNode<RC>(
      keyword: String,
      packageName: String?,
      codeStyleProviders: CodeStyleProviders<RC>
  ) : Node.Parent<RC>(
      StyleNode.TextStyledNode(keyword, codeStyleProviders.keywordStyleProvider),
      packageName?.let { StyleNode.TextStyledNode(it, codeStyleProviders.typesStyleProvider) }
  ) {
    companion object {
      private val PATTERN_ACTIONSCRIPT_PACKAGE =
          Pattern.compile("""^(package)(\s+[a-zA-Z_$][a-zA-Z0-9_$.]*)?""")

      fun <RC, S> createPackageRule(codeStyleProviders: CodeStyleProviders<RC>) =
          object : Rule<RC, Node<RC>, S>(PATTERN_ACTIONSCRIPT_PACKAGE) {
            override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>, S>, state: S): ParseSpec<RC, S> {
              val keyword = matcher.group(1)!!
              val pkgName = matcher.group(2)
              return ParseSpec.createTerminal(PackageNode(keyword, pkgName, codeStyleProviders), state)
            }
          }
    }
  }

  /**
   * Matches class/interface headers and inheritance clauses (`class`, `interface`, `extends`, `implements`).
   */
  class ClassHeritageNode<RC>(
      keyword: String,
      identifier: String,
      codeStyleProviders: CodeStyleProviders<RC>
  ) : Node.Parent<RC>(
      StyleNode.TextStyledNode(keyword, codeStyleProviders.keywordStyleProvider),
      StyleNode.TextStyledNode(identifier, codeStyleProviders.typesStyleProvider)
  ) {
    companion object {
      private val PATTERN_ACTIONSCRIPT_HERITAGE =
          Pattern.compile("""^(class|interface|extends|implements)(\s+[a-zA-Z_$][a-zA-Z0-9_$]*)""")

      fun <RC, S> createClassHeritageRule(codeStyleProviders: CodeStyleProviders<RC>) =
          object : Rule<RC, Node<RC>, S>(PATTERN_ACTIONSCRIPT_HERITAGE) {
            override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>, S>, state: S): ParseSpec<RC, S> {
              val keyword = matcher.group(1)!!
              val identifier = matcher.group(2)!!
              return ParseSpec.createTerminal(ClassHeritageNode(keyword, identifier, codeStyleProviders), state)
            }
          }
    }
  }

  /**
   * Matches function declarations, getter/setters, signatures, and parameter lists.
   */
  class FunctionNode<RC>(
      modifier: String?,
      pre: String,
      signature: String?,
      params: String,
      codeStyleProviders: CodeStyleProviders<RC>
  ) : Node.Parent<RC>(
      modifier?.let { StyleNode.TextStyledNode(modifier, codeStyleProviders.keywordStyleProvider) },
      StyleNode.TextStyledNode(pre, codeStyleProviders.keywordStyleProvider),
      signature?.let { StyleNode.TextStyledNode(signature, codeStyleProviders.identifierStyleProvider) },
      StyleNode.TextStyledNode(params, codeStyleProviders.paramsStyleProvider)
  ) {
    companion object {
      private val PATTERN_ACTIONSCRIPT_FUNC =
          """^((?:(?:public|private|protected|internal|static|final|override)\s+)+)?(function(?:\s+[gs]et)?)(\s+[a-zA-Z_$][a-zA-Z0-9_$]*)?(\s*\(.*?\))""".toRegex(RegexOption.DOT_MATCHES_ALL).toPattern()

      fun <RC, S> createFunctionRule(codeStyleProviders: CodeStyleProviders<RC>) =
          object : Rule<RC, Node<RC>, S>(PATTERN_ACTIONSCRIPT_FUNC) {
            override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>, S>, state: S): ParseSpec<RC, S> {
              val modifier = matcher.group(1)
              val pre = matcher.group(2)!!
              val signature = matcher.group(3)
              val params = matcher.group(4)!!
              return ParseSpec.createTerminal(FunctionNode(modifier, pre, signature, params, codeStyleProviders), state)
            }
          }
    }
  }

  private val PATTERN_ACTIONSCRIPT_COMMENTS =
      Pattern.compile("""^(?:(?://.*?(?=\n|$))|(/\*.*?\*/))""", Pattern.DOTALL)

  private val PATTERN_ACTIONSCRIPT_STRINGS =
      Pattern.compile("""^('.*?(?<!\\)'|".*?(?<!\\)")(?=\W|\s|$)""")

  private val PATTERN_ACTIONSCRIPT_REST_ARG =
      Pattern.compile("""^\.{3}[a-zA-Z_$][a-zA-Z0-9_$]*""")

  internal fun <RC, S> createCodeRules(
      codeStyleProviders: CodeStyleProviders<RC>
  ): List<Rule<RC, Node<RC>, S>> =
      listOf(
          PATTERN_ACTIONSCRIPT_COMMENTS.toMatchGroupRule(stylesProvider = codeStyleProviders.commentStyleProvider),
          PATTERN_ACTIONSCRIPT_STRINGS.toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
          PackageNode.createPackageRule(codeStyleProviders),
          ClassHeritageNode.createClassHeritageRule(codeStyleProviders),
          PATTERN_ACTIONSCRIPT_REST_ARG.toMatchGroupRule(stylesProvider = codeStyleProviders.paramsStyleProvider),
          FunctionNode.createFunctionRule(codeStyleProviders),
      )
}
