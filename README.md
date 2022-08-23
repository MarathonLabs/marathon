[![codecov](https://codecov.io/gh/malinskiy/marathon/branch/develop/graph/badge.svg)](https://codecov.io/gh/malinskiy/marathon)
[![Slack](https://img.shields.io/badge/slack-chat-green.svg?logo=slack&longCache=true&style=flat)](https://bit.ly/2LLghaW)
[![Telegram](https://img.shields.io/static/v1?label=Telegram&message=RU&color=0088CC)](https://t.me/marathontestrunner)

# Marathon
Cross-platform test runner written for Android and iOS projects

## Main focus
- **stability** of test execution adjusting for flakiness in the environment and in the tests.
- **performance** using high parallelization (handling dozens of devices)

## Documentation

Please check the official [documentation](https://marathonlabs.github.io/marathon/) for installation, configuration and more

## [iOS Only] Added Support for XCtest UI Test Functions (with Tags)
You can now tag to your iOS UITests just like you would add tag to your feature file (if you are using [Cucumberish](https://cocoapods.org/pods/Cucumberish) or [XCTGherkin](https://cocoapods.org/pods/XCTest-Gherkin))
_(Multiline tag support is not yet there. Pls make sure that tag is added just above the func signature)_
> How to tag your UITests. See Sample below.
```
    // @Flowers @apple @mock-batch-1
    func testButton() {
        let button = app.buttons.firstMatch
        XCTAssertTrue(button.waitForExistence())
        XCTAssertTrue(button.isHittable)
        button.tap()
        let label = app.staticTexts.firstMatch
        XCTAssertTrue(label.waitForExistence())
    }
```

> Tag reference in marathon file. (See last line)

```
vendorConfiguration:
  type: "iOS"
  derivedDataDir: "derived-data"
  sourceRoot: "sample-appUITests"
  knownHostsPath: ${HOME}/.ssh/known_hosts
  remoteUsername: ${USER}
  remotePrivateKey: ${HOME}/.ssh/marathon
  xcTestRunnerTag: "Flowers"
```

Refer Branch: [`origin/ios-uitest-runner-via-tags`](https://github.com/abhishekbedi1432/Cross-Platform-Test-Runner-Marathon/tree/ios-uitest-runner-via-tags)

> Pls Note:
> Tags are case-sensitive 😉
> xcTestRunnerTag supports single param at the moment
> Multiline tag support is not yet there. Pls make sure that tag is added just above the func signature

License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
