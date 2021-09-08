package com.discord.simpleast.sample

@Suppress("MemberVisibilityCanBePrivate")
object SampleTexts {

  const val TEXT = """
    Some really long **introduction** text that goes on **__forever__** explaining __something__.
  
    single `newline` above. `test` sentence
  """

  const val HEADERS = """
    Alt. __H1__ title
    =======
    stuff
  
    Alt. __H1__ remove marginTop {remove marginTop}
    =======
  
    Alt. __H2__ title
    -----
    * **bold item**
    * another point that is really obvious but just explained to death and should be half the length in reality
  
    # Conclusion __H1__
    So in conclusion. This whole endeavour was just a really long waste of time.
  
  
    ## Appendix __H2__
    ### Sources __H3__
    * mind's eye
    * friend of a friend
  """

  const val QUOTES = """
    Want to test quotes?
    
    > Quoted
    Not quoted
    >> Quote with literal > at the beginning
    Not quoted
    >>>The rest of the message is quoted
    Even here
    > Literal > at beginning of line
    >>> Literal >>> at beginning of line
    Still quoted
  """

  private const val CODE_BLOCK_KOTLIN = """
    Kotlin code block:
    ```kt
    object CodeRules {
      /**
       * Handles markdown syntax for code blocks for a given language.
       */
      val PATTERN_CODE_BLOCK = XXXX
    
      private val CODE_BLOCK_LANGUAGE_GROUP = 2
    
      @Annotation(test=true)
      interface CodeLanguageState<Self : CodeLanguageState<Self>> {
        var codeLangauge: String?  // Inline
        var isCommentBlock: Boolean
      }
      
      fun <T> foo(t: T): List<Int> {
        when (t) {
          is null -> throw Exception("oops!")
          else -> return listOf(1, 2, 3).let { it }
        }
      }
    }
    ```
  """

  private const val CODE_BLOCK_PROTO_BUFFERS = """
    ProtoBuffers code block:
    ```pb
    package com.discord.test
    
    import "google/protobuf/descriptor.proto";

    extend google.protobuf.MessageOptions {
      optional int32 my_message_option = 50001;
    }
    
    message MyMessage {
      option (my_message_option) = 1234;
    
      optional string bar = 1 [default = "Test"];
      oneof qux {
        option (my_oneof_option) = 42;
    
        required string quux = 3;
      }
      repeated in64 ids = 4;
    }
    
    enum MyEnum {
      option (my_enum_option) = true;
    
      FOO = 1 [(my_enum_value_option) = 321];
      BAR = 2;
    }
    
    message RequestType {}
    message ResponseType {}
    
    service MyService {
      option (my_service_option) = FOO;
    
      rpc MyMethod(RequestType) returns(ResponseType) {
        // Note:  my_method_option has type MyMessage.  We can set each field
        //   within it using a separate "option" line.
        option (my_method_option).bar = "Some string";
      }
    }
    ```
  """

  private const val CODE_BLOCK_PYTHON = """
    Python code block:
    ```py
    from com.discord import test
    
    # This is a python comment!
    class CodeRules:
      @Annotation(test=True)
      def test(x=0, y=False):
        while (True):
          if (x is bool):
            continue
          else:
            raise 'oops!' + "I did it again"
      try:
        test(456, False)
      except:
        lambda lookup: 1 in lookup
      finally:
         pass
    ```
  """

  private const val CODE_BLOCK_RUST = """
    Rust code block:
    ```rs
    mod test {
      use std::sync::Arc
      use crate::{UserId}
      
      #[derive(Clone)]
      pub struct Event {name: A, value: B}
      
      impl<T: Clone, V> Event<T, V>
      where
        V: Clone + Send + Sync
      {
        async fn count(&self, req: T) -> Result<Option<String>, String>{
          let name = "test";
          let limit = match req.limit {
            0 => usize::max_value(),
            _ => req.limit,
          }
          let count = self.count(limit).await?;
          Ok(Some(count))
        }
      }
    }
    ```
  """

  private const val CODE_BLOCK_SQL = """
    ```sql
    SELECT name
    from Users as u
    WHERE u.id > 0
      and u.name is NOT NULL
    Order by
      u.name
    ```
  """

  private const val CODE_BLOCK_XML = """
    XML code block:
    ```xml
    <!--
        Multi-line
        Commnent
    -->
    <resources xmlns:tools="http://schemas.android.com/tools">
      
      <attr name="primary_100" format="reference|color" />
      
      <!--<editor-fold desc="Android material styles">-->
      <item name="colorPrimary">@color/black</item>
    </resources>
    ```
  """

  private const val CODE_BLOCK_CRYSTAL = """
    Crystal code block:
    ```cr    
    regex = /\bs|d\b/i
    match = regex.match("start here but end here")

    # New Class

    @[Species(type: "human")]
    class Person(T)
      property balance
      
      def initialize(@name : String)
        @age = 0
        @balance = 100
      end

      def grow(years = 1)
        @age += years
      end

      def info
        puts "My name is #{@name} and I am #{@age}"
      end
      
      def buy(item : T)
        @balance -= 5
      end
    end
    ```
  """
  
  private const val CODE_BLOCK_JAVASCRIPT = """
    JavaScript code block:
    ```js
    const { performance } = require('perf_hooks');
    function getMem() {
      return Object.entries(process.memoryUsage())
               .map(([K, V]) => `${'$'}{K}: ${'$'}{(V / (1024 ** 2)).toFixed(1)}MB`)
               .join('\n');
    }
    const memories = [];
    let timer = performance.now();
    for (let i = 0; i < 50; i++) {
      if (memories.length === 5) break;
      else if (i % 5 === 0) memories.push(getMem());
    }
    timer = performance.now() - timer;

    console.log(`Took ${'$'}{timer} ms`);
    ```
  """

  private const val CODE_BLOCK_GO = """
    Go code block:
    ```go
    package main

    import "fmt"
    
    type User struct {
      ID   uint64
      Name string
    }
    
    const (
      // Create a huge number by shifting a 1 bit left 100 places.
      // In other words, the binary number that is 1 followed by 100 zeroes.
      Big = 1 << 100
      // Shift it right again 99 places, so we end up with 1<<1, or 2.
      Small = Big >> 99
    )
    
    func needInt(x int) int { return x*10 + 1 }
    func needFloat(x float64) float64 {
      return x * 0.1
    }
    
    func main() {
      fmt.Println(needInt(Small))
      fmt.Println(needFloat(Small))
      fmt.Println(needFloat(Big))
    }
    ```
  """

  const val CODE_BLOCKS = """
    # Code block samples
    inlined:```py language code blocks need newline```
    inlined:```kt 
    private fun test() {
      some.call()
    }```
    
    $CODE_BLOCK_KOTLIN
    $CODE_BLOCK_PROTO_BUFFERS
    $CODE_BLOCK_PYTHON
    $CODE_BLOCK_RUST
    $CODE_BLOCK_SQL
    $CODE_BLOCK_XML
    $CODE_BLOCK_CRYSTAL
    $CODE_BLOCK_JAVASCRIPT
    $CODE_BLOCK_GO
    
    That should do it....
  """


  const val BENCHMARK_TEXT = """
    Test __Inner **nested** rules__ as well as *look ahead* rules
    ==========
  
    [0;31mERROR:[0m Signature extraction failed: Traceback (most recent call last):
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
     (caused by ValueError("unknown url type: '/yts/jsbin/player-en_US-vflkk7pUE/base.js'",))
   """

  const val ALL = """
    $TEXT
    $HEADERS
    $CODE_BLOCKS
    $QUOTES
  """
}
