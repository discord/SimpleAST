package com.discord.simpleast.code

import com.discord.simpleast.assertNodeContents
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.core.utils.TreeMatcher
import org.junit.Before
import org.junit.Test

class CrystalRulesTest {

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
      ```cr
      # Single line comment
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        """
          # Single line comment
        """.trimIndent())
  }

  @Test
  fun strings() {
    val ast = parser.parse("""
      ```cr
      hello = "hello"
      "world"
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        """"hello"""",
        """"world""""
    )
  }

  @Test
  fun stringsMultiline() {
    val ast = parser.parse("""
      ```cr
      text = "
      hello
      world
      "
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        """
          ${'"'}
          hello
          world
          ${'"'}
        """.trimIndent(),
    )
  }

  @Test
  fun regex() {
    val ast = parser.parse("""
      ```cr
      /^ +/m
      /(\d+)-(?:\w{2,5}|0{5})/i
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "/^ +/m",
        """/(\d+)-(?:\w{2,5}|0{5})/i"""
    )
  }

  @Test
  fun annotations() {
    val ast = parser.parse("""
      ```cr
      @[Post]
      @[KeyValue(key: "value")]
      @[Positional("string", 123, false)]
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "@[Post]",
        "@[KeyValue(key: \"value\")]",
        "@[Positional(\"string\", 123, false)]"
    )
  }

  @Test
  fun functions() {
    val ast = parser.parse("""
      ```cr
      private def build(id : Int32)
      protected def find(@query : String)
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<Crystal.FunctionNode<*>>(
        "def build(id : Int32)",
        "def find(@query : String)")
  }

  @Test
  fun keywords() {
    val ast = parser.parse("""
      ```cr
      module CrystalTest
      property length
      
      macro printer()
      
      begin
        raise
      rescue
      ensure
      
      if true
      else false
      end
      
      close_door unless door_closed?
        
      case state
      when dirty
      
      while
        break
        next
      ```
    """.trimIndent(), TestState())

    ast.assertNodeContents<StyleNode.TextStyledNode<*>>(
        "module", "property", "macro",
        "begin", "raise", "rescue", "ensure",
        "if", "true", "else", "false", "end",
        "unless", "case", "when",
        "while", "break", "next",
    )
  }

  @Test
  fun numbers() {
    val ast = parser.parse("""
      ```cr
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
  fun classDef() {
    val ast = parser.parse("""
      ```cr
      class Person
        def initialize
      ```  
    """.trimIndent(), TestState())

    ast.assertNodeContents<CodeNode.DefinitionNode<*>>("class Person")
    ast.assertNodeContents<Crystal.FunctionNode<*>>("def initialize")
  }
}