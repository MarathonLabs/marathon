//
//  sample_appUITests.swift
//  sample-appUITests
//
//  Created by Panforov, Yurii (Agoda) on 28/8/18.
//  Copyright © 2018 Panforov, Yurii (Agoda). All rights reserved.
//

import XCTest

class StoryboardTests: XCTestCase {

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

        let label = app.staticTexts.firstMatch
        XCTAssertTrue(label.waitForExistence())
    }

    func testDisabledButton() {
        let button = app.buttons.firstMatch
        XCTAssertTrue(button.waitForExistence())
        XCTAssertTrue(button.isHittable)
    }

    func testLabel() {
        let button = app.buttons.firstMatch
        button.waitForExistence()
        button.tap()

        let label = app.staticTexts.firstMatch
        label.waitForExistence()
        XCTAssertEqual(label.label, "Label")
    }

    func disabledTestButton() {
        let button = app.buttons.firstMatch
        button.tap()

        let label = app.staticTexts.firstMatch
        XCTAssertEqual(label.label, "Label")
    }
}

private let standardTimeout: TimeInterval = 30.0
private extension XCUIElement {
    @discardableResult
    func waitForExistence() -> Bool {
        return waitForExistence(timeout: standardTimeout)
    }
}

