package marathon.routing

import marathon.component.home.home
import marathon.component.pool.pool
import marathon.component.test.test
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
                element = home()
            }
            Route {
                path = ""
                element = createElement {
                    div {
                        +"404"
                    }
                }
            }
            Route {
                path = "/pool/:id"
                element = pool()
            }
            Route {
                path = "/device/:device/test/:id"
                element = test()
            }
//            Route {
//                path = "/pools/:id/device/:device-id/test/:test-id"
//            }
//            Route {
//                path = "/pools/:id/device/:device-id/test/:test-id/log"
//            }
        }
    }
}
