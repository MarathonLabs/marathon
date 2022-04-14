@file:OptIn(ExperimentalTime::class)

package marathon.component.test

import marathon.extensions.format
import marathon.model.ShortTest
import marathon.model.Status
import marathon.reducers.AppState
import react.ChildrenBuilder
import react.FC
import react.Props
import react.ReactElement
import react.create
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.redux.useSelector
import react.router.dom.NavLink
import react.router.useParams
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

val TestView = FC<Props> {
    val params = useParams()
    val poolId = params["pool"] ?: error("No pool specified")
    val deviceId = params["device"] ?: error("No device specified")
    val testId = params["test"] ?: error("No test id specified")

    val test: ShortTest = useSelector { state: AppState ->
        state.run.pools.find { it.id == poolId }
            ?.tests
            ?.find { it.id == testId }
            ?: error("Test $testId not found for device $deviceId")
    }

    div {
        val testClass = buildString {
            append("label margin-right-10")
            when (test.status) {
                Status.Passed -> append(" success")
                Status.Failed -> append(" alert")
                Status.Ignored -> Unit
            }
        }

        div {
            className = "content margin-top-20"

            div {
                className = "title-common"

                NavLink {
                    to = "/"
                    className = "with-arrow"
                    +"Pools"
                }

                +" "

                NavLink {
                    to = "/pool/${poolId}"
                    className = "with-arrow"
                    +"Pool $poolId"
                }
            }

            div {
                className = "margin-top-20"

                div {
                    className = "card row full justify-between test-page ${test.status}"

                    div {
                        className = "margin-right-20"
                        div {
                            className = "margin-bottom-10 vertical-aligned-content"
                            div {
                                className = testClass
                                +test.status.toString()
                            }
                            span {
                                className = "test-page__title status-${test.status}"
                                +test.name
                            }
                        }
                        div {
                            className = "title-l text-sub-title margin-bottom-5"
                            +test.class_name
                        }
                        div {
                            className = "margin-bottom-5"
                            +test.package_name
                        }
                    }

                    div {
                        className = "card-info__content"
                        +test.duration_millis.toLong().milliseconds.format()
                    }

                    div {
                        className = "card-info__content"
                        NavLink {
                            to = "/logs"
                            +"Log file"
                        }
                    }
                }

//                if(test.video.isNotBlank()) {
//                    div {
//                        className = "card row full"
//                        //TODO: actual player
//                    }
//                }
//                
//                if(test.screenshot.isNotBlank()) {
//                    div {
//                        className = "card row full"
//                        img {
//                            src = test.screenshot
//                        }
//                    }
//                }
            }
        }
    }
}

fun ChildrenBuilder.test(): ReactElement {
    return TestView.create()
}
