package com.discord.simpleast.code

import com.discord.simpleast.code.CodeRules.toMatchGroupRule
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import java.util.regex.Matcher
import java.util.regex.Pattern

object Crystal {

  val KEYWORDS: Array<String> = arrayOf(
      "true|false|nil",
      "module|require|include|extend|lib",
      "abstract|private|protected",
      "annotation|class|finalize|new|initialize|allocate|self|super",
      "union|typeof|forall|is_a?|nil?|as?|as|responds_to?|alias|type",
      "property|getter|setter|struct|of",
      "previous_def|method|fun|enum|macro",
      "rescue|raise|begin|end|ensure",
      "if|else|elsif|then|unless|until",
      "for|in|of|do|when|select|with",
      "while|break|next|yield|case",
      "print|puts|return",
  )

  val BUILT_INS = arrayOf(
      "Nil|Bool|true|false|Void|NoReturn",
      "Number|BigDecimal|BigRational|BigFloat|BigInt",
      "Int|Int8|Int16|Int32|Int64|UInt8|UInt16|UInt32|UInt64|Float|Float32|Float64",
      "Char|String|Symbol|Regex",
      "StaticArray|Array|Set|Hash|Range|Tuple|NamedTuple|Union|BitArray",
      "Proc|Command|Enum|Class",
      "Reference|Value|Struct|Object|Pointer",
      "Exception|ArgumentError|KeyError|TypeCastError|IndexError|RuntimeError|NilAssertionError|InvalidBigDecimalException|NotImplementedError|OverflowError",
      "pointerof|sizeof|instance_sizeof|offsetof|uninitialized"
  )

  class FunctionNode<RC>(
      pre: String, signature: String, params: String?,
      codeStyleProviders: CodeStyleProviders<RC>
  ): Node.Parent<RC>(
      StyleNode.TextStyledNode(pre, codeStyleProviders.keywordStyleProvider),
      StyleNode.TextStyledNode(signature, codeStyleProviders.identifierStyleProvider),
      params?.let { StyleNode.TextStyledNode(it, codeStyleProviders.paramsStyleProvider) },
  ) {
    companion object {

      /**
       * Matches against a crystal function declaration
       *
       * ```
       * def initialize(val : T)
       * def increment(amount)
       * private def log
       * ```
       */
      private val PATTERN_CRYSTAL_FUNC =
          Pattern.compile("""^(def)( +\w+)( *\( *(?:@\w+ +: +\w*)?\w+(?: +[:=] +.*)? *\))?(?!.+)""")

      fun <RC, S> createFunctionRule(codeStyleProviders: CodeStyleProviders<RC>) =
        object : Rule<RC, Node<RC>, S>(PATTERN_CRYSTAL_FUNC) {
          override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>, S>, state: S): ParseSpec<RC, S> {
            val definition = matcher.group(1)
            val signature = matcher.group(2)
            val params = matcher.group(3)
            return ParseSpec.createTerminal(FunctionNode(definition!!, signature!!, params, codeStyleProviders), state)
          }
        }
    }
  }

  private val PATTERN_CRYSTAL_COMMENTS =
      Pattern.compile("""^(#.*)""")

  /**
   * Matches against crystal annotations
   *
   * ```
   * @[Annotation(key: "value")]
   * @[Annotation(1, 2, 3)]
   * @[Annotation]
   * ```
   */
  private val PATTERN_CRYSTAL_ANNOTATION =
      Pattern.compile("""^@\[(\w+)(?:\(.+\))?]""")

  /**
   * Matches against a crystal string or character
   *
   * ```
   * "hello"
   * "hello
   *        world"
   * 'a'
   * ```
   */
  private val PATTERN_CRYSTAL_STRINGS =
      Pattern.compile("""^"[\s\S]*?(?<!\\)"(?=\W|\s|$)""")

  /**
   * Matches against a crystal regex
   *
   * ```
   * /foo/m
   * ```
   */
  private val PATTERN_CRYSTAL_REGEX =
      Pattern.compile("""^/.*?/[imx]?""")

  /**
   * Matches against a crystal symbol
   *
   * ```
   * :symbol
   * :"quoted"
   * :<<
   */
  private val PATTERN_CRYSTAL_SYMBOL =
      Pattern.compile("""^(:"?(?:[+-/%&^|]|\*\*?|\w+|(?:<(?=[<=\s])[<=]?(?:(?<==)>)?|>(?=[>=\s])[>=]?(?:(?<==)>)?)|\[][?=]?|(?:!(?=[=~\s])[=~]?|=?(?:~|==?)))(?:(?<!\\)"(?=\s|$))?)""")

  internal fun <RC, S> createCrystalCodeRules(
      codeStyleProviders: CodeStyleProviders<RC>
  ): List<Rule<RC, Node<RC>, S>> =
      listOf(
          PATTERN_CRYSTAL_COMMENTS.toMatchGroupRule(stylesProvider = codeStyleProviders.commentStyleProvider),
          PATTERN_CRYSTAL_STRINGS.toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
          PATTERN_CRYSTAL_REGEX.toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
          PATTERN_CRYSTAL_ANNOTATION.toMatchGroupRule(stylesProvider = codeStyleProviders.genericsStyleProvider),
          PATTERN_CRYSTAL_SYMBOL.toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
          FunctionNode.createFunctionRule(codeStyleProviders),
      )
}