package com.discord.simpleast.code

import com.discord.simpleast.assertNodeContents
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.core.utils.TreeMatcher
import org.junit.Before
import org.junit.Test


class CodeRulesTest {

  private class TestState {}

  private lateinit var parser: Parser<TestRenderContext, Node<TestRenderContext>, TestState>
  private lateinit var treeMatcher: TreeMatcher

  @Before
  fun setup() {
    parser = Parser()
    val codeStyleProviders = CodeStyleProviders<TestRenderContext>()
    parser.addRule(CodeRules.createCodeRule(
        codeStyleProviders.defaultStyleProvider,
        CodeRules.createCodeLanguageMap(codeStyleProviders))
    )
    parser.addRules(SimpleMarkdownRules.createSimpleMarkdownRules())
    treeMatcher = TreeMatcher()
    treeMatcher.registerDefaultMatchers()
  }

  @Test
  fun noLanguageOneLined() {
    val ast = parser.parse("""
      ```code```
      
      ```spaces  ```
      
      ```some text```
    """.trimIndent(), TestState())

    ast.assertNodeContents<CodeNode<*>>("code", "spaces  ", "some text")
  }

  @Test
  fun noLanguageBlocked() {
    val ast = parser.parse("""
      Sample: ```
      **block text**
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<CodeNode<*>>("**block text**")
  }

  @Test
  fun commentsRust() {
    val ast = parser.parse("""
      ```rs
      some.call() // Inlined
      // Line comment
      
      /// Doc
      fun X()
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "// Inlined",
        "// Line comment",
        "/// Doc")
  }

  @Test
  fun commentsPython() {
    val ast = parser.parse("""
      ```py
      some.call() # Inlined
      # Line comment
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "# Inlined",
        "# Line comment")
  }

  @Test
  fun stringsRust() {
    val ast = parser.parse("""
      ```rs
      call.me("maybe")
      name = "lyte";
      multi= "hello
          world";
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        """"maybe"""",
        """"lyte"""",
        """"hello
        |    world"""".trimMargin(),)
  }

  @Test
  fun stringsPython() {
    val ast = parser.parse("""
      ```py
      call.me("maybe")
      name = "lyte";
      multi= "hello
          world";
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        """"maybe"""",
        """"lyte"""",
        """"hello
        |    world"""".trimMargin(),)
  }

  /**
   * Since number literals is generic we don't need specific language tests
   */
  @Test
  fun numbers() {
    val ast = parser.parse("""
      ```py
      x = 0
      x += 12
      add(123, 456)
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "0", "12", "123", "456"
    )
  }

  @Test
  fun annotationsPython() {
    val ast = parser.parse("""
      ```py
      @Route(method=GET)
      def get(user):
        pass
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "@Route",
        "def", " get",
        "pass"
    )
  }

  @Test
  fun annotationsRust() {
    val ast = parser.parse("""
      ```rs
      #[derive(HelperAttr)]
      struct Struct {
          #[helper] field: ()
      }
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "#[derive(HelperAttr)]\n",
        "struct", " Struct",
//        "#[helper]"  // Not supported
    )
  }

  @Test
  fun keywordsRust() {
    val ast = parser.parse("""
      ```rs
      mod test {
        use std::sync::Arc
        
        pub struct Event {name: A, value: B}
        
        impl<T: Clone, V> Event<T, V>
        where
          V: Clone + Send + Sync
        {
          async fn count(&self, req: T) -> Result<usize, String>{
            let limit = match req.limit {
              0 => usize::max_value(),
              _ => req.limit,
            }
            return Ok(self.count(limit).await?);
          }
        }
      }
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "mod", " test",
        "use", "Arc",
        "pub", "struct", " Event",
        "impl", "Clone",
        "where", "Clone", "Send", "Sync",
        "async", "fn", "self", "Result", "usize", "String",
        "let", "match", "0", "usize",
        "return", "Ok", "self", "await",
    )
  }

  @Test
  fun keywordsPython() {
    val ast = parser.parse("""
      ```py
      from com.discord import test as _test
      def hello_world(self, name, interests):
        try
          for entry in interests:
            if entry is None and not False:
              raise 0
            continue
        except:
          pass
        finally:
          return
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "from", "import", "as",
        "def", " hello_world", "self",
        "try",
        "for", "in",
        "if", "is", "None", "and", "not", "False",
        "raise", "0",
        "continue",
        "except",
        "pass",
        "finally",
        "return")
  }
}