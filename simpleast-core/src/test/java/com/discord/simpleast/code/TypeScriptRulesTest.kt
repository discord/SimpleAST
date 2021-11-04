package com.discord.simpleast.code

import com.discord.simpleast.assertNodeContents
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.core.utils.TreeMatcher
import org.junit.Before
import org.junit.Test


class TypeScriptRulesTest {

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
      ```ts
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
      ```ts
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
      ```ts
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
      ```ts
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

    ast.assertNodeContents<TypeScript.FunctionNode<*>>(
      "function test",
      "function ",
      "function* generator",
      "static test",
      "async fetch",
      "get tokens",
      "set constants")
  }

  @Test
  fun commentedFunction() {
    val ast = parser.parse("""
      ```ts
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
      ```ts
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
        "throw", "finally", "return")
  }

  @Test
  fun numbers() {
    val ast = parser.parse("""
      ```ts
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
      ```ts
      var foo = x;
      let bar = y;
      const baz = z;
        ```
    """.trimIndent(), TestState())
    ast.assertNodeContents<TypeScript.FieldNode<*>>(
        "var foo",
        "let bar",
        "const baz")
  }

  @Test
  fun classDef() {
    val ast = parser.parse("""
      ```ts
      class Bug {}
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<CodeNode.DefinitionNode<*>>(
        "class Bug")
  }

  @Test
  fun regex() {
    val ast = parser.parse("""
      ```ts
      /(.*)/g
      /[\$\{\}]/
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>("/(.*)/g", """/[\$\{\}]/""")
  }

  @Test
  fun objectProperties() {
    val ast = parser.parse("""
      ```ts
      { foo: bar }
      ```
    """.trimIndent(), TestState())

  ast.assertNodeContents<TypeScript.ObjectPropertyNode<*>>("{ foo:")
 }
 
 @Test
 fun types() {
   val ast = parser.parse("""
     ```ts
     string;
     boolean;
     number;
     symbol;
     ```
   """.trimIndent(), TestState())
   
   ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
     "string", "boolean", "number", "symbol")
 }
 
 @Test
 fun decorators() {
   val ast = parser.parse("""
     ```ts
     @sealed
     @test
     @internal
     @wrap
     @save()
     @call<T>()
     ```
   """.trimIndent(), TestState())
   
   ast.assertNodeContents<TypeScript.DecoratorNode<*>>(
     "@sealed", "@test", "@internal",
     "@wrap", "@save", "@call<T>")
 }

 @Test
 fun interfaces() {
   val ast = parser.parse("""
     ```ts
     interface Foo {}
     interface _Bar {}
     interface Baz_ {}
     ```
   """.trimIndent(), TestState())

   ast.assertNodeContents<CodeNode.DefinitionNode<*>>(
     "interface Foo", "interface _Bar", "interface Baz_")
 }
}
