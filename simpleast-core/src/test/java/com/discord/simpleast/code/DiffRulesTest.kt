package com.discord.simpleast.code

import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.utils.ASTUtils
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Diff markers are only meaningful at column zero of a physical line, but rules are matched against
 * the *remaining* source, so a bare `^` is not enough to express that.
 */
class DiffRulesTest {

  private class TestState

  // Distinct capturing lambdas: the `CodeStyleProviders` defaults are non-capturing and may be
  // shared instances, which would make addition/deletion indistinguishable by identity.
  private val additions = StyleNode.SpanProvider<TestRenderContext> { listOf("addition") }
  private val deletions = StyleNode.SpanProvider<TestRenderContext> { listOf("deletion") }

  private lateinit var parser: Parser<TestRenderContext, Node<TestRenderContext>, TestState>

  @Before
  fun setup() {
    val codeStyleProviders = CodeStyleProviders(
        additionStyleProvider = additions,
        deletionStyleProvider = deletions,
    )
    parser = Parser<TestRenderContext, Node<TestRenderContext>, TestState>()
        .addRule(CodeRules.createCodeRule(
            codeStyleProviders.defaultStyleProvider,
            CodeRules.createCodeLanguageMap(codeStyleProviders)))
  }

  /** Contents of every node styled with [provider], in document order. */
  private fun parseDiff(body: String, provider: StyleNode.SpanProvider<TestRenderContext>): List<String> {
    val ast = parser.parse("```diff\n$body\n```", TestState())
    val contents = mutableListOf<String>()
    ASTUtils.traversePreOrder(ast) { node ->
      if (node is StyleNode.TextStyledNode<*> && node.stylesProvider === provider) {
        contents.add(node.content)
      }
    }
    return contents
  }

  private fun assertDiff(body: String, additions: List<String>, deletions: List<String>) {
    Assert.assertEquals("additions", additions, parseDiff(body, this.additions))
    Assert.assertEquals("deletions", deletions, parseDiff(body, this.deletions))
  }

  @Test
  fun lineStartMarkers() = assertDiff(
      """
      -removed
      +added
      """.trimIndent(),
      additions = listOf("+added"),
      deletions = listOf("-removed"))

  @Test
  fun fileHeaders() = assertDiff(
      """
      --- a/foo.txt
      +++ b/foo.txt
      """.trimIndent(),
      additions = listOf("+++ b/foo.txt"),
      deletions = listOf("--- a/foo.txt"))

  @Test
  fun hunkHeaderIsNotAMarker() = assertDiff(
      "@@ -1,3 +1,4 @@ fun main() {",
      additions = emptyList(),
      deletions = emptyList())

  @Test
  fun midLineMarkersAreNotStyled() = assertDiff(
      """
      some-dashed-identifier
      run --flag=1
      total += 2
      offset = -42
      """.trimIndent(),
      additions = emptyList(),
      deletions = emptyList())

  @Test
  fun contextLinesAreNotStyled() = assertDiff(
      """
      fun main() {
        println("a-b")
        println("a+b")
      }
      """.trimIndent(),
      additions = emptyList(),
      deletions = emptyList())

  /** Markers must sit at literal column zero; leading whitespace disqualifies the line. */
  @Test
  fun indentedMarkersAreNotStyled() = assertDiff(
      "  ~ resource \"aws_s3_bucket\" \"b\" {\n" +
          "      - acl = \"private\"\n" +
          "\t+ acl = \"public\"\n" +
          "    }",
      additions = emptyList(),
      deletions = emptyList())

  @Test
  fun indentedMarkerOnFirstLineIsNotStyled() = assertDiff(
      "    - acl = \"private\"\ncontext",
      additions = emptyList(),
      deletions = emptyList())

  @Test
  fun blankLinesAndConsecutiveMarkers() = assertDiff(
      "-one\n-two\n\n+three\n+four\ncontext\n+five",
      additions = listOf("+three", "+four", "+five"),
      deletions = listOf("-one", "-two"))

  @Test
  fun crlfLineEndings() = assertDiff(
      "context\r\n-removed\r\n+added\r\ntrailing",
      additions = listOf("+added"),
      deletions = listOf("-removed"))
}
