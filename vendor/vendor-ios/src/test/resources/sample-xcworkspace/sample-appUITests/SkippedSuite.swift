//
//  sample_appUITests.swift
//  sample-appUITests
//
//  Created by Panforov, Yurii (Agoda) on 28/8/18.
//  Copyright © 2018 Panforov, Yurii (Agoda). All rights reserved.
//

import XCTest

class SkippedSuite: XCTestCase {

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

    func testTruth() {
        XCTAssertTrue(true)
    }


    func testLies() {
        XCTAssertFalse(false)
    }
}

