package marathon.component.stat

import marathon.model.Status
import react.FC
import react.dom.html.ReactHTML.div

val Stat = FC<StatProps> { props ->
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
