---
layout: docs
title: "Contribute"
category: dev
date: 2018-11-19 16:55:00
order: 2
---

Contributions to Marathon's source are welcome! Here is a quick guide on where to start.

# Setup

* Install your flavour of Java Development Kit, e.g. [Eclipse Temurin][1]
* Install an IDE of choice that supports Kotlin and Gradle, e.g. [IntelliJ IDEA CE][2]
* Install a git client to check out the repository, e.g. [GitHub Desktop][3]

Checkout the marathon repository and open the project in your IDE.

```shell-session
foo@bar $ git clone https://github.com/MarathonLabs/marathon.git
```

## Contribution workflow

All issues are tracked in GitHub, so check the [issue tracker][4] and [project board][5] for a list of something to work on.
Alternatively, you can submit an issue and then work on it. Before you do that, we highly encourage you to check if this issue may be a
duplicate. If it is new - chat with us using our [Slack channel][6].

Once you pick your issue please assign yourself to work on it.

All the issues relevant to a specific version are assigned to a GitHub milestone so if the issue you're working on is required to fixed for
the next version we'll add it to the current milestone.

## Getting the source code

You can get the source code of marathon by cloning the repo.

```bash
git clone https://github.com/MarathonLabs/marathon.git
```

If you plan to submit changes to the repo then please fork the project in GitHub. If you commit frequently then we can add you to the main
repository also.

## Included run configurations for IntelliJ IDEA

Marathon's project has built-in run configurations for executing included sample projects:

![html report home page](/img/idea-run-configurations.png "IntelliJ Run Configurations")

You can use this setup to quickly debug something on a sample app, or you can change the workdir of the configuration and debug your own
codebase. Default configurations for IntelliJ use CLI version of marathon.

## Building the project

### CLI

While working on an issue it's much faster to use CLI distribution which you can build using the ```:cli:installDist``` task in gradle:

```shell-session
foo@bar $ ./gradlew :cli:installDist
```

This task builds marathon binary with all of its dependencies.
The output binary can be found at ```cli/build/install/marathon/bin/marathon```.

If you use this output frequently consider changing your path to use this custom version:

```shell-session
foo@bar $ export PATH=$PATH:$MARATHON_CHECKOUT_DIR/cli/build/install/marathon/bin
```

### marathon-gradle-plugin

To test gradle plugin changes we install all modules into a maven structured folder.

To install all packages into *build/repository* folder you need to execute

```shell-session
./gradlew publishDefaultPublicationToLocalRepository -PreleaseMode=SNAPSHOT
```

After that you need to sync your project and point it to your local repository. Alternatively you can publish to maven local.

By default, all sample projects depend on the local folder and pick up marathon from there.
If it's not working, check you've actually built everything related to the plugin and the version that you specified in sample project and
the one that's published do indeed match.

## Creating a custom distribution

If you want to create a distributable zip or a tarball:

```shell-session
foo@bar $ ./gradlew :cli:distZip
foo@bar $ ls cli/build/distributions
marathon-X.X.X-SNAPSHOT.zip
```

## Testing changes

Before trying to execute real tests try executing unit and integration tests via ```./gradlew clean test jacocoTestReport integrationTest```
command. Assuming everything passes check relevant sample project where you can test your changes. If your change is related to the core
part then you must check that both android and ios vendor implementations will not be affected.

## Linting

Before pushing your changes please check if our linter (*detekt*) passes via ```./gradlew clean detektCheck``` command.

## General overview of modules

### core

This is the main logic part of the runner.

### marathon-gradle-plugin

This is a gradle plugin implementation for Android testing

### vendor

This is custom vendor implementation related to specific platform. One specific implementation that is important is
vendor-test, this is a fake implementation that we're using for integration testing

### report

This is a group of modules which implement various reports that marathon generates after the build

### cli

This is the command-line interface wrapper for Marathon

### analytics:usage

This is an analytics implementation that we're using for tracking anonymized usage of marathon.

# Development chat

We're available for any questions or proposals on [Slack][6] or [Telegram][7] if you prefer to just chat. Feel free to join!

[1]: https://projects.eclipse.org/projects/adoptium.temurin

[2]: https://www.jetbrains.com/idea/download/

[3]: https://desktop.github.com/

[4]: https://github.com/MarathonLabs/marathon/issues

[5]: https://github.com/MarathonLabs/marathon/projects/1

[6]: https://bit.ly/2LLghaW

[7]: https://t.me/marathontestrunner
