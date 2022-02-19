@file:OptIn(ExperimentalTime::class)

package marathon.component.pool

import marathon.component.stat.Stat
import marathon.extensions.format
import marathon.model.Status
import react.FC
import react.dom.html.ReactHTML.div
import react.key
import react.router.dom.NavLink
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

val PoolSummary = FC<PoolProps>(displayName = "Pool summary") { props ->
    div {
        key = props.pool.id
        className = "suite-item card"

        NavLink {
            to = "/pools"
            className = "title-common with-arrow"
            +"Pool ${props.pool.id}"
        }

        div {
            className = "row full margin-bottom-20 bounded"

            Stat {
                title = "Passed"
                status = Status.Passed
                value = props.pool.passed_count.toString()
            }

            Stat {
                title = "Failed"
                status = Status.Failed
                value = props.pool.failed_count.toString()
            }

            Stat {
                title = "Ignored"
                status = Status.Ignored
                value = props.pool.ignored_count.toString()
            }

            Stat {
                title = "Duration"
                value = Duration.milliseconds(props.pool.duration_millis.toLong()).format()
            }

            Stat {
                title = "Devices"
                value = props.pool.devices.size.toString()
            }
        }
    }
}
