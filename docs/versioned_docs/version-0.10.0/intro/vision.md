---
title: "Vision"
---

Every project needs a vision to prioritise what needs to be done.

# Main priorities

* **Stability of test runs**. This concerns flakiness of tests, flakiness of environment and everything that stands in the way of developers getting proper feedback and solving their problems. This doesn't mean that we hide bad code with retries, in fact quite the opposite: we want to help people find problems that they have in the tests, visualise them, measure and solve.
* **Performance** optimisations for ultra-high parallelization. Our goal is to try to have linear scalability of test execution time in regard to the number of execution units.

Unfortunately these two are quite intertwined, and we have to always find balance between these two preferably leaving the choice to the user of marathon and not to developers of marathon.

# Vendor extensibility
It should be easy to extend marathon for additional platforms. We should try our best to support whatever main testing technologies are used
on each platform, but refrain from using platform-specific features which can be reused from platform-agnostic implementations.

# Infrastructure provisioning
Setting up a testing at scale requires a lot of components, test runner is but a small piece here.

Marathon is a test runner that doesn't and shouldn't know anything about the provisioning of compute resources since
every setup is running a different orchestration plane (kubernetes, aws, gcp, terraform, etc). It is not practical 
to put the responsibility of spinning up compute into the open source version of marathon.

Putting up proper abstractions for every vendor implementation enables marathon to support any infrastructure, e.g.
connecting to devices via adb on Android is the only way (any other interaction is just a wrapper around adb).

This doesn't mean we don't want to help setting up infrastructure though, it's just a separate piece of the puzzle.

[Marathon Cloud][1] is a project that aims to solve testing as a service problem at scale. If marathon seems like
what you need for running your tests but you don't have the capacity to orchestrate the required compute for your
test runs then Marathon Cloud might be a good alternative. Ideally the testing tools should allow your engineers to
work on business problems rather than reinvent yet another device farm solution, support it and scale it.

[1]: https://marathonlabs.io/
