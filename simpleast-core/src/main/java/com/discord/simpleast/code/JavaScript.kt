package com.discord.simpleast.code

import com.discord.simpleast.code.CodeRules.toMatchGroupRule
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import java.util.regex.Matcher
import java.util.regex.Pattern

object JavaScript {

  val KEYWORDS: Array<String> = arrayOf(
     "public|private|protected",
     "import|export|default|package",
     "class|enum|interface",
     "function|super|this|extends|implements|arguments",
     "var|let|const|static|get|set|new",
     "return|break|continue|yield|void",
     "if|else|for|while|do|switch|async|await|case|try|catch|finally|delete|throw|NaN|Infinity",
     "of|in|instanceof|typeof",
     "debugger|with",
     "true|false|null|undefined"
  )


  val BUILT_INS: Array<String> = arrayOf(
    "String|Boolean|RegExp|Number|Date|Math|JSON|Symbol",
    "Function|Promise",
    "Array|Object|Map|Set|Uint8Array|Uint16Array|Uint32Array|Uint8ClampedArray|Buffer",
    "console|process|require|isNaN|parseInt|parseFloat|encodeURI|decodeURI|encodeURIComponent|decodeURIComponent",
    "Error|SyntaxError|TypeError|RangeError|ReferenceError|EvalError|AggregateError"
  )

  class FunctionNode<RC>(
    pre: String?, signature: String?, params: String,
    codeStyleProviders: CodeStyleProviders<RC>
  ) : Node.Parent<RC>(
      pre?.let { StyleNode.TextStyledNode(pre, codeStyleProviders.keywordStyleProvider) },
      signature?.let { StyleNode.TextStyledNode(signature, codeStyleProviders.identifierStyleProvider) },
      StyleNode.TextStyledNode(params, codeStyleProviders.paramsStyleProvider)
  ) {
      companion object {
        /**
         * Matches against a JavaScript function declaration.
         *
         * ```
         * function foo(bar)
         * function baz()
         * ```
         */
         private val PATTERN_JAVASCRIPT_FUNC = 
             """^(function\*?|static|get|set)? *?(\w+)?( *?\(.*?\)) *?\{""".toRegex(RegexOption.DOT_MATCHES_ALL).toPattern()

         fun <RC, S> createFunctionRule(codeStyleProviders: CodeStyleProviders<RC>) =
          object : Rule<RC, Node<RC>, S>(PATTERN_JAVASCRIPT_FUNC) {
            override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>, S>, state: S): ParseSpec<RC, S> {
              val definition = matcher.group(1)
              val signature = matcher.group(2)
              val params = matcher.group(3)
              return ParseSpec.createTerminal(FunctionNode(definition, signature, params!!, codeStyleProviders), state)
            }
          }
    }
  }

  class FieldNode<RC>(
      definition: String, name: String,
      codeStyleProviders: CodeStyleProviders<RC>
  ) : Node.Parent<RC>(
      StyleNode.TextStyledNode(definition, codeStyleProviders.keywordStyleProvider),
      StyleNode.TextStyledNode(name, codeStyleProviders.identifierStyleProvider),
  ) {
    companion object {
      /**
       * Matches against a JavaScript field definition.
       *
       * ```
       * var x = 1;
       * let y = 5;
       * const z = 10;
       * ```
       */
      private val PATTERN_JAVASCRIPT_FIELD =
          Pattern.compile("""^(var|let|const)(\s+\w+)""", Pattern.DOTALL)

      fun <RC, S> createFieldRule(
          codeStyleProviders: CodeStyleProviders<RC>
      ) =
          object : Rule<RC, Node<RC>, S>(PATTERN_JAVASCRIPT_FIELD) {
            override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>, S>, state: S):
                ParseSpec<RC, S> {
              val definition = matcher.group(1)
              val name = matcher.group(2)
              return ParseSpec.createTerminal(
                  FieldNode(definition!!, name!!, codeStyleProviders), state)
            }
          }
    }
  }

  /**
   * Matches against a JavaScript regex.
   *
   * ```
   * /(.*)/
   * ```
   */
   private val PATTERN_JAVASCRIPT_REGEX = 
       Pattern.compile("""^/.*?/(?:\w*)?""", Pattern.DOTALL)

  /**
   * Matches against a JavaScript generic.
   *
   * ```
   * <pending>
   * ```
   */
  private val PATTERN_JAVASCRIPT_GENERIC = 
      Pattern.compile("""^<(.*)>""", Pattern.DOTALL)

  /**
   * Matches against a JavaScript comment.
   *
   * ```
   * // Hey there
   * /* Hello */
   * ```
   */
  private val PATTERN_JAVASCRIPT_COMMENTS = 
      Pattern.compile("""^(?:(?://.*?(?=\n|$))|(/\*.*?\*/))""", Pattern.DOTALL)

  /**
   * Matches against a JavaScript object property.
   *
   * ```
   * { foo: 'bar' }
   * ```
   */
  private val PATTERN_JAVASCRIPT_OBJECT_PROPERTY = 
      Pattern.compile("""^[\{\[\,]\s*?(.*):""", Pattern.DOTALL)

  /**
   * Matches against a JavaScript string.
   *
   * ```
   * 'Hi'
   * "Hello"
   * `Hey`
   * ```
   */
  private val PATTERN_JAVASCRIPT_STRINGS = 
      Pattern.compile("""^('[\s\S]*?(?<!\\)'|"[\s\S]*?(?<!\\)"|`[\s\S]*?(?<!\\)`)(?=\W|\s|$)""")

  internal fun <RC, S> createJavaScriptCodeRules(
      codeStyleProviders: CodeStyleProviders<RC>
  ): List<Rule<RC, Node<RC>, S>> =
      listOf(
          PATTERN_JAVASCRIPT_COMMENTS.toMatchGroupRule(stylesProvider = codeStyleProviders.commentStyleProvider),
          PATTERN_JAVASCRIPT_STRINGS.toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
          PATTERN_JAVASCRIPT_OBJECT_PROPERTY.toMatchGroupRule(stylesProvider = codeStyleProviders.genericsStyleProvider),
          PATTERN_JAVASCRIPT_GENERIC.toMatchGroupRule(stylesProvider = codeStyleProviders.genericsStyleProvider),
          PATTERN_JAVASCRIPT_REGEX.toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
          FieldNode.createFieldRule(codeStyleProviders),
          FunctionNode.createFunctionRule(codeStyleProviders),
      )
}
