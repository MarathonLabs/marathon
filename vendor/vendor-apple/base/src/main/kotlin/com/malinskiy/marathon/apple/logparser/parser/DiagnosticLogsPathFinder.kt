package com.malinskiy.marathon.apple.logparser.parser

import com.malinskiy.marathon.apple.listener.ArtifactFinderListener

class DiagnosticLogsPathFinder : ArtifactFinderListener("""(^\s*|\s+)/.+\.log\s*$""".toRegex(), "Diagnostic logs available at")
