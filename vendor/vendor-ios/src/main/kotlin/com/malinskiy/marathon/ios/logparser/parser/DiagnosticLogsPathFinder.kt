package com.malinskiy.marathon.ios.logparser.parser

import com.malinskiy.marathon.ios.executor.listener.ArtifactFinderListener

class DiagnosticLogsPathFinder : ArtifactFinderListener("""(^\s*|\s+)/.+\.log\s*$""".toRegex(), "Diagnostic logs available at")
