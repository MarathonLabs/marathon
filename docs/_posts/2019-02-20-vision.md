---
layout: docs
title: "Vision"
category: dev
date: 2019-02-11 14:44:00
order: 3
---

Every project needs a vision in order to prioritise what need to be done and in what order.

# Main priorities

* **Stability of test runs**. This concerns flakiness of tests, flakiness of environment and everything that stands in the way of developers getting proper feedback and solving their problems. This doesn't mean that we hide bad code with retries, in fact quite the opposite: we want to help people find problems that they have in the tests, visualise them, measure and solve.
* **Performance** optimisations for ultra-high parallelization. Our goal is to try to have linear scalability of test execution time with regards to the number of execution units.

Unfortunately these two are quite intertwined from experience and we have to always find balance between these two preferably leaving the choice to the user of marathon and not to developers.

# Vendor extensibility

It should be easy to extend marathon for additional platforms. We should try our best to support whatever main testing technologies are used on each platform, but refrain from using platform specific features which can reused for all platforms instead.
