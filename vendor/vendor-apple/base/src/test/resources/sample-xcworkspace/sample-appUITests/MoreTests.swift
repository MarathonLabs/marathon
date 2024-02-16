//
//  sample_appUITests.swift
//  sample-appUITests
//
//  Created by Panforov, Yurii (Agoda) on 28/8/18.
//  Copyright © 2018 Panforov, Yurii (Agoda). All rights reserved.
//

import XCTest

class MoreTests: XCTestCase {

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

    func testPresentModal() {
        let button = app.buttons.element(boundBy: 1)
        button.waitForExistence()
        button.tap()

        let modalLabelPredicate = NSPredicate(format: "%K == %@", "label", "Modal Label")
        let modalLabel = app.staticTexts.element(matching: modalLabelPredicate)
        XCTAssertTrue(modalLabel.exists)
    }


    func testDismissModal() {
        let button = app.buttons.element(boundBy: 1)
        button.tap()

        let dismissButtonPredicate = NSPredicate(format: "%K == %@", "label", "Close")
        let dismissButton = app.buttons.element(matching: dismissButtonPredicate)
        dismissButton.tap()

        let dismissedLabel = app.staticTexts.firstMatch
        XCTAssertEqual(dismissedLabel.label, "Label")
    }
}

private let standardTimeout: TimeInterval = 30.0
private extension XCUIElement {
    @discardableResult
    func waitForExistence() -> Bool {
        return waitForExistence(timeout: standardTimeout)
    }
}
