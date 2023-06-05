// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');
const math = require('remark-math');
const katex = require('rehype-katex');
const path = require("path");

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'Marathon docs',
    tagline: 'Cross-platform test runner written for Android and iOS projects',
    url: 'https://docs.marathonlabs.io/',
    baseUrl: '/',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',
    favicon: 'img/favicon.svg',

    i18n: {
        defaultLocale: 'en',
        locales: ['en', 'zh'],
        localeConfigs: {
            en: {
                htmlLang: 'en-US',
            },
            zh: {
                htmlLang: 'zh_CN',
            },
        },
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
                    editCurrentVersion: false,
                    remarkPlugins: [math],
                    rehypePlugins: [katex],
                    breadcrumbs: false,
                    versions: {
                        current: {
                            label: 'develop',
                            path: 'next',
                            banner: 'none',
                        },
                    }
                },
                theme: {
                    customCss: [
                        require.resolve('./src/styles/custom.scss'),
                        require.resolve('./src/styles/colors.css'),
                    ]
                },
                gtag: {
                    trackingID: 'G-7RE7PPY2QW',
                    anonymizeIP: false,
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
                    width: 113,
                },
                items: [
                    {
                        type: 'docsVersionDropdown',
                        position: 'right',
                        dropdownActiveClassDisabled: true,
                        className: 'navbar__dropdown--versions',
                    },
                    {
                        type: 'localeDropdown',
                        position: 'right',
                    },
                    {
                        type: 'search',
                        position: 'right',
                    },
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
                                label: 'Telegram',
                                target: '_blank',
                                rel: null,
                            },
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
                additionalLanguages: ["shell-session", "kotlin", "groovy"]
            },
            algolia: {
                appId: '5FSD01C36S',
                apiKey: '9a3b61683f7ae8215249da35b66fc74b',
                indexName: 'malinskiy_marathon',
                contextualSearch: true,
                searchPagePath: false,
            },
        }),
    themes: [
        "@docusaurus/theme-mermaid",
    ],
    stylesheets: [
        {
            href: 'https://cdn.jsdelivr.net/npm/katex@0.13.24/dist/katex.min.css',
            type: 'text/css',
            integrity:
                'sha384-odtC+0UGzzFL/6PNoE8rX/SPcQDXBJ+uRepguP4QkPCm2LBxH3FA3y+fKSiJ+AmM',
            crossorigin: 'anonymous',
        },
    ],
};

module.exports = config;
