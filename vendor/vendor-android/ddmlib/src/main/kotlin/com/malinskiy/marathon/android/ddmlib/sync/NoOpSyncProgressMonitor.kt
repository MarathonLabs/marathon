package com.malinskiy.marathon.android.ddmlib.sync

import com.android.ddmlib.SyncService

class NoOpSyncProgressMonitor : SyncService.ISyncProgressMonitor {
    override fun startSubTask(name: String?) = Unit
    override fun start(totalWork: Int) = Unit
    override fun stop() = Unit
    override fun isCanceled() = false
    override fun advance(work: Int) = Unit
}
