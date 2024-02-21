---
title: "Parameters"
---

###
You can find all the available marathon-cli parameters by running the command `marathon-cloud help.` 
Below, you'll find a list of the commands and options you can set.

```bash
--> marathon-cloud help

Command-line client for Marathon Cloud

Usage: marathon-cloud [OPTIONS] [COMMAND]

Commands:
  run          Submit a test run
  download     Download artifacts from a previous test run
  completions  Output shell completion code for the specified shell (bash, zsh, fish)
  help         Print this message or the help of the given subcommand(s)

Options:
  -v, --verbose...  Increase logging verbosity
  -q, --quiet...    Decrease logging verbosity
  -h, --help        Print help
  -V, --version     Print version
  
--> marathon-cloud run android -h
                                                 
Run tests for Android

Usage: marathon-cloud run android [OPTIONS] --test-application <TEST_APPLICATION> --api-key <API_KEY>

Options:
  -a, --application <APPLICATION>
          application filepath, example: /home/user/workspace/sample.apk
  -t, --test-application <TEST_APPLICATION>
          test application filepath, example: /home/user/workspace/testSample.apk
      --os-version <OS_VERSION>
          OS version [10, 11, 12, 13]
      --system-image <SYSTEM_IMAGE>
          Runtime system image [possible values: default, google-apis]
  -v, --verbose...
          More output per occurrence
  -o, --output <OUTPUT>
          Output folder for test run results
  -q, --quiet...
          Less output per occurrence
      --isolated <ISOLATED>
          Run each test in isolation, i.e. isolated batching. [possible values: true, false]
      --filter-file <FILTER_FILE>
          Test filters supplied as a YAML file following the schema at https://docs.marathonlabs.io/runner/configuration/filtering/#filtering-logic. For iOS see also https://docs.marathonlabs.io/runner/next/ios#test-plans
      --wait
          Wait for test run to finish if true, exits after triggering a run if false
      --name <NAME>
          name for run, for example it could be description of commit
      --link <LINK>
          link to commit
      --api-key <API_KEY>
          Marathon Cloud API key [env: MARATHON_CLOUD_API_KEY=]
      --base-url <BASE_URL>
          Base url for Marathon Cloud API [default: https://cloud.marathonlabs.io/api/v1]
      --instrumentation-arg <INSTRUMENTATION_ARG>
          Instrumentation arguments, example: FOO=BAR
  -h, --help
          Print help
          
--> marathon-cloud run ios -h

Run tests for iOS

Usage: marathon-cloud run ios [OPTIONS] --application <APPLICATION> --test-application <TEST_APPLICATION> --api-key <API_KEY>

Options:
  -a, --application <APPLICATION>
          application filepath, example: /home/user/workspace/sample.zip
  -t, --test-application <TEST_APPLICATION>
          test application filepath, example: /home/user/workspace/sampleUITests-Runner.zip
  -o, --output <OUTPUT>
          Output folder for test run results
      --isolated <ISOLATED>
          Run each test in isolation, i.e. isolated batching. [possible values: true, false]
  -v, --verbose...
          More output per occurrence
      --filter-file <FILTER_FILE>
          Test filters supplied as a YAML file following the schema at https://docs.marathonlabs.io/runner/configuration/filtering/#filtering-logic. For iOS see also https://docs.marathonlabs.io/runner/next/ios#test-plans
  -q, --quiet...
          Less output per occurrence
      --wait
          Wait for test run to finish if true, exits after triggering a run if false
      --name <NAME>
          name for run, for example it could be description of commit
      --link <LINK>
          link to commit
      --api-key <API_KEY>
          Marathon Cloud API key [env: MARATHON_CLOUD_API_KEY=]
      --base-url <BASE_URL>
          Base url for Marathon Cloud API [default: https://cloud.marathonlabs.io/api/v1]
      --xctestrun-env <XCTESTRUN_ENV>
          xctestrun environment variables, example FOO=BAR

  -h, --help
          Print help
```
