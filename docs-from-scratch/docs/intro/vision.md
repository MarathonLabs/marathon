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
