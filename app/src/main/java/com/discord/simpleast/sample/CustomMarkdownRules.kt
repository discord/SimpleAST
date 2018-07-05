package com.discord.simpleast.sample

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.annotation.StyleRes
import android.text.style.BulletSpan
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import com.agarron.simpleast.R
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.parser.Rule
import com.discord.simpleast.markdown.MarkdownRules
import com.discord.simpleast.sample.spans.VerticalMarginSpan

/**
 * Custom markdown rules to show potential of the framework if you have a bit of creativity.
 *
 * @see MarkdownRules.createMarkdownRules for the default setting
 */
object CustomMarkdownRules {

  fun <RC> createMarkdownRules(context: Context,
                              @StyleRes headerStyles: List<Int>,
                              @StyleRes classStyles: List<Int>) =
      createHeaderRules<RC>(context, headerStyles, classStyles) + MarkdownRules.ListItemRule {
        BulletSpan(24, Color.parseColor("#6E7B7F"))
      }

  private fun <RC> createHeaderRules(context: Context,
                                    @StyleRes headerStyles: List<Int>,
                                    @StyleRes classStyles: List<Int>): List<Rule<RC, Node<RC>>> {
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
}