---
layout: docs
title: "Executing"
category: doc
date: 2018-11-19 16:55:00
order: 5
---

# CLI

## CLI <0.8.0

Executing the CLI version of marathon requires you to provide all the options through the *Marathonfile*. 
By default, it will be searched in the working directory by the name **Marathonfile**, but if you need a custom file path you can specify it via the options.

```bash
$ marathon -h
usage: marathon [-h] [--marathonfile MARATHONFILE]

optional arguments:
  -h, --help                     show this help message and exit

  --marathonfile MARATHONFILE,   marathonfile file path
  -m MARATHONFILE
```

## CLI >=0.8.0

Since 0.8.0, Marathon CLI supports the following commands: run, parse and version.

### Run command
```bash
$ marathon run -h
Usage: marathon run [OPTIONS]

  Run Marathon to execute tests

Options:
  -m, --marathonfile PATH    Marathonfile file path
  --analyticsTracking VALUE  Enable anonymous analytics tracking
  --bugsnag VALUE            Enable/Disable anonymous crash reporting. Enabled by default
  -h, --help                 Show this message and exit
```

### Parse command
```bash
$ marathon parse -h
Usage: marathon parse [OPTIONS]

  Print the list of tests without executing them

Options:
  -m, --marathonfile PATH  Marathonfile file path
  -o, --output TEXT        Output file name in yaml format
  -h, --help               Show this message and exit
```

### Version command
```bash
$ marathon version -h
Usage: marathon version [OPTIONS]

  Print version and exit

Options:
  -h, --help  Show this message and exit
```

### Default command
Default command is the run command, so the old CLI syntax works the same way:
```bash
$ marathon -m MARATHONFILE
```

# Gradle plugin

Executing your tests via gradle is done via calling appropriate gradle task, for example *marathonDebugAndroidTest*. These tasks will be
 created for all testing flavors including multi-dimension setup.
 
```bash
$ gradle :app:marathonDebugAndroidTest
```
