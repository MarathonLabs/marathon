//
//  Failingtest.swift
//  sample-appUITests
//
//  Created by Anton Malinskiy on 9/15/18.
//  Copyright © 2018 Panforov, Yurii (Agoda). All rights reserved.
//

import XCTest

class FailingTests: XCTestCase {
    
    private var app: XCUIApplication!
    
    override func setUp() {
        super.setUp()
        
        continueAfterFailure = true
        
        app = XCUIApplication()
        app.launch()
    }
    
    override func tearDown() {
        app = nil
        
        super.tearDown()
    }
    
    func testAlwaysFailing() {
        XCTAssertTrue(false)
    }
    
}

private let standardTimeout: TimeInterval = 30.0
private extension XCUIElement {
    @discardableResult
    func waitForExistence() -> Bool {
        return waitForExistence(timeout: standardTimeout)
    }
}
