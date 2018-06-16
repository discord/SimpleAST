package com.discord.simpleast.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.agarron.simpleast.R
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.TextNode
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import com.discord.simpleast.core.simple.SimpleMarkdownRules
import com.discord.simpleast.core.simple.SimpleRenderer
import com.discord.simpleast.markdown.MarkdownRules
import java.util.regex.Matcher
import java.util.regex.Pattern

private const val SAMPLE_TEXT = """
  Some really long introduction text that goes on forever explaining something.

  FIRST.Title italics word
  =======

  Title italics word
  =======
  * **bold item**
  * another point that is really obvious but just explained to death and should be half the length in reality
  * last item

""" /* + """
  # Conclusion __H1__
  So in conclusion. This whole endeavour was just a really long waste of time.

  ## Appendix __H2__
  some other stuff that should go here

  ### Sources __H3__
  * mind's eye
  * friend of a friend
  """*/

class MainActivity : AppCompatActivity() {

  private lateinit var resultText: TextView
  private lateinit var input: EditText

  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    resultText = findViewById(R.id.result_text)
    input = findViewById(R.id.input)

    input.setText(SAMPLE_TEXT.trimIndent())

    findViewById<View>(R.id.benchmark_btn).setOnClickListener {
      val times = 50.0
      var totalDuration = 0L
      var i = 0
      while (i < times) {
        val start = System.currentTimeMillis()
        testParse(50)
        val end = System.currentTimeMillis()
        val duration = end - start
        totalDuration += duration
        Log.d("timer", "duration of parse: $duration ms")
        i++
      }
      Log.d("timer", "average parse time: " + totalDuration / times + " ms")
    }

    findViewById<View>(R.id.test_btn).setOnClickListener {
      val parser = Parser<RenderContext, Node<RenderContext>>()
          .addRule(UserMentionRule())
          .addRules(MarkdownRules.createExtremeBRSTXXrdMarkdownRules(
              this, listOf(R.style.Demo_Header_1, R.style.Demo_Header_2, R.style.Demo_Header_3)))
          .addRules(SimpleMarkdownRules.createSimpleMarkdownRules())

      resultText.text = SimpleRenderer.render(
          source = input.text,
          parser = parser,
          renderContext = RenderContext(mapOf(1234 to "User1234"))
      )
    }
  }

  private fun createTestText() = """[0;31mERROR:[0m Signature extraction failed: Traceback (most recent call last):
  File "/usr/local/lib/python3.5/dist-packages/youtube_dl/extractor/youtube.py", line 1011, in _decrypt_signature
    video_id, player_url, s
  File "/usr/local/lib/python3.5/dist-packages/youtube_dl/extractor/youtube.py", line 925, in _extract_signature_function
    errnote='Download of %s failed' % player_url)
  File "/usr/local/lib/python3.5/dist-packages/youtube_dl/extractor/common.py", line 519, in _download_webpage
    res = self._download_webpage_handle(url_or_request, video_id, note, errnote, fatal, encoding=encoding, data=data, headers=headers, query=query)
  File "/usr/local/lib/python3.5/dist-packages/youtube_dl/extractor/common.py", line 426, in _download_webpage_handle
    urlh = self._request_webpage(url_or_request, video_id, note, errnote, fatal, data=data, headers=headers, query=query)
  File "/usr/local/lib/python3.5/dist-packages/youtube_dl/extractor/common.py", line 406, in _request_webpage
    return self._downloader.urlopen(url_or_request)
  File "/usr/local/lib/python3.5/dist-packages/youtube_dl/YoutubeDL.py", line 2000, in urlopen
    req = sanitized_Request(req)
  File "/usr/local/lib/python3.5/dist-packages/youtube_dl/utils.py", line 518, in sanitized_Request
    return compat_urllib_request.Request(sanitize_url(url), *args, **kwargs)
  File "/usr/lib/python3.5/urllib/request.py", line 269, in init
    self.full_url = url
  File "/usr/lib/python3.5/urllib/request.py", line 295, in full_url
    self._parse()
  File "/usr/lib/python3.5/urllib/request.py", line 324, in _parse
    raise ValueError("unknown url type: %r" % self.full_url)
ValueError: unknown url type: '/yts/jsbin/player-en_US-vflkk7pUE/base.js'
 (caused by ValueError("unknown url type: '/yts/jsbin/player-en_US-vflkk7pUE/base.js'",))"""

  private fun testParse(times: Int) {
    val text = createTestText()

    for (i in 0 until times) {
      SimpleRenderer.renderBasicMarkdown(text, resultText)
    }
  }

  @Suppress("unused")
  class FooRule : Rule<Any?, Node<Any?>>(Pattern.compile("^<Foo>")) {
    override fun parse(matcher: Matcher, parser: Parser<Any?, in Node<Any?>>, isNested: Boolean): ParseSpec<Any?, Node<Any?>> {
      return ParseSpec.createTerminal(TextNode("Bar"))
    }
  }

  data class RenderContext(val usernameMap: Map<Int, String>)
  class UserNode(private val userId: Int) : Node<RenderContext>() {
    override fun render(builder: SpannableStringBuilder, renderContext: RenderContext) {
      builder.append(renderContext.usernameMap[userId] ?: "Invalid User")
    }
  }

  class UserMentionRule : Rule<RenderContext, UserNode>(Pattern.compile("^<(\\d+)>")) {
    override fun parse(matcher: Matcher, parser: Parser<RenderContext, in UserNode>, isNested: Boolean): ParseSpec<RenderContext, UserNode> {
      return ParseSpec.createTerminal(UserNode(matcher.group(1).toInt()))
    }
  }
}

