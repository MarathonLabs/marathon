@file:OptIn(ExperimentalTime::class)

package marathon.component.home

import kotlinx.browser.document
import marathon.component.pool.PoolSummary
import marathon.reducers.AppState
import react.Props
import react.RBuilder
import react.RComponent
import react.State
import react.create
import react.dom.html.ReactHTML.div
import react.fc
import react.redux.useSelector
import kotlin.time.ExperimentalTime

val HomeFunctional = fc<Props> {
    val run = useSelector { state: AppState -> state.run }

    div {
        attrs {
            className = "content margin-top-20"
        }
        div {
            attrs {
                className = "title-common"
            }
            +"Pools"
        }

        console.log(run.pools.unsafeCast<Array<Any>>().size)
        run.pools.iterator().forEach {
            PoolSummary.create() {
                this.pool = it
            }
        }
    }
}

class Home : RComponent<Props, State>() {
    private val run = useSelector { state: AppState -> state.run }

    override fun RBuilder.render() {
        div {
            +"testing"
        }
        div {
            attrs {
                className = "content margin-top-20"
            }
            div {
                attrs {
                    className = "title-common"
                }
                +"Pools"
            }

            for (pool in run.pools) {
                PoolSummary.create() {
                    this.pool = pool
                }
            }
        }

        //TODO: Timeline?
    }

    override fun componentWillMount() {
        super.componentWillMount()
        document.title = run.title
    }
}

fun RBuilder.home() = child(Home::class) {}
