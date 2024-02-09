package com.malinskiy.marathon.apple.ios.logparser.parser

import com.malinskiy.marathon.apple.ios.listener.ArtifactFinderListener

class DiagnosticLogsPathFinder : ArtifactFinderListener("""(^\s*|\s+)/.+\.log\s*$""".toRegex(), "Diagnostic logs available at")
