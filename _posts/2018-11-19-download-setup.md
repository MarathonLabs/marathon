---
layout: page
title: "Download & Setup"
category: doc
date: 2018-11-19 16:55:00
order: 1
---
* TOC
{:toc}

### Download

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

### Setup

[1]: https://github.com/Malinskiy/marathon/releases
[2]: https://search.maven.org/
[3]: https://bintray.com/bintray/jcenter
