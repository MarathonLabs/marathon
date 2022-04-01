@file:OptIn(ExperimentalTime::class)

package marathon.component.home

import marathon.component.pool.poolSummary
import marathon.reducers.AppState
import react.ChildrenBuilder
import react.FC
import react.Props
import react.create
import react.dom.html.ReactHTML.div
import react.redux.useSelector
import kotlin.time.ExperimentalTime

val HomeView = FC<Props> {
    val run = useSelector { state: AppState -> state.run }
    div {
        className = "content margin-top-20"
        div {
            className = "title-common"
            +"Pools"
        }

        run.pools.forEach {
            poolSummary(it)
        }

        //TODO: Timeline?
    }
}

fun ChildrenBuilder.home() = HomeView.create()
