package marathon.routing

import marathon.component.home.HomeFunctional
import react.FC
import react.Props
import react.createElement
import react.dom.html.ReactHTML.div
import react.router.Route
import react.router.Routes
import react.router.dom.HashRouter

val Application = FC<Props> {
    HashRouter {
        Routes {
            Route {
                index = true
                element = createElement { HomeFunctional() }
//                element = createElement {
//                    div {
//                        +"blablalah"
//                    }
//                }
            }
            Route {
                path = ""
                element = createElement {
                    div {
                        +"404"
                    }
                }
            }
//            Route {
//                path = "/pools"
//
//                val testRunData = document.getElementById("test-run")?.innerHTML ?: error("No embedded test data found")
//                val testRun = JSON.parse<Run>(testRunData)
//                element = Pool.create {
//                    pool = testRun.pools.first()
//                }
//            }
//            Route {
//                path = "/test"
//                val testRunData = document.getElementById("test-run")?.innerHTML ?: error("No embedded test data found")
//                val testRun = JSON.parse<Run>(testRunData)
//                element = Test.create {
//                    test = testRun.pools.first().tests.first()
//                }
//            }
//            Route {
//                path = "/pools/:id/device/:device-id/test/:test-id"
//            }
//            Route {
//                path = "/pools/:id/device/:device-id/test/:test-id/log"
//            }
        }
    }
}
