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
    
    $QUOTES
  """
}