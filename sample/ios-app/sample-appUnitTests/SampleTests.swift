//
//  SampleTests.swift
//  sample-appUnitTests
//
//  Created by Anton Malinskiy on 29/12/2022.
//  Copyright © 2022 Panforov, Yurii (Agoda). All rights reserved.
//

import Foundation
import XCTest

final class SampleTests: XCTestCase {

    override func setUpWithError() throws {}

    override func tearDownWithError() throws {}

    func testPassing() throws {
        usleep(100_000)
    }
    
    func testFailing() throws {
        usleep(100_000)
        XCTFail("Expected failure")
    }
    
    func testSkipping() throws {
        throw XCTSkip("Sample of skipping a test")
    }

    func testPerformanceExample() throws {
        // This is an example of a performance test case.
        self.measure {
            // Put the code you want to measure the time of here.
            usleep(300_000)
        }
    }

}
