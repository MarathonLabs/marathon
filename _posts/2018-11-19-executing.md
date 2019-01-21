---
layout: page
title: "Executing"
category: doc
date: 2018-11-19 16:55:00
order: 5
---

### CLI

Executing the CLI version of marathon requires you to provide all the options through the *Marathonfile*. By default it will be searched in the working directory by the name **Marathonfile**, but if you need a custom file path you can specify it via the options. Additionally for Android vendor extensions it's possible to provide a custom Android SDK location instead of using *ANDROID_HOME* environment variable or providing this via the Marathonfile.

```bash
$ marathon -h
usage: marathon [-h] [--marathonfile MARATHONFILE]
                       [--android-sdk ANDROID_SDK]

optional arguments:
  -h, --help                     show this help message and exit

  --marathonfile MARATHONFILE,   marathonfile file path
  -m MARATHONFILE

  --android-sdk ANDROID_SDK      Android sdk location
```

### Gradle plugin

Executing your tests via gradle is done via calling appropriate gradle task, for example *marathonDebugAndroidTest*. These tasks will be created for all testing flavors including multi-dimension setup.
