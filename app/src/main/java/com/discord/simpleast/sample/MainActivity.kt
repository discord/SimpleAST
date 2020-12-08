package com.discord.simpleast.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.discord.simpleast.R
import androidx.lifecycle.lifecycleScope
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.TextNode
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.core.simple.SimpleRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Matcher
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

  private lateinit var resultText: TextView
  private lateinit var input: EditText

  private lateinit var parser: Parser<RenderContext, Node<RenderContext>, ParseState>

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    parser = Parser<RenderContext, Node<RenderContext>, ParseState>()
        .addRules(
            // Allow callers to escape markdown commands such as code block ticks
            SimpleMarkdownRules.createEscapeRule(),
            UserMentionRule(),
            CustomMarkdownRules.createBlockQuoteRule())
        .addRules(CustomMarkdownRules.createMarkdownRules(
            this@MainActivity,
            listOf(R.style.Demo_Header_1, R.style.Demo_Header_2, R.style.Demo_Header_3),
            listOf(R.style.Demo_Header_1_Add, R.style.Demo_Header_1_Remove, R.style.Demo_Header_1_Fix)))
        .addRules(
            CustomMarkdownRules.createCodeRule(this@MainActivity),
            CustomMarkdownRules.createCodeInlineRule(this@MainActivity))
        .addRules(SimpleMarkdownRules.createSimpleMarkdownRules(includeEscapeRule = false))

    resultText = findViewById(R.id.result_text)
    input = findViewById(R.id.input)

    input.setText(SampleTexts.ALL.trimIndent())

    findViewById<View>(R.id.benchmark_btn).setOnClickListener {
      val times = 50

      lifecycleScope.launchWhenResumed {
        withContext(Dispatchers.IO) {
          var totalDuration = 0L

          (0..times).forEach { _ ->
            val start = System.currentTimeMillis()
            testParse(50)
            val end = System.currentTimeMillis()
            val duration = end - start
            totalDuration += duration
            Log.d("timer", "duration of parse: $duration ms")
          }

          Log.d("timer", "average parse time: " + totalDuration / times + " ms")
        }
      }
    }

    findViewById<View>(R.id.test_btn).setOnClickListener {
      parseInput()
    }
    parseInput()
  }

  data class ParseState(override val isInQuote: Boolean) : CustomMarkdownRules.BlockQuoteState<ParseState> {
    override fun newBlockQuoteState(isInQuote: Boolean): ParseState = ParseState(isInQuote)
  }

  private fun parseInput() = lifecycleScope.launchWhenStarted {
    val renderedText = withContext(Dispatchers.IO) {
      SimpleRenderer.render(
          source = input.text,
          parser = parser,
          initialState = ParseState(false),
          renderContext = RenderContext(mapOf(1234 to "User1234")))
    }

    resultText.text = renderedText
  }

  private fun testParse(times: Int) {
    for (i in 0 until times) {
      SimpleRenderer.render(
          source = SampleTexts.BENCHMARK_TEXT.trimIndent(),
          parser = parser,
          initialState = ParseState(false),
          renderContext = RenderContext(mapOf(1234 to "User1234")))
    }
  }

  @Suppress("unused")
  class FooRule<S> : Rule<Any?, Node<Any?>, S>(Pattern.compile("^<Foo>")) {
    override fun parse(matcher: Matcher, parser: Parser<Any?, in Node<Any?>, S>, state: S): ParseSpec<Any?, S> {
      return ParseSpec.createTerminal(TextNode("Bar"), state)
    }
  }

  data class RenderContext(val usernameMap: Map<Int, String>)

  class UserNode(private val userId: Int) : Node<RenderContext>() {
    override fun render(builder: SpannableStringBuilder, renderContext: RenderContext) {
      builder.append(renderContext.usernameMap[userId] ?: "Invalid User")
    }
  }

  class UserMentionRule<S> : Rule<RenderContext, UserNode, S>(Pattern.compile("^<(\\d+)>")) {
    override fun parse(matcher: Matcher, parser: Parser<RenderContext, in UserNode, S>, state: S): ParseSpec<RenderContext, S> {
      return ParseSpec.createTerminal(UserNode(matcher.group(1)!!.toInt()), state)
    }
  }
}

