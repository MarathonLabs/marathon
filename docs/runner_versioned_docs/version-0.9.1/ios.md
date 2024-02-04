---
title: "Overview"
---

Executing tests on iOS simulators requires access to Apple hardware capable of executing tests. This can be a local macOS instance or a
remote instance accessible via [secure shell][2]. For remote access file transfers are carried out incrementally using [rsync][3].

Device provider can provision simulators on-demand, reuse existing ones if they match desired configuration as well as utilize
pre-provisioned simulators. See documentation on [workers][1] for more information on this topic.

Marathon can run both XCUITests and XCTests. Test bundle requires you to specify application under test as well as test application.
After preprocessing both of these inputs are distilled into an application bundle (e.g. `my.app`) and xctest bundle (e.g. `my-tests.xctest`)
You can specify `.ipa` [application archives][4] as well as `.zip` with the same content as application archive. They will be searched for
the
application and xctest bundles. If there are multiple entries matching description - marathon will fail.

:::tip

It is much easier to supply the `.app` application bundle and `.xctest` bundle directly instead of wasting time on packaging a signed
application
archive and depending on runtime discovery of your bundles

:::

## Compatibility notes

### XcodeGen

For users of [XcodeGen][5] marathon requires you to specify the following in your project's spec to make sure Info.plist is generated for
parsing your app's information:

```yaml
settings:
  GENERATE_INFOPLIST_FILE: YES
```

### Test plans

Support for Xcode test plans is intentionally missing: fundamentally test plan is a way of filtering tests and Marathon supports universal
test filtering regardless of the test platform. Having multiple layers of filtering is undesirable and leading to cumbersome user
experience. The simplest way to achieve test plans is to provide a list of tests in a file by combining [dynamic configuration][7]
and [fully-qualified-test-name filter][8] providing the external file via [values-file-filtering][9].

See [XcodeGen docs][6] for more information.


[1]: ios/workers.md

[2]: https://en.wikipedia.org/wiki/Secure_Shell

[3]: https://en.wikipedia.org/wiki/Rsync

[4]: https://en.wikipedia.org/wiki/.ipa

[5]: https://github.com/yonaskolb/XcodeGen

[6]: https://github.com/yonaskolb/XcodeGen/blob/a9ed3cec0800ac9a8b4cd5cfb0bb3ee76429a22c/Docs/ProjectSpec.md

[7]: configuration/dynamic-configuration.md

[8]: configuration/filtering.md#fully-qualified-test-name

[9]: configuration/filtering.md#values-filtering
