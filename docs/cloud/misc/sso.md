---
title: "SSO"
---

###
Marathon Cloud supports Single Sign-On (SSO) via SAML.

### Domain verification
To set up SAML, you must verify ownership of your domain(s). To add your domain, visit the [Domains page](https://cloud.marathonlabs.io/domains) and follow the instructions to add it. After adding the domain, you'll need to add a TXT record to your DNS settings. DNS record updates typically take 5 to 10 minutes, but in some cases, it may take up to 72 hours.

### SAML configuration
To configure SAML, you'll need the following parameters:
- SAML SSO URL
- Entity ID
- Public certificate

Once you've set up these parameters, check the configuration by clicking "Test сonfiguration."
