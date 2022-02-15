package marathon.component

import marathon.model.PoolSummary
import marathon.model.Run
import marathon.model.Status
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.key

external interface HomeProps : Props {
    var run: Run
}

val HomePage = FC<HomeProps> {
    div {
        className = "content margin-top-20"

        div {
            className = "title-common"
            +"Pools"
        }

        for (pool in it.run.pools) {
            Pool {
                this.pool = pool
            }
        }
    }
}

external interface PoolProps : Props {
    var pool: PoolSummary
}

val Pool = FC<PoolProps> { props ->
    div {
        key = props.pool.id
        className = "suite-item card"

        a {
            href = "somewhere"
            className = "title-common with-arrow"
            +"Pool ${props.pool.id}"
        }

        div {
            className = "row full margin-bottom-20 bounded"

            Card {
                title = "Passed"
                status = Status.Passed
                value = props.pool.passed_count.toString()
            }

            Card {
                title = "Failed"
                status = Status.Failed
                value = props.pool.failed_count.toString()
            }

            Card {
                title = "Ignored"
                status = Status.Ignored
                value = props.pool.ignored_count.toString()
            }

            Card {
                title = "Duration"
                value = props.pool.duration_millis.toString()
            }

            Card {
                title = "Devices"
                value = props.pool.devices.size.toString()
            }
        }
    }
}

external interface CardProps : Props {
    var title: String
    var status: Status?
    var value: String
}

val Card = FC<CardProps> { props ->
    div {
        className = "card-info"

        div {
            className = "text-sub-title-light"
            +props.title
        }

        div {
            className = "card-info__content" + when (props.status) {
                Status.Passed -> " status-passed"
                Status.Failed -> " status-failed"
                Status.Ignored -> " status-ignored"
                else -> ""
            }
            +props.value
        }
    }
}
