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

#### Building the project
While working on an issue it's much faster to use CLI distribution which you can build using the ```:cli:installDist``` task in gradle. After this marathon binary with all of it's dependencies can be executed as the following command ```cli/build/install/marathon/bin/marathon```, or you can add this entry to your path ```export PATH=$PATH:$MARATHON_CHECKOUT_DIR/cli/build/install/marathon/bin/marathon```.

If you don't have any project at hand that you want to test use one of our [samples][7].

#### Testing changes

#### Code Style

### Documentation

[1]: https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
[2]: https://www.jetbrains.com/idea/download/
[3]: https://desktop.github.com/
[4]: https://github.com/Malinskiy/marathon/issues
[5]: https://github.com/Malinskiy/marathon/projects/1
[6]: https://join.slack.com/t/marathon-test-runner/shared_invite/enQtNDczODU5MDUzOTg0LTNhYjRhOGRhMjMwMGZjMjY5MTY3MDI3ZmMzNTRjYzhmOGRkNDQ5OTIzMzA4ODQ5YjZmMWNiZjljMzcyY2VhMzE
[7]: {{ site.baseurl }}{% post_url 2018-11-19-samples %}
