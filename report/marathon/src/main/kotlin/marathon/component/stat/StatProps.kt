package marathon.component.stat

import marathon.model.Status
import react.Props

external interface StatProps : Props {
    var title: String
    var status: Status?
    var value: String
}
