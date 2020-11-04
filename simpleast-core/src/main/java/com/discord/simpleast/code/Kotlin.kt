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
 * Support for full syntax highlighting in kotlin
 *
 * @see CodeRules
 */
object Kotlin {

  val KEYWORDS: Array<String> = arrayOf(
      "public|private|internal|inline|lateinit|abstract|open|reified",
      "import|package",
      "class|interface|data|enum|sealed|object|typealias",
      "fun|override|this|super|where|constructor|init|param|delegate",
      "const|val|var|get|final|vararg|it",
      "return|break|continue|suspend",
      "for|while|do|if|else|when|try|catch|finally|throw",
      "in|out|is|as|typeof",
      "shr|ushr|shl|ushl",
      "true|false|null"
  )

  val BUILT_INS = arrayOf("true|false|Boolean|String|Char",
      "Int|UInt|Long|ULong|Float|Double|Byte|UByte|Short|UShort",
      "Self|Set|Map|MutableMap|List|MutableList|Array|Runnable|Unit",
      "arrayOf|listOf|mapOf|setOf|let|also|apply|run",
  )

  class FunctionNode<RC>(
      pre: String, generic: String?, signature: String, params: String,
      codeStyleProviders: CodeStyleProviders<RC>
  ) : Node.Parent<RC>(
      StyleNode.TextStyledNode(pre, codeStyleProviders.keywordStyleProvider),
      generic?.let { StyleNode.TextStyledNode(it, codeStyleProviders.genericsStyleProvider) },
      StyleNode.TextStyledNode(signature, codeStyleProviders.identifierStyleProvider),
      StyleNode.TextStyledNode(params, codeStyleProviders.paramsStyleProvider),
  ) {
    companion object {

      /**
       * Matches against a kotlin function declaration
       *
       * ```
       * fun <T> foo(x: T)
       * fun createPoint(x: Int, y: Int)
       * ```
       */
      private val PATTERN_KOTLIN_FUNC =
          """^(fun)( *<.*>)?( \w+)( *\(.*?\))""".toRegex(RegexOption.DOT_MATCHES_ALL).toPattern()

      fun <RC, S> createFunctionRule(codeStyleProviders: CodeStyleProviders<RC>) =
          object : Rule<RC, Node<RC>, S>(PATTERN_KOTLIN_FUNC) {
            override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>, S>, state: S): ParseSpec<RC, S> {
              val definition = matcher.group(1)
              val generic = matcher.group(2)
              val signature = matcher.group(3)
              val params = matcher.group(4)
              return ParseSpec.createTerminal(FunctionNode(definition!!, generic, signature!!, params!!, codeStyleProviders), state)
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
       * Matches against a kotlin field definitions
       *
       * ```
       * val x = 1
       * val p: Point = Point(1,2)
       * ```
       */
      private val PATTERN_KOTLIN_FIELD =
          Pattern.compile("""^(val|var)(\s+\w+)""", Pattern.DOTALL)

      fun <RC, S> createFieldRule(
          codeStyleProviders: CodeStyleProviders<RC>
      ) =
          object : Rule<RC, Node<RC>, S>(PATTERN_KOTLIN_FIELD) {
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

  private val PATTERN_KOTLIN_COMMENTS =
      Pattern.compile("""^(?:(?://.*?(?=\n|$))|(/\*.*?\*/))""", Pattern.DOTALL)

  /**
   * Matches against a kotlin annotations
   *
   * ```
   * @Annotation
   * ```
   */
  private val PATTERN_KOTLIN_ANNOTATION =
      Pattern.compile("""^@(\w+)""")

  /**
   * Matches against a kotlin string
   *
   * ```kt
   * call("hello")
   * """
   * hello world
   * """
   * ```
   */
  private val PATTERN_KOTLIN_STRINGS =
      Pattern.compile("""^"[\s\S]*?(?<!\\)"(?=\W|\s|$)""")

  internal fun <RC, S> createKotlinCodeRules(
      codeStyleProviders: CodeStyleProviders<RC>
  ): List<Rule<RC, Node<RC>, S>> =
      listOf(
          PATTERN_KOTLIN_COMMENTS.toMatchGroupRule(stylesProvider = codeStyleProviders.commentStyleProvider),
          PATTERN_KOTLIN_STRINGS.toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
          PATTERN_KOTLIN_ANNOTATION.toMatchGroupRule(stylesProvider = codeStyleProviders.genericsStyleProvider),
          FieldNode.createFieldRule(codeStyleProviders),
          FunctionNode.createFunctionRule(codeStyleProviders),
      )
}