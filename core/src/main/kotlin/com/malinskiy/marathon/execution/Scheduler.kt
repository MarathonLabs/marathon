package com.malinskiy.marathon.execution

/**
 * The logic of scheduler
 *
 * 1. Pooling:      Create pools of devices
 * 2. Sharding:     Define sharding (creates device-test association)
 * 3. Flakiness:    Add known retries to tests in all shards
 * 4. Sorting:      Sort all tests
 * 5. Batching:     TestBatch into manageable chunks
 * 6. Retries:      Retry if something fails and we didn't account for it in the flakiness
 */
class Scheduler(val timeout: Int, val batchSize: Int) {

}