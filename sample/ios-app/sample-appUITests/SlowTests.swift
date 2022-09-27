//
//  SlowTests.swift
//  sample-appUITests
//
//  Created by Anton Malinskiy on 9/15/18.
//  Copyright © 2018 Panforov, Yurii (Agoda). All rights reserved.
//

//
//  AbstractFailingTests.swift
//  sample-appUITests
//
//  Created by Anton Malinskiy on 9/15/18.
//  Copyright © 2018 Panforov, Yurii (Agoda). All rights reserved.
//

import Foundation
import XCTest

class SlowTests: XCTestCase {
    
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
    
    // @Flowers
    func testTextSlow() {
        sleep(5)
        let button = app.buttons.firstMatch
        button.waitForExistence()
        button.tap()
        
        let label = app.staticTexts.firstMatch
        label.waitForExistence()
        XCTAssertEqual(label.label, "Label")
    }
    
    // @Flowers
    func testTextSlow1() {
        sleep(5)
        let button = app.buttons.firstMatch
        button.waitForExistence()
        button.tap()
        
        let label = app.staticTexts.firstMatch
        label.waitForExistence()
        XCTAssertEqual(label.label, "Label")
    }
    
    func testTextSlow2() {
        sleep(5)
        let button = app.buttons.firstMatch
        button.waitForExistence()
        button.tap()
        
        let label = app.staticTexts.firstMatch
        label.waitForExistence()
        XCTAssertEqual(label.label, "Label")
    }
    
    func testTextSlow3() {
        sleep(5)
        let button = app.buttons.firstMatch
        button.waitForExistence()
        button.tap()
        
        let label = app.staticTexts.firstMatch
        label.waitForExistence()
        XCTAssertEqual(label.label, "Label")
    }
    
    func testTextSlow4() {
        sleep(5)
        let button = app.buttons.firstMatch
        button.waitForExistence()
        button.tap()
        
        let label = app.staticTexts.firstMatch
        label.waitForExistence()
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

