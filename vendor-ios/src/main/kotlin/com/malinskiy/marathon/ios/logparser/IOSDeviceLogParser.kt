package com.malinskiy.marathon.ios.logparser

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.ios.logparser.formatter.PackageNameFormatter
import com.malinskiy.marathon.ios.logparser.listener.ProgressReportingListener
import com.malinskiy.marathon.ios.logparser.listener.TestLogListener
import com.malinskiy.marathon.ios.logparser.parser.*
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.experimental.CompletableDeferred

class IOSDeviceLogParser(device: Device,
                         packageNameFormatter: PackageNameFormatter,
                         poolId: DevicePoolId,
                         testBatch: TestBatch,
                         deferredResults: CompletableDeferred<TestBatchResults>,
                         progressReporter: ProgressReporter): StreamingLogParser {

    private val underlyingLogParser: StreamingLogParser
    private val diagnosticsParser: TestDiagnosticsParser
    init {
        val testLogListener = TestLogListener()
        diagnosticsParser = TestDiagnosticsParser()
        underlyingLogParser = CompositeLogParser(
            listOf(
                //Order matters here: first grab the log with log listener,
                //then use this log to insert into the test report
                testLogListener,
                DeviceFailureParser(),
                diagnosticsParser,
                TestRunProgressParser(
                    SystemTimer(),
                    packageNameFormatter,
                    listOf(
                        ProgressReportingListener(
                            device = device,
                            poolId = poolId,
                            testBatch = testBatch,
                            deferredResults = deferredResults,
                            progressReporter = progressReporter,
                            testLogListener = testLogListener
                        ),
                        testLogListener
                    )
                ),
                DebugLogPrinter()
            )
        )
    }

    val diagnosticLogPaths: Collection<String>
        get() = diagnosticsParser.diagnosticLogPaths

    override fun close() = underlyingLogParser.close()

    override fun onLine(line: String) = underlyingLogParser.onLine(line)
}
