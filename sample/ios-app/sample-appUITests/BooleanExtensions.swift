//
//  BooleanExtensions.swift
//  sample-appUITests
//
//  Created by Anton Malinskiy on 9/15/18.
//  Copyright © 2018 Panforov, Yurii (Agoda). All rights reserved.
//

import Foundation

extension Bool {
    static func random() -> Bool {
        return arc4random_uniform(2) == 0
    }
}
