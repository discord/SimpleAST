package com.discord.simpleast.code

import com.discord.simpleast.assertNodeContents
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.core.utils.TreeMatcher
import org.junit.Before
import org.junit.Test


class KotlinRulesTest {

  private class TestState

  private lateinit var parser: Parser<TestRenderContext, Node<TestRenderContext>, TestState>
  private lateinit var treeMatcher: TreeMatcher

  @Before
  fun setup() {
    val codeStyleProviders = CodeStyleProviders<TestRenderContext>()
    parser = Parser()
    parser
        .addRule(CodeRules.createCodeRule(
            codeStyleProviders.defaultStyleProvider,
            CodeRules.createCodeLanguageMap(codeStyleProviders))
        )
        .addRules(SimpleMarkdownRules.createSimpleMarkdownRules())
    treeMatcher = TreeMatcher()
    treeMatcher.registerDefaultMatchers()
  }

  @Test
  fun comments() {
    val ast = parser.parse("""
      ```kt
      /** Multiline
          Comment
      */
      some.call() // Inlined
      // Line comment
      
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        """
          /** Multiline
              Comment
          */
        """.trimIndent(),
        "// Inlined",
        "// Line comment")
  }

  @Test
  fun strings() {
    val ast = parser.parse("""
      ```kt
      call("hello")
      x = "world"
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        """"hello"""",
        """"world"""")
  }

  @Test
  fun stringsMultiline() {
    val multiLineQuote = "\"".repeat(3)

    val ast = parser.parse("""
      ```kt
      text = $multiLineQuote
      hello
      world
      $multiLineQuote
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "\"\"",
        """
          ${'"'}
          hello
          world
          ${'"'}
        """.trimIndent(),
        "\"\""
    )
  }

  @Test
  fun annotations() {
    val ast = parser.parse("""
      ```kt
      @Test(exception=Exception)
      call(@Nullable t: Test)
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "@Test",
        "@Nullable",
    )
  }

  @Test
  fun functions() {
    val ast = parser.parse("""
      ```kt
      private override fun <T> test(x: T) {
        // Implementation
      }

      fun getVal() = value
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<Kotlin.FunctionNode<*>>("fun <T> test(x: T)", "fun getVal()")
  }

  @Test
  fun commentedFunction() {
    val ast = parser.parse("""
      ```kt
      /*
        private override fun <T> test(x:T) {
          throw Exception()
        }
      */
      // public fun foo() {}
      call(x /* test var */)
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        """
        /*
          private override fun <T> test(x:T) {
            throw Exception()
          }
        */
        """.trimIndent(),
        "// public fun foo() {}",
        "/* test var */")
  }

  @Test
  fun keywords() {
    val ast = parser.parse("""
      ```kt
      package com.test;
      import com.test.unit;

      while(true) {
        add(i, 123)
        break;
      }
      if (false) {}

      open _class X {}
      abstract _fun x()

      try {
      } catch(e: Exception) {
        when(e.type) {
        }
      } finally {
        return
      }
      ```
    """.trimIndent(), TestState())
    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "package", "import",
        "while", "true", "123", "break",
        "if", "false",
        "open", "abstract",
        "try", "catch", "when", "finally", "return")
  }

  @Test
  fun numbers() {
    val ast = parser.parse("""
      ```kt
      x = 0
      x += 12
      add(123,456)
      ```
    """.trimIndent(), TestState())
    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
            "0", "12", "123", "456"
   )
  }

  @Test
  fun fields() {
    val ast = parser.parse("""
      ```kt
      private val x: Int = 4
      public val y = 123
      private const val CODE = 2
      val z =
        Foo("bar")
        ```
    """.trimIndent(), TestState())
    ast.assertNodeContents<Kotlin.FieldNode<*>>(
        "val x",
        "val y",
        "val CODE",
        "val z")
  }

  @Test
  fun classDef() {
    val ast = parser.parse("""
      ```kt
      class Foo(bar: String, callaback: () -> Unit) {
        fun test() {}

        data class Bar {}
      }
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<CodeNode.DefinitionNode<*>>(
        "class Foo",
        "class Bar")
    ast.assertNodeContents<Kotlin.FunctionNode<*>>("fun test()")
  }


  @Test
  fun interfaceDef() {
    val ast = parser.parse("""
      ```kt
      interface CodeLanguageState<Self : CodeLanguageState<Self>> {
        val langauge: String?  // Inline
        var isCommentBlock: Boolean
      }
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<CodeNode.DefinitionNode<*>>("interface CodeLanguageState")
    ast.assertNodeContents<Kotlin.FieldNode<*>>("val langauge", "var isCommentBlock")
  }
}