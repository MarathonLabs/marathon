//
//  FlakyTests.swift
//  sample-appUnitTests
//
//  Created by Anton Malinskiy on 29/12/2022.
//  Copyright © 2022 Panforov, Yurii (Agoda). All rights reserved.
//

import XCTest

final class FlakyTests: XCTestCase {

    override func setUpWithError() throws {
        // Put setup code here. This method is called before the invocation of each test method in the class.
    }

    override func tearDownWithError() throws {
        // Put teardown code here. This method is called after the invocation of each test method in the class.
    }

    func testFlaky1() throws {
        usleep(100_000)
        XCTAssertTrue(random())
    }
    func testFlaky2() throws {
        usleep(100_000)
        XCTAssertTrue(random())
    }
    func testFlaky3() throws {
        usleep(100_000)
        XCTAssertTrue(random())
    }
    func testFlaky4() throws {
        usleep(100_000)
        XCTAssertTrue(random())
    }
    func testFlaky5() throws {
        usleep(100_000)
        XCTAssertTrue(random())
    }
    func testFlaky6() throws {
        usleep(100_000)
        XCTAssertTrue(random())
    }
    func testFlaky7() throws {
        usleep(100_000)
        XCTAssertTrue(random())
    }
    func testFlaky8() throws {
        usleep(100_000)
        XCTAssertTrue(random())
    }
    
    func testFlaky9() throws {
        usleep(100_000)
        XCTAssertTrue(random())
    }
    
    func random() -> Bool {
        return rng.next() % 2 == 1
    }
    
    private var rng = SystemRandomNumberGenerator()
}
