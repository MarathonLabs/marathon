/**
* Creating a sidebar enables you to:
- create an ordered group of docs
- render a sidebar for each doc of that group
- provide next/previous navigation

The sidebars can be generated from the filesystem, or explicitly defined here.

Create as many sidebars as you want.
*/

// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  docs: [
    {
      type: 'category',
      label: 'Getting Started',
      collapsed: false,
      items: [
        'index',
        'intro/overview',
        'intro/install',
        {
          type: 'category',
          label: 'Configuration',
          collapsed: true,
          items: [
            'intro/configure',
            'configuration/filtering',
            'configuration/analytics',
            'configuration/pooling',
            'configuration/sharding',
            'configuration/sorting',
            'configuration/batching',
            'configuration/flakiness',
            'configuration/retries',
            'configuration/dynamic-configuration',
          ]
        },
        'intro/execute',
        'intro/reports',
        'intro/faq',
        'intro/vision',
        'intro/contribute',
        'intro/special-thanks',
      ],
    },
    {
      type: 'category',
      label: 'Android',
      collapsed: false,
      items: [
        'android',
        'android/install',
        'android/configure',
        'android/examples',
      ],
    },
    {
      type: 'category',
      label: 'Apple',
      collapsed: false,
      items: [
        'apple',
        'apple/workers',
        {
          type: 'category',
          label: 'Configuration',
          collapsed: true,
          items: [
              {
                  type: 'doc',
                  label: 'iOS',
                  id: 'apple/configure/ios'
              },
              {
                  type: 'doc',
                  label: 'watchOS',
                  id: 'apple/configure/ios'
              },
              {
                  type: 'doc',
                  label: 'tvOS',
                  id: 'apple/configure/ios'
              },
              {
                  type: 'doc',
                  label: 'visionOS',
                  id: 'apple/configure/ios'
              },
              {
                  type: 'doc',
                  label: 'macOS',
                  id: 'apple/configure/macos'
              },
          ]
        },
        'apple/examples',
      ],
    },
  ],
};

module.exports = sidebars;
