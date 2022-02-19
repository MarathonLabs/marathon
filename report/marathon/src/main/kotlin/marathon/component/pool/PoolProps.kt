package marathon.component.pool

import marathon.model.PoolSummary
import react.Props

external interface PoolProps : Props {
    var pool: PoolSummary
}
