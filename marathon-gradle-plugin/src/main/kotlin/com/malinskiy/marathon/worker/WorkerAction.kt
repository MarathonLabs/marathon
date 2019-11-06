package com.malinskiy.marathon.worker

import com.malinskiy.marathon.execution.ComponentInfo

sealed class WorkerAction {

    object Start : WorkerAction()

    class ScheduleTests(val componentInfo: ComponentInfo) : WorkerAction()

    object Finish : WorkerAction()

}
