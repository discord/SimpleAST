package com.discord.simpleast.sample

@Suppress("MemberVisibilityCanBePrivate")
object SampleTexts {

  const val TEXT = """
    Some really long **introduction** text that goes on **__forever__** explaining __something__.
  
    single newline above
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

  const val CODE_BLOCKS = """
    # Code block samples
    inlined:```kt private fun test() {}```
    inlined:```kt private fun test() {
      some.call()
    }```
    
    $CODE_BLOCK_KOTLIN
    $CODE_BLOCK_PYTHON
    $CODE_BLOCK_RUST
    
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