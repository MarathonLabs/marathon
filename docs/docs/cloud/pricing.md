---
title: "Pricing"
---

We operate on a Pay-As-You-Go model, meaning you only pay for the actual time spent running tests on our virtual devices. The pricing is set at a straightforward rate of $2 per hour for each virtual device.

## Billing
To better understand this, picture having a test suite that requires 2 hours and 15 minutes (or 135 minutes) to run on one device. With Marathon Cloud, we automatically deploy an infrastructure of 9 virtual devices for you (135 minutes / 9 devices = 15 minutes). We then evenly distribute the tests among these devices, run them, and deliver the results to you within just 15 minutes. However, your tests will have used up 2 hours and 15 minutes (or 2.25 hours) of our device time in total. This means the cost of this test run for you would be $4.5.

# Calculating Time per Device

The computation of time for each device during a test run is based on the time span from the beginning of the first test on that device to the end of the last test on the same device. This means we don't factor in the device booting process or the application installation stage. We do, however, take into account the brief pauses between tests and the periodic device clean-ups that occur when using a batching strategy. Typically, pauses between tests take only fractions of a second, but clean-ups can require up to 6-8 seconds.
