package com.discord.simpleast.code

import android.text.SpannableStringBuilder
import android.text.Spanned
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.node.TextNode
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
@Suppress("MemberVisibilityCanBePrivate")
object CodeRules {
  /**
   * Handles markdown syntax for code blocks for a given language.
   *
   * Examples:
   * inlined ```test```
   * inlined ```kt language code blocks need newline```
   * inlined block start ```
   * fun test()
   * ```
   *
   * ```kotlin
   * class Test: Runnable {
   *   override fun run() {
   *     val x = new BigInt(5)
   *   }
   * }
   * ```
   */
  val PATTERN_CODE_BLOCK: Pattern =
      Pattern.compile("""^```(?:([\w+\-.]+?)?(\s*\n))?([^\n].*?)\n*```""", Pattern.DOTALL)

  val PATTERN_CODE_INLINE: Pattern =
      Pattern.compile("""^`([^`]+)`|``([^`]+)``""", Pattern.DOTALL)

  private const val CODE_BLOCK_LANGUAGE_GROUP = 1
  private const val CODE_BLOCK_WS_PREFIX = 2
  private const val CODE_BLOCK_BODY_GROUP = 3

  /**
   * This is needed to simplify the other rule parsers to only need a leading pattern match.
   * We also don't want to remove extraneous newlines/ws like what the [SimpleMarkdownRules.createNewlineRule] does.
   */
  val PATTERN_LEADING_WS_CONSUMER: Pattern = Pattern.compile("""^(?:\n\s*)+""")

  /**
   * This is needed to simplify the other rule parsers to only need a leading pattern match.
   * The pattern splits on each token (symbol/word) unlike [SimpleMarkdownRules.createTextRule] which merges
   * symbols and words until another symbol is reached.
   */
  val PATTERN_TEXT: Pattern = Pattern.compile("""^[\s\S]+?(?=\b|[^0-9A-Za-z\s\u00c0-\uffff]|\n| {2,}\n|\w+:\S|$)""")

  val PATTERN_NUMBERS: Pattern = Pattern.compile("""^\b\d+?\b""")

  internal fun createWordPattern(vararg words: String) =
      Pattern.compile("""^\b(?:${words.joinToString("|")})\b""")

  fun <R, S> Pattern.toMatchGroupRule(
      group: Int = 0,
      stylesProvider: StyleNode.SpanProvider<R>? = null
  ) =
      object : Rule<R, Node<R>, S>(this) {
        override fun parse(
            matcher: Matcher, parser: Parser<R, in Node<R>, S>, state: S
        ): ParseSpec<R, S> {
          val content = matcher.group(group).orEmpty()
          val node = stylesProvider
              ?.let { StyleNode.TextStyledNode(content, it) }
              ?: TextNode(content)
          return ParseSpec.createTerminal(node, state)
        }
      }

