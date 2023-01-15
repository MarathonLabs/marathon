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
