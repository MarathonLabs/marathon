---
title: "Parameters"
---

###
You can find all the available marathon-cli parameters by running the command "marathon-cloud --help." 
Below, you'll find a list of the parameters you can set.

```bash
marathon-cloud --help
  -app string
        application filepath. Required
        android example: /home/user/workspace/sample.apk 
        ios example: /home/user/workspace/sample.zip
  -testapp string
        test apk file path. Required
        android example: /home/user/workspace/testSample.apk 
        ios example: /home/user/workspace/sampleUITests-Runner.zip
  -platform string 
        testing platform. Required
        possible values: "Android" or "iOS"
  -api-key string
        api-key for client. Required
  -os-version string
        Android or iOS OS version
  -link string
        link to commit
  -name string
        name for run, for example it could be description of commit
  -o string
        allure raw results output folder
  -system-image string
        OS-specific system image. For Android one of [default,google_apis]. For iOS only [default]
  -isolated bool
        Run each test using isolated execution. Default is false.
```
