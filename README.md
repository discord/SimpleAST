# SimpleAST

[![](https://jitpack.io/v/discord/SimpleAST.svg)](https://jitpack.io/#discord/SimpleAST)

SimpleAST is a Kotlin/Java library designed to parse text into Abstract Syntax Trees. It is heavily inspired by (and began as a port of) [Khan Academy's simple-markdown](https://github.com/Khan/simple-markdown).

It strives for extensibility and robustness. How text is parsed into nodes in a tree is determined by a set of rules, provided by the client. This makes detecting and rendering your own custom entities in text a breeze.

Besides basic markdown, SimpleAST is what the Discord Android app uses to detect and render various entities in text.

For example:

"<@123456789> has \*\*joined the server\*\*." becomes "@AndyG has **joined the server**." Read more here: [How Discord Renders Rich Messages on the Android App](https://blog.discordapp.com/how-discord-renders-rich-messages-on-the-android-app-67b0e5d56fbe)

# Using SimpleAST in your application
If you are building with Gradle, see https://jitpack.io/#discord/SimpleAST/ for instructions:

Latest versions can be found on the [releases page](https://github.com/discord/SimpleAST/releases).

# Basic Usage with SimpleMarkdownRenderer

If you want to simply render some text with basic markdown, you can use `SimpleRenderer`:

```kotlin
val source = "here is some bold text: **this is bold**"
val textView = findViewById<TextView>(R.id.textView)

SimpleRenderer.render(source, textView)
```

`SimpleRenderer.render` uses the rules provided in `SimpleMarkdownRules.kt`. These rules currently include:

* **Bold**: \*\*bold\*\*
* *Italics 1*: \*italics\*
* _Italics 2_: \_italics\_
* Underline: \_\_underline\_\_
* ~~Strikethru~~: \~\~Strikethru\~\~
* Escaping: \\\*Not Italics*

# Adding your own Rules

We can create rules which will detect other entities in text. Rules should detect text that begins with symbols or other characters not matched in the plaintext rule.

A few things to keep in mind when building your own Parsers and Rules:

1. Always include, at the very least, the plaintext rule. Without this rule, you may end up with unmatched text in the source (a fatal error.)
2. A `Pattern` that defines a `Rule` should begin with a symbol that is non-alphanumeric. The plaintext rule is designed so that a non-alphanumeric character will trigger the `Parser` to consider whether the source matches any other rules first, before consuming it as plaintext.
3. The `Pattern` that defines the rule matches **only** with the beginning of source text (i.e. begins with the `'^'` character). You will end up with magically-disappearing text if you suddenly match something in the middle of your source.

## Simplest example

Let's imagine we want to render all occurrences of `<Foo>` as `Bar`, i.e. "This is &lt;Foo&gt; speaking" becomes "This is Bar speaking".

We create a simple `Rule` that detects and performs the replacement:

```kotlin
class FooRule : Rule<Any?, Node<Any?>>(Pattern.compile("^<Foo>")) {
  override fun parse(matcher: Matcher, parser: Parser<Any?, in Node<Any?>>, isNested: Boolean): ParseSpec<Any?, Node<Any?>{
    return ParseSpec.createTerminal(TextNode("Bar"))
  }
}
```

Now we create a `Parser`, add that `Rule` (and the rest of the basic rules) and render it.

```kotlin
val parser = Parser<Any?, Node<Any?>>()
  .addRule(FooRule())
  .addRules(SimpleMarkdownRules.createSimpleMarkdownRules())
  
resultText.text = SimpleRenderer.render(
    source = input.text,
    parser = parser,
    renderContext = null
)
```

| Input  | Output |
| ------------- | ------------- |
| Hello \*\*\<Foo\>\*\*  | Hello **Bar** |

## Slightly more complex
Suppose we want to replace all occurrences of `<1234>` (where "1234" is a user id) with `UserNode`.

We'll create the `Rule` the same as before.

```kotlin
class UserNode(private val userId: Int) : Node<Any?>() {
  override fun render(builder: SpannableStringBuilder, renderContext: Any?) {
    builder.append("User $userId")
  }
}

class UserMentionRule : Rule<Any?, UserNode>(Pattern.compile("^<(\\d+)>")) {
  override fun parse(matcher: Matcher, parser: Parser<Any?, in UserNode>, isNested: Boolean): ParseSpec<Any?, UserNode> {
    return ParseSpec.createTerminal(UserNode(matcher.group(1).toInt()))
  }
}
```

The usage is the analogous to the first example.

| Input  | Output |
| ------------- | ------------- |
| Hello <1234>  | Hello User 1234  |

## Real-world application: Adding a Render Context

We modify our `UserNode` thusly to specify that it requires an instance of `RenderContext` in order to render, which contains a map of `Int` -> username.

```kotlin
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
```

Now at the call-site, we specify that the parser produces nodes that require the `RenderContext`, and perform the parse:

```kotlin
val parser = Parser<RenderContext, Node<RenderContext>>()
    .addRule(UserMentionRule())
    .addRules(SimpleMarkdownRules.createSimpleMarkdownRules())

resultText.text = SimpleRenderer.render(
    source = input.text,
    parser = parser,
    renderContext = RenderContext(mapOf(1234 to "CoolDude1234"))
)
```

Note that we only provide `{1234 : "CoolDude1234"}` as our map of usernames. As such, we will render the following:

| Input  | Output |
| ------------- | ------------- |
| Hello <1234>  | Hello CoolDude1234 |
| Hello <6789> | Hello Invalid User |
