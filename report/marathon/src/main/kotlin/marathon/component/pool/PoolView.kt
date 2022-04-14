@file:OptIn(ExperimentalTime::class)

package marathon.component.pool

import marathon.extensions.format
import marathon.reducers.AppState
import react.ChildrenBuilder
import react.FC
import react.Props
import react.ReactElement
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.key
import react.redux.useSelector
import react.router.dom.NavLink
import react.router.useParams
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

val PoolView = FC<Props> {
    val id = useParams()["pool"] ?: error("Unknown pool id")
    val pool = useSelector { state: AppState -> state.run.pools.find { it.id == id } } ?: error("Pool $id not found")

    div {
        className = "content margin-top-20"
        div {
            className = "title-common"

            NavLink {
                to = "/"
                className = "with-arrow"
                +"Pools"
            }

            +" Pool ${pool.id}"
        }

        div {
            className = "card"

            div {
                className = "vertical-aligned-content title-common"
                div {
                    className = "margin-right-10"
                    +"Tests"
                }
                span {
                    className = "label"
                    +pool.tests.size
                }
            }

            div {
                className = "container-expanded list"
                pool.tests.forEachIndexed { index, test ->
                    NavLink {
                        key = index.toString()
                        to = "/pool/${pool.id}/device/${test.deviceId}/test/${test.id}"
                        className = "list__item row full justify-between ${test.status}"

                        div {
                            div {
                                className = "margin-bottom-5 text-sub-title"
                                +test.name
                            }
                            div {
                                className = "title-l text-sub-title margin-bottom-5 margin-right-10"
                                +test.class_name
                            }
                            div {
                                className = "margin-bottom-5"
                                +test.package_name
                            }
                        }

                        div {
                            className = "labels-list"
                            div {
                                className = "margin-bottom-5"
                                span {
                                    className = "label info"
                                    +test.deviceId
                                    //TODO: colorize the device
                                }
                            }
                            div {
                                className = "margin-bottom-5"
                                span {
                                    className = "label big"
                                    +test.duration_millis.toLong().milliseconds.format()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


fun ChildrenBuilder.pool(): ReactElement {
    return PoolView.create()
}
