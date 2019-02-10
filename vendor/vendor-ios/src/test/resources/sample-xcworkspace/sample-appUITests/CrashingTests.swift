//
//  CrashingTests.swift
//  sample-appUITests
//
//  Created by Panforov, Yurii (Agoda) on 22/11/18.
//  Copyright © 2018 Panforov, Yurii (Agoda). All rights reserved.
//

import XCTest

class CrashingTests: XCTestCase {
        
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
        let button = app.buttons.element(boundBy: 2)
        XCTAssertTrue(button.waitForExistence())
        XCTAssertTrue(button.isHittable)

        button.tap()
        
        XCTAssertTrue(button.waitForExistence())
    }
}

private let standardTimeout: TimeInterval = 30.0
private extension XCUIElement {
    @discardableResult
    func waitForExistence() -> Bool {
        return waitForExistence(timeout: standardTimeout)
    }
}

