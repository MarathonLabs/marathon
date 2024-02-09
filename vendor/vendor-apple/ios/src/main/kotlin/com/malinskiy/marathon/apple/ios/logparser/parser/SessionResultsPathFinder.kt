package com.malinskiy.marathon.apple.ios.logparser.parser

import com.malinskiy.marathon.apple.ios.listener.ArtifactFinderListener

class SessionResultsPathFinder : ArtifactFinderListener("""(^\s*|\s+)/.+\.xcresult\s*$""".toRegex(), "Session results available at")