  fun <R, S> createDefinitionRule(
      codeStyleProviders: CodeStyleProviders<R>, vararg identifiers: String) =
      object : Rule<R, Node<R>, S>(Pattern.compile("""^\b(${identifiers.joinToString("|")})(\s+\w+)""")) {
        override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>, S>, state: S): ParseSpec<R, S> {
          val definition = matcher.group(1)!!
          val signature = matcher.group(2)!!
          return ParseSpec.createTerminal(
              CodeNode.DefinitionNode(definition, signature, codeStyleProviders),
              state)
        }
      }

  fun <R, S> createCodeLanguageMap(codeStyleProviders: CodeStyleProviders<R>)
      : Map<String, List<Rule<R, Node<R>, S>>> {

    val kotlinRules = createGenericCodeRules<R, S>(
        codeStyleProviders,
        additionalRules = Kotlin.createKotlinCodeRules(codeStyleProviders),
        definitions = arrayOf("object", "class", "interface"),
        builtIns = Kotlin.BUILT_INS,
        keywords = Kotlin.KEYWORDS)

    val protoRules = createGenericCodeRules<R, S>(
        codeStyleProviders,
        additionalRules = listOf(
            createSingleLineCommentPattern("//")
                .toMatchGroupRule(stylesProvider = codeStyleProviders.commentStyleProvider),
            Pattern.compile("""^"[\s\S]*?(?<!\\)"(?=\W|\s|$)""")
                .toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
        ),
        definitions = arrayOf("message|enum|extend|service"),
        builtIns = arrayOf("true|false",
            "string|bool|double|float|bytes",
            "int32|uint32|sint32|int64|unit64|sint64",
            "map"),
        "required|repeated|optional|option|oneof|default|reserved",
        "package|import",
        "rpc|returns")

    val pythonRules = createGenericCodeRules<R, S>(
        codeStyleProviders,
        additionalRules = listOf(
            createSingleLineCommentPattern("#")
                .toMatchGroupRule(stylesProvider = codeStyleProviders.commentStyleProvider),
            Pattern.compile("""^"[\s\S]*?(?<!\\)"(?=\W|\s|$)""")
                    .toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
            Pattern.compile("""^'[\s\S]*?(?<!\\)'(?=\W|\s|$)""")
                    .toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
            Pattern.compile("""^@(\w+)""")
                .toMatchGroupRule(stylesProvider = codeStyleProviders.genericsStyleProvider)),
        definitions = arrayOf("class", "def", "lambda"),
        builtIns = arrayOf("True|False|None"),
        "from|import|global|nonlocal",
        "async|await|class|self|cls|def|lambda",
        "for|while|if|else|elif|break|continue|return",
        "try|except|finally|raise|pass|yeild",
        "in|as|is|del",
        "and|or|not|assert",
    )

    val rustRules = createGenericCodeRules<R, S>(
        codeStyleProviders,
        additionalRules = listOf(
            createSingleLineCommentPattern("//")
                .toMatchGroupRule(stylesProvider = codeStyleProviders.commentStyleProvider),
            Pattern.compile("""^"[\s\S]*?(?<!\\)"(?=\W|\s|$)""")
                .toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
            Pattern.compile("""^#!?\[.*?\]\n""")
                .toMatchGroupRule(stylesProvider = codeStyleProviders.genericsStyleProvider)),
        definitions = arrayOf("struct", "trait", "mod"),
        builtIns = arrayOf(
            "Self|Result|Ok|Err|Option|None|Some",
            "Copy|Clone|Eq|Hash|Send|Sync|Sized|Debug|Display",
            "Arc|Rc|Box|Pin|Future",
            "true|false|bool|usize|i64|u64|u32|i32|str|String"
        ),
        "let|mut|static|const|unsafe",
        "crate|mod|extern|pub|pub(super)|use",
        "struct|enum|trait|type|where|impl|dyn|async|await|move|self|fn",
        "for|while|loop|if|else|match|break|continue|return|try",
        "in|as|ref",
    )

    val xmlRules = listOf<Rule<R, Node<R>, S>>(
        Xml.PATTERN_XML_COMMENT
            .toMatchGroupRule(stylesProvider = codeStyleProviders.commentStyleProvider),
        Xml.createTagRule(codeStyleProviders),
        PATTERN_LEADING_WS_CONSUMER.toMatchGroupRule(),
        PATTERN_TEXT.toMatchGroupRule(),
    )

    val queryLangauge = listOf<Rule<R, Node<R>, S>>(
        createSingleLineCommentPattern("#")
            .toMatchGroupRule(stylesProvider = codeStyleProviders.commentStyleProvider),
        Pattern.compile("""^"[\s\S]*?(?<!\\)"(?=\W|\s|$)""")
            .toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
        createWordPattern("true|false|null").pattern().toPattern(Pattern.CASE_INSENSITIVE)
            .toMatchGroupRule(stylesProvider = codeStyleProviders.genericsStyleProvider),
        createWordPattern(
            "select|from|join|where|and|as|distinct|count|avg",
            "order by|group by|desc|sum|min|max",
            "like|having|in|is|not").pattern().toPattern(Pattern.CASE_INSENSITIVE)
            .toMatchGroupRule(stylesProvider = codeStyleProviders.keywordStyleProvider),
        PATTERN_NUMBERS.toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
        PATTERN_LEADING_WS_CONSUMER.toMatchGroupRule(),
        PATTERN_TEXT.toMatchGroupRule(),
    )

    val crystalRules = createGenericCodeRules<R, S>(
        codeStyleProviders,
        additionalRules = Crystal.createCrystalCodeRules(codeStyleProviders),
        definitions = arrayOf("def", "class"),
        builtIns = Crystal.BUILT_INS,
        keywords = Crystal.KEYWORDS)
        
    val javascriptRules = createGenericCodeRules<R, S>(
        codeStyleProviders,
        additionalRules = JavaScript.createCodeRules(codeStyleProviders),
        definitions = arrayOf("class"),
        builtIns = JavaScript.BUILT_INS,
        keywords = JavaScript.KEYWORDS)

    val goRules = createGenericCodeRules<R, S>(
        codeStyleProviders,
        additionalRules = listOf(
            Pattern.compile("""^(?:(?://.*?(?=\n|$))|(/\*.*?\*/))""", Pattern.DOTALL)
                .toMatchGroupRule(stylesProvider = codeStyleProviders.commentStyleProvider),
            Pattern.compile("""^(".*?(?<!\\)"|`[\s\S]*?(?<!\\)`)(?=\W|\s|$)""")
                .toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
            Pattern.compile("""^func""")
                .toMatchGroupRule(stylesProvider = codeStyleProviders.keywordStyleProvider),
            Pattern.compile("""^[a-z0-9_]+(?=\()""", Pattern.CASE_INSENSITIVE)
                .toMatchGroupRule(stylesProvider = codeStyleProviders.identifierStyleProvider),
        ),
        definitions = arrayOf("package", "type", "var|const"),
        builtIns = arrayOf(
            "nil|iota|true|false|bool",
            "byte|complex(?:64|128)|error|float(?:32|64)|rune|string|u?int(?:8|16|32|64)?|uintptr"
        ),
        "break|case|chan|continue|default|defer|else|fallthrough|for|go(?:to)?|if|import|interface|map|range|return|select|switch|struct",
        "type|var|const"
    )

    return mapOf(
        "kt" to kotlinRules,
        "kotlin" to kotlinRules,

        "protobuf" to protoRules,
        "proto" to protoRules,
        "pb" to protoRules,

        "py" to pythonRules,
        "python" to pythonRules,

        "rs" to rustRules,
        "rust" to rustRules,

        "cql" to queryLangauge,
        "sql" to queryLangauge,

        "xml" to xmlRules,
        "http" to xmlRules,

        "cr" to crystalRules,
        "crystal" to crystalRules,
      
        "js" to javascriptRules,
        "javascript" to javascriptRules,

        "go" to goRules,
    )
  }

  private fun createSingleLineCommentPattern(prefix: String) =
      Pattern.compile("""^(?:$prefix.*?(?=\n|$))""")

  private fun <R, S> createGenericCodeRules(
      codeStyleProviders: CodeStyleProviders<R>,
      additionalRules: List<Rule<R, Node<R>, S>>,
      definitions: Array<String>, builtIns: Array<String>, vararg keywords: String
  ): List<Rule<R, Node<R>, S>> =
      additionalRules +
          listOf(
              createDefinitionRule(codeStyleProviders, *definitions),
              createWordPattern(*builtIns).toMatchGroupRule(stylesProvider = codeStyleProviders.genericsStyleProvider),
              createWordPattern(*keywords).toMatchGroupRule(stylesProvider = codeStyleProviders.keywordStyleProvider),
              PATTERN_NUMBERS.toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
              PATTERN_LEADING_WS_CONSUMER.toMatchGroupRule(),
              PATTERN_TEXT.toMatchGroupRule(),
          )

  /**
   * @param textStyleProvider appearance of the text inside the block
   * @param languageMap maps language identifer to a list of rules to parse the syntax
   * @param wrapperNodeProvider set if you want to provide additional styling on the code representation.
   *    Useful for setting code blocks backgrounds.
   */
  fun <R, S> createCodeRule(
      textStyleProvider: StyleNode.SpanProvider<R>,
      languageMap: Map<String, List<Rule<R, Node<R>, S>>>,
      wrapperNodeProvider: (CodeNode<R>, Boolean, S) -> Node<R> =
          @Suppress("UNUSED_ANONYMOUS_PARAMETER")
          { codeNode, startsWithNewline, state -> codeNode }
  ): Rule<R, Node<R>, S> {
    return object : Rule<R, Node<R>, S>(PATTERN_CODE_BLOCK) {
      override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>, S>, state: S)
          : ParseSpec<R, S> {
        val language = matcher.group(CODE_BLOCK_LANGUAGE_GROUP)
        val codeBody = matcher.group(CODE_BLOCK_BODY_GROUP).orEmpty()
        val startsWithNewline = matcher.group(CODE_BLOCK_WS_PREFIX)?.contains('\n') ?: false

        val languageRules = language?.let { languageMap[it] }

        val content = languageRules?.let {
          @Suppress("UNCHECKED_CAST")
          val children = parser.parse(codeBody, state, languageRules) as List<Node<R>>
          CodeNode.Content.Parsed(codeBody, children)
        } ?: CodeNode.Content.Raw(codeBody)

        val codeNode = CodeNode(content, language, textStyleProvider)
        return ParseSpec.createTerminal(wrapperNodeProvider(codeNode, startsWithNewline, state), state)
      }
    }
  }

  fun <R, S> createInlineCodeRule(
      textStyleProvider: StyleNode.SpanProvider<R>,
      bgStyleProvider: StyleNode.SpanProvider<R>,

  ): Rule<R, Node<R>, S> {
    return object : Rule<R, Node<R>, S>(PATTERN_CODE_INLINE) {
      override fun parse(matcher: Matcher, parser: Parser<R, in Node<R>, S>, state: S)
          : ParseSpec<R, S> {
        val codeBody = matcher.group(1).orEmpty()
        if (codeBody.isEmpty()) {
          return ParseSpec.createTerminal(TextNode(matcher.group()), state)
        }

        val content = CodeNode.Content.Raw(codeBody)

        val codeNode = CodeNode(content, null, textStyleProvider)
        // We can't use a StyleNode here as we can't share background spans.
        val node = object : Node.Parent<R>(codeNode) {
          override fun render(builder: SpannableStringBuilder, renderContext: R) {
            val startIndex = builder.length
            super.render(builder, renderContext)
            bgStyleProvider.get(renderContext).forEach {
              builder.setSpan(it, startIndex, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
          }
        }
        return ParseSpec.createTerminal(node, state)
      }
    }
  }
}
