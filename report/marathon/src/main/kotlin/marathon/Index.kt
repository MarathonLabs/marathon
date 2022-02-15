package marathon

import kotlinx.browser.document
import marathon.component.HomePage
import marathon.model.Run
import react.create
import react.dom.render

fun main() {
    val rootDiv = document.getElementById("root") ?: error("Couldn't find root container!")
    val testRunData = document.getElementById("test-run")?.innerHTML ?: error("No embedded test data found")

    val testRun = JSON.parse<Run>(testRunData)
    render(HomePage.create {
        run = testRun
    }, rootDiv)
}
