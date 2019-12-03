package com.malinskiy.marathon.report.attachment

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.io.AttachmentManager
import com.malinskiy.marathon.report.Reporter

class AttachmentsReporter(
    private val attachmentManager: AttachmentManager
) : Reporter {

    /**
     * Writes test attachments to the destination directory
     */
    override fun generate(executionReport: ExecutionReport) {
        executionReport
            .summary
            .pools
            .forEach { pool ->
                val poolId = pool.poolId
                pool.tests.forEach { test ->
                    test.attachments.forEach { attachment ->
                        attachmentManager.writeToTarget(poolId, test.device, test.test, attachment)
                    }
                }
            }
    }
}
