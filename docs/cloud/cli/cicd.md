---
title: "CI/CD"
---

###
Marathon Cloud offers several ways to run tests within your CI/CD platform:
1. Platform-Specific Steps
2. Docker Image
3. Marathon CLI

### Platform steps
We provide shared steps for the following platforms:
- [Github Action](https://github.com/MarathonLabs/action-test)
- [CircleCI Orb](https://circleci.com/developer/orbs/orb/marathonlabs/marathon-cloud-orb)
- [Bitrise](https://bitrise.io/integrations/steps/run-tests-using-marathon-cloud)

### Docker image
Some platforms allow usage of [Marathon Docker Image](https://hub.docker.com/r/marathonlabs/marathon-cloud) 
for each step. We recommend using it for Gitlab CI and Jenkins environments.

### Marathon CLI
If the previously mentioned solutions are not applicable, 
you have the alternative of installing the [Marathon CLI](./installation) and executing tests using it.

### Other
If you have problems using all of the previous solutions feel free to [contact us](email:sy@marathonlabs.io).


