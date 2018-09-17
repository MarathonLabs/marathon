//
//  AbstractFailingTests.swift
//  sample-appUITests
//
//  Created by Anton Malinskiy on 9/15/18.
//  Copyright © 2018 Panforov, Yurii (Agoda). All rights reserved.
//

import Foundation
import XCTest

class FlakyTests: XCTestCase {
    
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
    
    func testTextFlaky() {
        usleep(100)
        XCTAssertTrue(Bool.random())
    }
    
    func testTextFlaky1() {
        usleep(100)
        XCTAssertTrue(Bool.random())
    }
    
    func testTextFlaky2() {
        usleep(100)
        XCTAssertTrue(Bool.random())
    }
    
    func testTextFlaky3() {
        usleep(100)
        XCTAssertTrue(Bool.random())
    }
    
    func testTextFlaky4() {
        usleep(100)
        XCTAssertTrue(Bool.random())
    }
    
    func testTextFlaky5() {
        usleep(100)
        XCTAssertTrue(Bool.random())
    }
    
    func testTextFlaky6() {
        usleep(100)
        XCTAssertTrue(Bool.random())
    }
    
    func testTextFlaky7() {
        usleep(100)
        XCTAssertTrue(Bool.random())
    }
    
    func testTextFlaky8() {
        usleep(100)
        XCTAssertTrue(Bool.random())
    }
}

private let standardTimeout: TimeInterval = 30.0
private extension XCUIElement {
    @discardableResult
    func waitForExistence() -> Bool {
        return waitForExistence(timeout: standardTimeout)
    }
}
