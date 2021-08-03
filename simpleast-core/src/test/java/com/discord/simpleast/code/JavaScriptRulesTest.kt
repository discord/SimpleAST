package com.discord.simpleast.code

import com.discord.simpleast.assertNodeContents
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.core.utils.TreeMatcher
import org.junit.Before
import org.junit.Test


class JavaScriptRulesTest {

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
      ```js
      /** Multiline
          Comment
      */
      foo.bar(); // Inlined
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
      ```js
      x = 'Hello';
      y = "world";
      log(`Hi`);
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "'Hello'",
        "\"world\"",
        "`Hi`")
  }
  @Test
  fun stringsMultiline() {
    val ast = parser.parse("""
      ```js
      text = `
      hello
      world
      `;
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        """
          `
          hello
          world
          `
        """.trimIndent(),
    )
  }

  @Test
  fun functions() {
    val ast = parser.parse("""
      ```js
      function test(T) {
        // Implementation
      }
      fn = function () {};
      function* generator() {}
      static test() {}
      async fetch() {}
      get tokens() {}
      set constants() {}
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<JavaScript.FunctionNode<*>>(
      "function test(T)",
      "function () {",
      "function* generator() {",
      "static test() {",
      "async fetch() {",
      "get tokens() {",
      "set constants() {")
  }

  @Test
  fun commentedFunction() {
    val ast = parser.parse("""
      ```js
      /*
        function test(T) {
          throw new Error();
        }
      */
      // function O() {}
      log(x /* test var */);
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        """
        /*
          function test(T) {
            throw new Error();
          }
        */
        """.trimIndent(),
        "// function O() {}",
        "/* test var */")
  }

  @Test
  fun keywords() {
    val ast = parser.parse("""
      ```js
      while (true) {}
      for (;;) {}
      if (false) {}
      class {}
      try {
      } catch (err) {
        throw
      } finally {
        return;
      }
      ```
    """.trimIndent(), TestState())
    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "while", "true", 
        "for", "if", "false",
        "class", "try", "catch",
        "finally", "return", "throw")
  }

  @Test
  fun numbers() {
    val ast = parser.parse("""
      ```js
      x = 0;
      x += 69;
      max(x, 420);
      ```
    """.trimIndent(), TestState())
    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
            "0", "69", "420"
   )
  }

  @Test
  fun fields() {
    val ast = parser.parse("""
      ```js
      var foo = x;
      let bar = y;
      const baz = z;
        ```
    """.trimIndent(), TestState())
    ast.assertNodeContents<JavaScript.FieldNode<*>>(
        "var foo",
        "let bar",
        "const baz")
  }

  @Test
  fun classDef() {
    val ast = parser.parse("""
      ```js
      class Bug {}
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<CodeNode.DefinitionNode<*>>(
        "class Bug")
  }

  @Test
  fun regex() {
    val ast = parser.parse("""
      ```js
      /(.*)/g
      /[\$\{\}]/
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>("/(.*)/g", """/[\$\{\}]/""")
  }

  @Test
  fun generics() {
    val ast = parser.parse("""
      ```js
      <pending>
      ```
    """.trimIndent(), TestState())

   ast.assertNodeContents<StyleNode.TextStyledNode<*>>("<pending>")
  }

  @Test
  fun objectProperties() {
    val ast = parser.parse("""
      ```js
      { foo: bar }
      ```
    """.trimIndent(), TestState())

  ast.assertNodeContents<JavaScript.ObjectPropertyNode<*>>("{ foo:")
 }
}
