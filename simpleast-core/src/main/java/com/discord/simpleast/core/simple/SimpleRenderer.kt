package com.discord.simpleast.core.simple

import android.support.annotation.StringRes
import android.text.SpannableStringBuilder
import android.widget.TextView
import com.discord.simpleast.core.node.Node
import com.discord.simpleast.core.parser.Parser
import com.discord.simpleast.core.parser.Rule

object SimpleRenderer {

  @JvmStatic
  fun renderBasicMarkdown(@StringRes sourceResId: Int, textView: TextView) {
    val source = textView.context.getString(sourceResId)
    renderBasicMarkdown(source, textView)
  }

  @JvmStatic
  fun renderBasicMarkdown(source: CharSequence, textView: TextView) {
    textView.text = renderBasicMarkdown(source)
  }

  @JvmStatic
  fun renderBasicMarkdown(source: CharSequence): SpannableStringBuilder {
    return render(source, SimpleMarkdownRules.createSimpleMarkdownRules(), null)
  }

  @JvmStatic
  fun <R> render(source: CharSequence, rules: Collection<Rule<R, Node<R>>>, renderContext: R): SpannableStringBuilder {
    val parser = Parser<R, Node<R>>()
    for (rule in rules) {
      parser.addRule(rule)
    }

    return render(SpannableStringBuilder(), parser.parse(source, false), renderContext)
  }

  @JvmStatic
  fun <R> render(source: CharSequence, parser: Parser<R, Node<R>>, renderContext: R): SpannableStringBuilder {
    return render(SpannableStringBuilder(), parser.parse(source, false), renderContext)
  }

  @JvmStatic
  fun <T: SpannableStringBuilder, R> render(builder: T, ast: Collection<Node<R>>, renderContext: R): T {
    for (node in ast) {
      node.render(builder, renderContext)
    }

    return builder
  }
}
