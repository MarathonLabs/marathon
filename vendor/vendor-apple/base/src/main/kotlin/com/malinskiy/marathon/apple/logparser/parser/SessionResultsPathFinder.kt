package com.malinskiy.marathon.apple.logparser.parser

import com.malinskiy.marathon.apple.listener.ArtifactFinderListener

class SessionResultsPathFinder : ArtifactFinderListener("""(^\s*|\s+)/.+\.xcresult\s*$""".toRegex(), "Session results available at")
