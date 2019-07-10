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
    return render(source, SimpleMarkdownRules.createSimpleMarkdownRules(), null, null)
  }

  @JvmStatic
  fun <R, S> render(source: CharSequence, rules: Collection<Rule<R, Node<R>, S>>, initialState: S, renderContext: R): SpannableStringBuilder {
    val parser = Parser<R, Node<R>, S>()
    for (rule in rules) {
      parser.addRule(rule)
    }

    return render(SpannableStringBuilder(), parser.parse(source, initialState), renderContext)
  }

  @JvmStatic
  fun <R, S> render(source: CharSequence, parser: Parser<R, Node<R>, S>, renderContext: R, initialState: S): SpannableStringBuilder {
    return render(SpannableStringBuilder(), parser.parse(source, initialState), renderContext)
  }

  @JvmStatic
  fun <T: SpannableStringBuilder, R> render(builder: T, ast: Collection<Node<R>>, renderContext: R): T {
    for (node in ast) {
      node.render(builder, renderContext)
    }
    return builder
  }
}
