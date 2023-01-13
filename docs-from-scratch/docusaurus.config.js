// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');
const path = require("path");

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'Marathon',
    tagline: 'Cross-platform test runner written for Android and iOS projects',
    url: 'https://docs.marathonlabs.io/',
    baseUrl: '/',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',
    favicon: 'img/favicon-96x96.png',
  
    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },

    markdown: {
        mermaid: true,
    },

    plugins: [
        'docusaurus-plugin-sass',
        [
            'docusaurus-plugin-module-alias',
            {
                alias: {
                    'styled-components': path.resolve(__dirname, './node_modules/styled-components'),
                    react: path.resolve(__dirname, './node_modules/react'),
                    'react-dom': path.resolve(__dirname, './node_modules/react-dom'),
                },
            },
        ],
    ],

    presets: [
        [
            '@docusaurus/preset-classic',
            {
                docs: {
                    routeBasePath: '/',
                    sidebarPath: require.resolve('./sidebars.js'),
                },
                theme: {
                    customCss: [
                        require.resolve('./src/styles/custom.scss'),
                        require.resolve('./node_modules/modern-normalize/modern-normalize.css'),
                    ]
                },
            },
        ],
    ],

    themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            colorMode: {
                defaultMode: 'light',
                disableSwitch: true,
            },
            navbar: {
                hideOnScroll: true,
                logo: {
                    alt: 'Site Logo',
                    src: `/logos/marathon-text-docs-dark.svg`,
                    srcDark: `/logos/marathon-text-docs-light.svg`,
                    href: '/',
                    target: '_self',
                    width: 139,
                    height: 28,
                },
                items: [
                    {
                        label: 'Support',
                        position: 'right',
                        items: [
                            {
                                href: 'https://bit.ly/2LLghaW',
                                label: 'Slack',
                                target: '_blank',
                                rel: null,
                            },
                            {
                                href: 'https://t.me/marathontestrunner',
                                label: 'Telegram (RU)',
                                target: '_blank',
                                rel: null,
                            }
                        ],
                        className: 'navbar__link--support',
                    },
                    {
                        type: 'custom-separator',
                        position: 'right',
                    },
                    {
                        type: 'custom-iconLink',
                        position: 'right',
                        icon: {
                            alt: 'github logo',
                            src: `/logos/github.svg`,
                            href: 'https://github.com/MarathonLabs/marathon',
                            target: '_blank',
                        },
                    },
                ],
            },
            prism: {
                theme: lightCodeTheme,
                additionalLanguages: ["shell-session"]
            },
        }),
    themes: [
        "@docusaurus/theme-mermaid",
    ],
};

module.exports = config;
