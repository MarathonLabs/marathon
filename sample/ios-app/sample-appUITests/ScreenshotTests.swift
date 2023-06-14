//
//  ScreenshotTests.swift
//  sample-appUITests
//
//  Created by Anton Malinskiy on 29/12/2022.
//  Copyright © 2022 Panforov, Yurii (Agoda). All rights reserved.
//

import XCTest

class ScreenshotTests: XCTestCase {

    private var app: XCUIApplication!

    override func setUp() {
        super.setUp()
        
        continueAfterFailure = false

        app = XCUIApplication()
        app.launch()
    }

    override func tearDown() {
        app = nil

        super.tearDown()
    }

    func testButton() {
        let button = app.buttons.firstMatch
        XCTAssertTrue(button.waitForExistence())
        XCTAssertTrue(button.isHittable)

        button.tap()

        let fullScreenshot = XCUIScreen.main.screenshot()
        //This will end up in the report
        let screenshot = XCTAttachment(screenshot: fullScreenshot)
        screenshot.lifetime = .keepAlways //This will be set externally from test runner, so here only for demo purposes
        add(screenshot)
    }
}

private let standardTimeout: TimeInterval = 30.0
private extension XCUIElement {
    @discardableResult
    func waitForExistence() -> Bool {
        return waitForExistence(timeout: standardTimeout)
    }
}

