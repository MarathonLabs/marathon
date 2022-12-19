package com.malinskiy.marathon.ios.logparser.parser

import com.malinskiy.marathon.ios.executor.listener.ArtifactFinderListener

class SessionResultsPathFinder : ArtifactFinderListener("""(^\s*|\s+)/.+\.xcresult\s*$""".toRegex(), "Session results available at")
