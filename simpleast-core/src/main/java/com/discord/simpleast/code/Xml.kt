package com.discord.simpleast.code

import android.text.SpannableStringBuilder
import android.text.Spanned
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import java.util.regex.Matcher
import java.util.regex.Pattern


object Xml {
  val PATTERN_XML_COMMENT: Pattern = Pattern.compile("""^<!--[\s\S]*?-->""", Pattern.DOTALL)

  val PATTERN_XML_TAG: Pattern = Pattern.compile(
      """^<([\s\S]+?)(?:>(.*?)<\/([\s\S]+?))?>""", Pattern.DOTALL)
  const val PATTERN_XML_TAG_OPENING_GROUP = 1
  const val PATTERN_XML_TAG_CONTENT_GROUP = 2
  const val PATTERN_XML_TAG_CLOSING_GROUP = 3

  class TagNode<RC>(
      val opening: String, val closing: String?,
      private val codeStyleProviders: CodeStyleProviders<RC>
  ) : Node.Parent<RC>() {
    override fun render(builder: SpannableStringBuilder, renderContext: RC) {
      val (name, remainder) =
          when (val index = opening.indexOfFirst { it.isWhitespace() || it == '/' }) {
            -1 -> opening to ""
            else -> opening.substring(0, index) to opening.substring(index)
          }
      val typeStylesProvider = codeStyleProviders.genericsStyleProvider::get

      var startIndex = builder.length
      builder.append("<$name")
      typeStylesProvider(renderContext).forEach {
        builder.setSpan(it, startIndex, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      }

      startIndex = builder.length
      builder.append("$remainder>")
      codeStyleProviders.paramsStyleProvider.get(renderContext).forEach {
        builder.setSpan(it, startIndex, builder.length - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      }
      typeStylesProvider(renderContext).forEach {
        builder.setSpan(it, builder.length - 1, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
      }

      super.render(builder, renderContext)

      if (!closing.isNullOrEmpty()) {
        startIndex = builder.length
        builder.append("</$closing>")
        typeStylesProvider(renderContext).forEach {
          builder.setSpan(it, startIndex + 1, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
      }
    }
  }

  fun <RC, S> createTagRule(
      codeStyleProviders: CodeStyleProviders<RC>
  ) =
      object : Rule<RC, Node<RC>, S>(PATTERN_XML_TAG) {
        override fun parse(matcher: Matcher, parser: Parser<RC, in Node<RC>, S>, state: S):
            ParseSpec<RC, S> {
          val opening = matcher.group(PATTERN_XML_TAG_OPENING_GROUP)!!
          val closing = matcher.group(PATTERN_XML_TAG_CLOSING_GROUP)

          return if (matcher.group(PATTERN_XML_TAG_CONTENT_GROUP) != null) {
            ParseSpec.createNonterminal(
                TagNode(opening, closing, codeStyleProviders),
                state,
                matcher.start(PATTERN_XML_TAG_CONTENT_GROUP),
                matcher.end(PATTERN_XML_TAG_CONTENT_GROUP))
          } else {
            ParseSpec.createTerminal(
                TagNode(opening, closing, codeStyleProviders),
                state)
          }
        }
      }
}