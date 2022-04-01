package marathon

import kotlinx.browser.document
import kotlinx.serialization.json.Json
import marathon.model.Run
import marathon.reducers.AppState
import marathon.reducers.ViewState
import marathon.reducers.rootReducer
import marathon.routing.Application
import react.createElement
import react.dom.render
import react.redux.provider
import redux.createStore
import redux.rEnhancer


fun main() {
    val rootDiv = document.getElementById("root") ?: error("Couldn't find root container!")

    val testRunData = document.getElementById("test-run")?.innerHTML ?: error("No embedded test data found")
    val testRun = Json.decodeFromString(Run.serializer(), testRunData)
    val store = createStore(::rootReducer, AppState(testRun, ViewState.Home), rEnhancer())

    render(createElement {
        provider(store) {
            Application()
        }
    }, rootDiv) {
    }
}
