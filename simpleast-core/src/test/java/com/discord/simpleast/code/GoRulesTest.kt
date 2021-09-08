package com.discord.simpleast.code

import com.discord.simpleast.assertNodeContents
import com.discord.simpleast.core.node.*
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.core.utils.TreeMatcher
import org.junit.Before
import org.junit.Test

class GoRulesTest {

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
      ```go
      /** Multiline
        Comment
      */
      foo.bar() // Inlined
      // Line comment
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
      """
        /** Multiline
          Comment
        */
      """.trimIndent(),
      "bar",
      "// Inlined",
      "// Line comment")
  }

  @Test
  fun strings() {
    val ast = parser.parse("""
      ```go
      x := "Hello"
      println(`world`)
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
      """"Hello"""",
      "println",
      "`world`")
  }

  @Test
  fun stringsMultiline() {
    val ast = parser.parse("""
      ```go
      text := `
      hello
      world
      `
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
      """
        `
        hello
        world
        `
      """.trimIndent())
  }

  @Test
  fun functions() {
    val ast = parser.parse("""
      ```go
      func bar(a int) bool {
        return false
      }
      
      bar(1)
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
      "func",
      "bar",
      "int",
      "bool",
      "return",
      "false",
      "bar",
      "1")
  }

  @Test
  fun keywords() {
    val ast = parser.parse("""
      ```go
      package main
      
      import "fmt"
      
      func main() {
        for {
          if false {
            continue
          } else {
            break
          }
        }
        
        switch a {
        case 0:
          fallthrough
        case 1:
          fmt.Println("a")
        }
      }
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
      "package",
      " main",
      "import",
      """"fmt"""",
      "func",
      "main",
      "for",
      "if",
      "false",
      "continue",
      "else",
      "break",
      "switch",
      "case",
      "0",
      "fallthrough",
      "case",
      "1",
      "Println",
      """"a"""")
  }

  @Test
  fun numbers() {
    val ast = parser.parse("""
      ```go
      x := 0
      x += 12
      add(123,456)
      ```
    """.trimIndent(), TestState())
    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
      "0", "12", "add", "123", "456"
    )
  }

  @Test
  fun structDef() {
    val ast = parser.parse("""
      ```go
      type User struct {
        ID   uint64
        Name string
      }
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<CodeNode.DefinitionNode<*>>("type User")
    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
      "type",
      " User",
      "struct",
      "uint64",
      "string")
  }
}
