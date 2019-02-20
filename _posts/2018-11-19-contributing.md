---
layout: page
title: "Contributing"
category: dev
date: 2018-11-19 16:55:00
order: 2
---
* TOC
{:toc}

Contributions to Marathon's source are welcome! Here is a quick guide where to start.

### Setup

* Install [Oracle JDK][1]
* Install your IDE of choice that supports Kotlin and Gradle, e.g. [IntelliJ IDEA CE][2]
* Install a git client to checkout the repository, e.g. [GitHub Desktop][3]s

Then checkout the marathon repository and open the project in your IDE.

#### Contribution workflow

All the issues are tracked in GitHub, so check the [issue tracker][4] and [project board][5] for a list of something to work on. Alternatively you can submit an issue and then work on it. Before you do that you're highly encouraged to check if this issue is not already closed and talk with us using our [slack channel][6].

Once you pick your issue please assign yourself to work on it.

All the issues relevant to a specific version are assigned to a GitHub milestone so if the issue you're working on is required to fixed for the next version we'll add it to the current milestone.

#### Getting the source code
You can get the source code of marathon by cloning the repo.

```bash
git clone https://github.com/Malinskiy/marathon.git
```

If you plan to submit changes to the repo then please fork the project in GitHub. If you commit frequently then we can add you to the main repository also.

#### Building the project
While working on an issue it's much faster to use CLI distribution which you can build using the ```:cli:installDist``` task in gradle. After this marathon binary with all of it's dependencies can be executed as the following command ```cli/build/install/marathon/bin/marathon```, or you can add this entry to your path ```export PATH=$PATH:$MARATHON_CHECKOUT_DIR/cli/build/install/marathon/bin/marathon```.

If you don't have any project at hand that you want to test use one of our [samples][7].

#### Building and testing the marathon-gradle-plugin
In order to test gradle plugin changes we install all modules into a maven structured folder. Unfortunately you can't use gradle plugins with *SNAPSHOT* version, so in order to test your changes via gradle plugin you have to publish a release version of marathon which gets quite cumbersome, that's why preferred method of testing for development purposes is the CLI.

In order to install all packages into *build/repository* folder you need to execute ```./gradlew publishDefaultPublicationToLocalRepository -PreleaseMode=RELEASE```

After that you need to sync your project. By default all sample projects depend on this local folder and pick up marathon from there. If it's not working check that you actually built everything related to the plugin and the version that you specified in sample project and the one that's published match.

#### Testing changes

Before trying to execute real tests try executing unit and integration tests via ```./gradlew clean test jacocoTestReport integrationTest``` command. If everything passes check relevant sample project where you can test your changes. If your change is related to the core part then you must check that both android and ios vendor implementations will not be affected.

#### Code Style
Before pushing your changes please check if our linter (*detekt*) passes via ```./gradlew clean detektCheck``` command.

### General overview of modules

[1]: https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[2]: https://www.jetbrains.com/idea/download/
[3]: https://desktop.github.com/
[4]: https://github.com/Malinskiy/marathon/issues
[5]: https://github.com/Malinskiy/marathon/projects/1
[6]: https://join.slack.com/t/marathon-test-runner/shared_invite/enQtNDczODU5MDUzOTg0LTNhYjRhOGRhMjMwMGZjMjY5MTY3MDI3ZmMzNTRjYzhmOGRkNDQ5OTIzMzA4ODQ5YjZmMWNiZjljMzcyY2VhMzE
[7]: {{ site.baseurl }}{% post_url 2018-11-19-samples %}
