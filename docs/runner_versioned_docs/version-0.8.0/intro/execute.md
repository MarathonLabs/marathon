---
title: "Execute"
---

Executing the CLI version of marathon requires you to provide all the options through the *Marathonfile*. 
By default, it will be searched in the working directory by the name **Marathonfile**.

If you need a custom file path you can specify it via the options.

```shell-session
foo@bar $ marathon -h
usage: marathon [-h] [--marathonfile MARATHONFILE]

optional arguments:
  -h, --help                     show this help message and exit

  --marathonfile MARATHONFILE,   marathonfile file path
  -m MARATHONFILE
```

Marathon CLI supports the following commands: run, parse and version.

## Run command
```shell-session
foo@bar $ marathon run -h
Usage: marathon run [OPTIONS]

  Run Marathon to execute tests

Options:
  -m, --marathonfile PATH    Marathonfile file path
  --analyticsTracking VALUE  Enable anonymous analytics tracking
  --bugsnag VALUE            Enable/Disable anonymous crash reporting. Enabled by default
  -h, --help                 Show this message and exit
```

## Parse command
```shell-session
foo@bar $ marathon parse -h
Usage: marathon parse [OPTIONS]

  Print the list of tests without executing them

Options:
  -m, --marathonfile PATH  Marathonfile file path
  -o, --output TEXT        Output file name in yaml format
  -h, --help               Show this message and exit
```

## Version command
```shell-session
foo@bar $ marathon version -h
Usage: marathon version [OPTIONS]

  Print version and exit

Options:
  -h, --help  Show this message and exit
```

## Default command
Default command is the run command, so the old CLI syntax works the same way:
```shell-session
foo@bar $ marathon -m MARATHONFILE
```
