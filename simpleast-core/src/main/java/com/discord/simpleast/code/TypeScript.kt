package com.discord.simpleast.code

import com.discord.simpleast.code.CodeRules.toMatchGroupRule
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import java.util.regex.Matcher
import java.util.regex.Pattern

object TypeScript {

  val KEYWORDS: Array<String> = arrayOf(
     "import|from|export|default|package",
     "class|enum",
     "function|super|extends|implements|arguments",
     "var|let|const|static|get|set|new",
     "return|break|continue|yield|void",
     "if|else|for|while|do|switch|async|await|case|try|catch|finally|delete|throw|NaN|Infinity",
     "of|in|instanceof|typeof",
     "debugger|with",
     "true|false|null|undefined",
     "type|as|interface|public|private|protected|module|declare|namespace",
     "abstract|keyof|readonly|is|asserts|infer|override|intrinsic"
  )

  val BUILT_INS: Array<String> = arrayOf(
    "String|Boolean|RegExp|Number|Date|Math|JSON|Symbol|BigInt|Atomics|DataView",
    "Function|Promise|Generator|GeneratorFunction|AsyncFunction|AsyncGenerator|AsyncGeneratorFunction",
    "Array|Object|Map|Set|WeakMap|WeakSet|Int8Array|Int16Array|Int32Array|Uint8Array|Uint16Array",
    "Uint32Array|Uint8ClampedArray|Float32Array|Float64Array|BigInt64Array|BigUint64Array|Buffer",
    "ArrayBuffer|SharedArrayBuffer",
    "Reflect|Proxy|Intl|WebAssembly",
    "console|process|require|isNaN|parseInt|parseFloat|encodeURI|decodeURI|encodeURIComponent",
    "decodeURIComponent|this|global|globalThis|eval|isFinite|module",
    "setTimeout|setInterval|clearTimeout|clearInterval|setImmediate|clearImmediate",
    "queueMicrotask|document|window",
    "Error|SyntaxError|TypeError|RangeError|ReferenceError|EvalError|InternalError|URIError",
    "AggregateError|escape|unescape|URL|URLSearchParams|TextEncoder|TextDecoder",
    "AbortController|AbortSignal|EventTarget|Event|MessageChannel",
    "MessagePort|MessageEvent|FinalizationRegistry|WeakRef",
    "regeneratorRuntime|performance",
    "Iterable|Iterator|IterableIterator",
    "Partial|Required|Readonly|Record|Pick|Omit|Exclude|Extract",
    "NonNullable|Parameters|ConstructorParameters|ReturnType",
    "InstanceType|ThisParameterType|OmitThisParameter",
    "ThisType|Uppercase|Lowercase|Capitalize|Uncapitalize"
  )
  
  val TYPES: Array<String> = arrayOf(
    "string|number|boolean|object|symbol|any|unknown|bigint|never"
  )

