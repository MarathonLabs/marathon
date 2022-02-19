@file:OptIn(ExperimentalTime::class)

package marathon.component.test

import marathon.extensions.format
import marathon.model.Status
import react.FC
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.router.dom.NavLink
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

val Test = FC<TestProps> { props ->
    val test = props.test

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
                className = "title-common vertical-aligned-content"
                NavLink {
                    to = "/"
                    +"Pools list"
                }
                NavLink {
                    to = "/pools"
                    +"Pool //TODO"
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
                        +Duration.milliseconds(test.duration_millis.toLong()).format()
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
