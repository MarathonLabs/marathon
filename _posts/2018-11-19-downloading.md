---
layout: page
title: "Downloading"
category: doc
date: 2018-11-19 16:55:00
order: 2
---

### Choosing the distribution
By default everyone should try using CLI first. Gradle plugin is only applicable to Android project. However there are some caveats to using it, e.g. incompatible gradle versions between the plugin and your project, incompatible libraries which in the end require you to change gradle version such as Kotlin DSL plugin or Coroutines.

CLI provides other benefits such as ease of having multiple configurations which you will have to implement through your gradle code otherwise. If you upgrade your build setup you're gonna need to check if gradle plugin still works, but you don't depend on your build system using CLI.

In the end CLI is a much more stable and concise choice.

#### CLI
Grab the latest release from [GitHub Releases][1] page. Extract the archive into your apps folder and add the binary to your path using local terminal session or using your profile file (.bashrc or equivalent), e.g.

```
unzip -d $DESTINATION marathon-X.X.X.zip
export PATH=$PATH:$DESTINATION/marathon-X.X.X/bin
```

#### Gradle
Marathon gradle plugin is published to [MavenCentral][2] so make sure you have this registry either by declaring it directly or using an aggregating registry such as [jCenter][3]. Then apply the plugin

```
plugins {
    id 'marathon' version 'X.X.X'
}
```

For kts scripts use the following snippet:

```
plugins {
    id("marathon") version "X.X.X"
}
```

All the relevant test tasks should start with **marathon** prefix such as *marathonDebugAndroidTest*.

[1]: https://github.com/Malinskiy/marathon/releases
[2]: https://search.maven.org/
[3]: https://bintray.com/bintray/jcenter
[4]: https://github.com/Malinskiy/marathon/releases/latest
