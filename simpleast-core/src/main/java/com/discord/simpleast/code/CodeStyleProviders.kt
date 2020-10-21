package com.discord.simpleast.code

import com.discord.simpleast.core.node.StyleNode

data class CodeStyleProviders<R>(
    val defaultStyleProvider: StyleNode.SpanProvider<R> = emptyProvider(),
    val commentStyleProvider: StyleNode.SpanProvider<R> = emptyProvider(),
    val literalStyleProvider: StyleNode.SpanProvider<R> = emptyProvider(),
    val keywordStyleProvider: StyleNode.SpanProvider<R> = emptyProvider(),
    val identifierStyleProvider: StyleNode.SpanProvider<R> = emptyProvider(),
    val typesStyleProvider: StyleNode.SpanProvider<R> = emptyProvider(),
    val genericsStyleProvider: StyleNode.SpanProvider<R> = emptyProvider(),
    val paramsStyleProvider: StyleNode.SpanProvider<R> = emptyProvider(),
)

private fun <R> emptyProvider() = StyleNode.SpanProvider<R> { emptyList<Any>() }