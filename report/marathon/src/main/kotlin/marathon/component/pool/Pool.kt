@file:OptIn(ExperimentalTime::class)

package marathon.component.pool

import kotlinx.browser.document
import marathon.extensions.format
import react.RBuilder
import react.RComponent
import react.State
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.router.dom.NavLink
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class Pool(props: PoolProps) : RComponent<PoolProps, State>(props) {

    override fun componentWillMount() {
        super.componentWillMount()
        document.title = "Pool ${props.pool.id}"
    }

    override fun RBuilder.render() {
        div {
            attrs.className = "content margin-top-20"
            div {
                attrs.className = "title-common"

                a {
                    attrs.href = "toIndex"
                    +"Pools list"
                }

                +"Pool ${props.pool.id}"
            }

            div {
                attrs.className = "card"

                div {
                    attrs.className = "vertical-aligned-content title-common"
                    div {
                        attrs.className = "margin-right-10"
                        +"Tests"
                    }
                    span {
                        attrs.className = "label"
                        +props.pool.tests.size
                    }
                }

                div {
                    attrs.className = "container-expanded list"
                    props.pool.tests.forEachIndexed { index, test ->
                        NavLink {
                            key = index.toString()
                            attrs {
                                href = "/test"
                                className = "list__item row full justify-between ${test.status}"
                            }

                            div {
                                div {
                                    attrs.className = "margin-bottom-5 text-sub-title"
                                    +test.name
                                }
                                div {
                                    attrs.className = "title-l text-sub-title margin-bottom-5 margin-right-10"
                                    +test.class_name
                                }
                                div {
                                    attrs.className = "margin-bottom-5"
                                    +test.package_name
                                }
                            }

                            div {
                                attrs.className = "labels-list"
                                div {
                                    attrs.className = "margin-bottom-5"
                                    span {
                                        attrs.className = "label info"
                                        +test.deviceId
                                        //TODO: colorize the device
                                    }
                                }
                                div {
                                    attrs.className = "margin-bottom-5"
                                    span {
                                        attrs.className = "label big"
                                        +Duration.milliseconds(test.duration_millis.toLong()).format()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
