// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'YMATE-APIDocs',
    tagline: 'A simple document generation tool.',
    url: 'https://ymate.net',
    baseUrl: '/',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',
    favicon: 'img/favicon.ico',
    organizationName: '', // Usually your GitHub org/user name.
    projectName: '', // Usually your repo name.

    presets: [
        [
            '@docusaurus/preset-classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    sidebarPath: require.resolve('./sidebars.js'),
                    routeBasePath: '/',
                },
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }),
        ],
    ],

    themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            docs: {
                sidebar: {
                    hideable: true,
                },
            },
            navbar: {
                title: 'YMATE-APIDocs',
                logo: {
                    alt: 'YMP Logo',
                    src: 'img/logo.png',
                },
                items: [
                    {
                        type: 'doc',
                        docId: 'intro',
                        position: 'right',
                        label: '首页',
                    },
                    {
                        to: 'https://ymate.net/quickstart',
                        label: '快速上手',
                        position: 'right'
                    },
                    {
                        to: 'https://ymate.net/guide',
                        label: '开发指南',
                        position: 'right',
                    },
                    {
                        to: 'https://ymate.net/modules',
                        label: '模块',
                        position: 'right',
                    },
                    {to: 'https://ymate.net/blog', label: '博客', position: 'right'},
                    {
                        to: 'https://ymate.net/support',
                        label: '支持 & 捐赠',
                        position: 'right'
                    },
                    {
                        type: 'dropdown',
                        label: '下载源码',
                        position: 'right',
                        items: [
                            {
                                href: 'https://github.com/suninformation/ymate-platform-v2',
                                label: 'GitHub',
                            },
                            {
                                href: 'https://gitee.com/suninformation/ymate-platform-v2',
                                label: 'Gitee',
                            },
                        ],
                    },
                ],
            },
            footer: {
                style: 'dark',
                links: [
                    {
                        title: '文档',
                        items: [
                            {
                                label: '快速上手',
                                to: 'https://ymate.net/quickstart',
                            },
                            {
                                label: '开发指南',
                                to: 'https://ymate.net/guide',
                            },
                            {
                                label: '模块',
                                to: 'https://ymate.net/modules',
                            },
                        ],
                    },
                    {
                        title: '社群',
                        items: [
                            {
                                label: 'Github Issues',
                                href: 'https://github.com/suninformation/ymate-platform-v2/issues',
                            },
                            {
                                label: 'Gitee Issues',
                                href: 'https://gitee.com/suninformation/ymate-platform-v2/issues',
                            },
                            {
                                label: 'QQ群：480374360',
                                href: 'https://qm.qq.com/cgi-bin/qm/qr?k=3KSXbRoridGeFxTVA8HZzyhwU_btZQJ2',
                            },
                        ],
                    },
                    {
                        title: '更多',
                        items: [
                            {
                                label: '博客',
                                to: 'https://ymate.net/blog',
                            },
                            {
                                label: '支持 & 捐赠',
                                to: 'https://ymate.net/support',
                            },
                            {
                                label: 'GitHub',
                                href: 'https://github.com/suninformation/ymate-platform-v2',
                            },
                            {
                                label: 'Gitee',
                                href: 'https://gitee.com/suninformation/ymate-platform-v2',
                            },
                        ],
                    },
                ],
                copyright: `Copyright © ${new Date().getFullYear()} <a href="https://ymate.net" target="_blank">yMate.Net</a>. All Rights Reserved. Built with Docusaurus.<br/>Apache License Version 2.0`,
            },
            prism: {
                theme: lightCodeTheme,
                darkTheme: darkCodeTheme,
                additionalLanguages: ['java', 'properties'],
            },
        }),
};

module.exports = config;
