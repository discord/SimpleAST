package com.discord.simpleast.sample

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.style.*
import androidx.annotation.StyleRes
import com.discord.simpleast.R
import com.discord.simpleast.code.CodeRules
import com.discord.simpleast.code.CodeStyleProviders
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.node.StyleNode
import com.discord.simpleast.core.parser.ParseSpec
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule
import com.discord.simpleast.markdown.MarkdownRules
import com.discord.simpleast.sample.spans.BlockBackgroundNode
import com.discord.simpleast.sample.spans.VerticalMarginSpan
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Custom markdown rules to show potential of the framework if you have a bit of creativity.
 *
 * @see MarkdownRules.createMarkdownRules for the default setting
 */
object CustomMarkdownRules {

  fun <RC, S> createMarkdownRules(context: Context,
                                  @StyleRes headerStyles: List<Int>,
                                  @StyleRes classStyles: List<Int>) =
      createHeaderRules<RC, S>(context, headerStyles, classStyles) + MarkdownRules.ListItemRule {
        BulletSpan(24, Color.parseColor("#6E7B7F"))
      }

  private fun <RC, S> createHeaderRules(context: Context,
                                    @StyleRes headerStyles: List<Int>,
                                    @StyleRes classStyles: List<Int>): List<Rule<RC, Node<RC>, S>> {
    fun spanProvider(header: Int): CharacterStyle =
        when (header) {
          0 -> TextAppearanceSpan(context, headerStyles[0])
          in 1..headerStyles.size -> TextAppearanceSpan(context, headerStyles[header - 1])
          else -> StyleSpan(Typeface.BOLD_ITALIC)
        }

    val marginTopPx = context.resources.getDimensionPixelSize(R.dimen.markdown_margin_top)

    return listOf(
        MarkdownRules.HeaderRule(::spanProvider),
        MarkdownRules.HeaderLineClassedRule(::spanProvider) { className ->
          @Suppress("IMPLICIT_CAST_TO_ANY")
          when (className) {
            "add" -> TextAppearanceSpan(context, classStyles[0])
            "remove" -> TextAppearanceSpan(context, classStyles[1])
            "fix" -> TextAppearanceSpan(context, classStyles[2])
            "marginTop" -> VerticalMarginSpan(topPx = marginTopPx, bottomPx = 0)
            else -> null
          }
        }
    )
  }

    interface BlockQuoteState<Self: BlockQuoteState<Self>> {
        val isInQuote: Boolean
        fun newBlockQuoteState(isInQuote: Boolean): Self
    }

    /**
     * Examples:
     * > Quoted text
     *
     * >>> Quoted text
     * that is on
     * multiple lines
     */
    private val PATTERN_BLOCK_QUOTE = Pattern.compile("^(?: *>>> ?(.+)| *>(?!>>) ?([^\\n]+\\n?))", Pattern.DOTALL)

    class BlockQuoteNode<RC> : StyleNode<RC, Any>(listOf(
        LeadingMarginSpan.Standard(40), BackgroundColorSpan(Color.GRAY)))

    // Use a block rule to ensure we only match at the beginning of a line.
    fun <RC, S: BlockQuoteState<S>> createBlockQuoteRule(): Rule.BlockRule<RC, BlockQuoteNode<RC>, S> =
            object : Rule.BlockRule<RC, BlockQuoteNode<RC>, S>(PATTERN_BLOCK_QUOTE) {
                override fun match(inspectionSource: CharSequence, lastCapture: String?, state: S): Matcher? {
                    // Only do this if we aren't already in a quote.
                    return if (state.isInQuote) { null } else { super.match(inspectionSource, lastCapture, state) }
                }

                override fun parse(matcher: Matcher, parser: Parser<RC, in BlockQuoteNode<RC>, S>, state: S): ParseSpec<RC, S> {
                    val groupIndex = if (matcher.group(1) != null) { 1 } else { 2 }
                    val newState = state.newBlockQuoteState(isInQuote = true)
                    return ParseSpec.createNonterminal(BlockQuoteNode(), newState, matcher.start(groupIndex), matcher.end(groupIndex))
                }
            }


  fun <RC, S: BlockQuoteState<S>> createCodeRule(context: Context): Rule<RC, Node<RC>, S> {
    val codeStyleProviders = CodeStyleProviders<RC>(
        defaultStyleProvider = { listOf(TextAppearanceSpan(context, R.style.Code_TextAppearance)) },
        commentStyleProvider = { listOf(TextAppearanceSpan(context, R.style.Code_TextAppearance_Comment)) },
        literalStyleProvider = { listOf(TextAppearanceSpan(context, R.style.Code_TextAppearance_Literal)) },
        keywordStyleProvider = { listOf(TextAppearanceSpan(context, R.style.Code_TextAppearance_Keyword)) },
        identifierStyleProvider = { listOf(TextAppearanceSpan(context, R.style.Code_TextAppearance_Identifier)) },
        typesStyleProvider = { listOf(TextAppearanceSpan(context, R.style.Code_TextAppearance_Types)) },
        genericsStyleProvider = { listOf(TextAppearanceSpan(context, R.style.Code_TextAppearance_Generics)) },
        paramsStyleProvider = { listOf(TextAppearanceSpan(context, R.style.Code_TextAppearance_Params)) },
    )
    val languageMap = CodeRules.createCodeLanguageMap<RC, S>(codeStyleProviders)

    return CodeRules.createCodeRule(
        codeStyleProviders.defaultStyleProvider,
        languageMap
    ) { codeNode, block, state ->
      if (!block) {
        StyleNode<RC, Any>(listOf(BackgroundColorSpan(Color.DKGRAY)))
            .apply { addChild(codeNode) }
      } else {
        BlockBackgroundNode(state.isInQuote, codeNode)
      }
    }
  }

  fun <RC, S : BlockQuoteState<S>> createCodeInlineRule(context: Context): Rule<RC, Node<RC>, S> {
    return CodeRules.createInlineCodeRule(
        { listOf(TextAppearanceSpan(context, R.style.Code_TextAppearance)) },
        { listOf(BackgroundColorSpan(Color.DKGRAY)) },
    )
  }
}