  class FunctionNode<RC>(
    pre: String, signature: String?, generics: String?,
    codeStyleProviders: CodeStyleProviders<RC>
  ) : Node.Parent<RC>(
      StyleNode.TextStyledNode(pre, codeStyleProviders.keywordStyleProvider),
      signature?.let { StyleNode.TextStyledNode(signature, codeStyleProviders.identifierStyleProvider) },
      generics?.let { StyleNode.TextStyledNode(generics, codeStyleProviders.genericsStyleProvider) },
  ) {
      companion object {
        /**
         * Matches against a TypeScript function declaration.
         *
         * ```
         * function foo(bar: string)
         * function baz()
         * function control<T extends any>(target: T): T
         * async test()
         * static nice()
         * function* generator()
         * get token()
         * set internals()
         * ```
         */
         private val PATTERN_TYPESCRIPT_FUNC = 
             """^((?:function\*?|static|get|set|async)\s)(\s*[a-zA-Z_$][a-zA-Z0-9_$]*)?(\s*<.*>)?""".toRegex(RegexOption.DOT_MATCHES_ALL).toPattern()

         fun <RC, S> createFunctionRule(codeStyleProviders: CodeStyleProviders<RC>) =
          object : Rule<RC, Node<RC>, S>(PATTERN_TYPESCRIPT_FUNC) {
            override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>, S>, state: S): ParseSpec<RC, S> {
              val pre = matcher.group(1)
              val signature = matcher.group(2)
              val generics = matcher.group(3)
              return ParseSpec.createTerminal(FunctionNode(pre!!, signature, generics, codeStyleProviders), state)
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
       * Matches against a TypeScript field definition.
       *
       * ```
       * var x = 1;
       * let y = 5;
       * const z = 10;
       * const h: string = 'Hello world';
       * ```
       */
      private val PATTERN_TYPESCRIPT_FIELD =
          Pattern.compile("""^(var|let|const)(\s+[a-zA-Z_$][a-zA-Z0-9_$]*)""")

      fun <RC, S> createFieldRule(
          codeStyleProviders: CodeStyleProviders<RC>
      ) =
          object : Rule<RC, Node<RC>, S>(PATTERN_TYPESCRIPT_FIELD) {
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

  class ObjectPropertyNode<RC>(
      prefix: String, accessModifier: String?, property: String, suffix: String,
      codeStyleProviders: CodeStyleProviders<RC>
  ) : Node.Parent<RC>(
      StyleNode.TextStyledNode(prefix, codeStyleProviders.defaultStyleProvider),
      accessModifier?.let { StyleNode.TextStyledNode(accessModifier, codeStyleProviders.keywordStyleProvider) },
      StyleNode.TextStyledNode(property, codeStyleProviders.identifierStyleProvider),
      StyleNode.TextStyledNode(suffix, codeStyleProviders.defaultStyleProvider),
  ) {
    companion object {
      /**
       * Matches against a TypeScript object property.
       *
       * ```
       * { foo: 'bar' }
       * ```
       */
      private val PATTERN_TYPESCRIPT_OBJECT_PROPERTY = 
          Pattern.compile("""^([{\[(,;](?:\s*-)?)(\s*(?:public|private|protected|readonly))?(\s*[a-zA-Z0-9_$]+)((?:\s*\?)?\s*:)""")

      fun <RC, S> createObjectPropertyRule(
          codeStyleProviders: CodeStyleProviders<RC>
      ) =
          object : Rule<RC, Node<RC>, S>(PATTERN_TYPESCRIPT_OBJECT_PROPERTY) {
            override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>, S>, state: S):
                ParseSpec<RC, S> {
              val prefix = matcher.group(1)
              val accessModifier = matcher.group(2)
              val property = matcher.group(3)
              val suffix = matcher.group(4)
              return ParseSpec.createTerminal(
                  ObjectPropertyNode(prefix!!, accessModifier, property!!, suffix!!, codeStyleProviders), state)
            }
          }
    }
  }

  class DecoratorNode<RC>(
    prefix: String, decorator: String, generics: String?,
    codeStyleProviders: CodeStyleProviders<RC>
  ) : Node.Parent<RC>(
    StyleNode.TextStyledNode(prefix, codeStyleProviders.keywordStyleProvider),
    StyleNode.TextStyledNode(decorator, codeStyleProviders.genericsStyleProvider),
    generics?.let { StyleNode.TextStyledNode(generics, codeStyleProviders.genericsStyleProvider) }
  ) {
    companion object {
      /**
       * Matches against a TypeScript decorator.
       *
       * ```
       * @sealed
       * @timed
       * @wrap
       * @expose()
       * @log<T>()
       * ```
       */
      private val PATTERN_TYPESCRIPT_DECORATOR =
        Pattern.compile("""^(@)(\s*[a-zA-Z_$][a-zA-Z0-9_$]*)(<.*>)?""", Pattern.DOTALL)
      
      fun <RC, S> createDecoratorRule(
        codeStyleProviders: CodeStyleProviders<RC>
      ) =
          object : Rule<RC, Node<RC>, S>(PATTERN_TYPESCRIPT_DECORATOR) {
            override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>, S>, state: S):
                ParseSpec<RC, S> {
              val prefix = matcher.group(1)
              val decorator = matcher.group(2)
              val generics = matcher.group(3)
              return ParseSpec.createTerminal(DecoratorNode(prefix!!, decorator!!, generics, codeStyleProviders), state)
            }
          }
    }
  }

  /**
   * Matches against a TypeScript regex.
   *
   * ```
   * /(.*)/
   * ```
   */
   private val PATTERN_TYPESCRIPT_REGEX = 
       Pattern.compile("""^/.+(?<!\\)/[dgimsuy]*""")

  /**
   * Matches against a TypeScript comment.
   *
   * ```
   * // Hey there
   * /* Hello */
   * ```
   */
  private val PATTERN_TYPESCRIPT_COMMENTS = 
      Pattern.compile("""^(?:(?://.*?(?=\n|$))|(/\*.*?\*/))""", Pattern.DOTALL)

  /**
   * Matches against a TypeScript string.
   *
   * ```
   * 'Hi'
   * "Hello"
   * `Hey`
   * ```
   */
  private val PATTERN_TYPESCRIPT_STRINGS = 
      Pattern.compile("""^('.*?(?<!\\)'|".*?(?<!\\)"|`[\s\S]*?(?<!\\)`)(?=\W|\s|$)""")

  internal fun <RC, S> createCodeRules(
      codeStyleProviders: CodeStyleProviders<RC>
  ): List<Rule<RC, Node<RC>, S>> =
      listOf(
          PATTERN_TYPESCRIPT_COMMENTS.toMatchGroupRule(stylesProvider = codeStyleProviders.commentStyleProvider),
          PATTERN_TYPESCRIPT_STRINGS.toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
          ObjectPropertyNode.createObjectPropertyRule(codeStyleProviders),
          PATTERN_TYPESCRIPT_REGEX.toMatchGroupRule(stylesProvider = codeStyleProviders.literalStyleProvider),
          FieldNode.createFieldRule(codeStyleProviders),
          FunctionNode.createFunctionRule(codeStyleProviders),
          DecoratorNode.createDecoratorRule(codeStyleProviders),
      )
}
