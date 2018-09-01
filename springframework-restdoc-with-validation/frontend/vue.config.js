// https://cli.vuejs.org/config/#vue-config-js

const path = require('path')
const webpack = require('webpack')

module.exports = {
    outputDir: path.resolve(__dirname, '../out/production/resources/templates'),
    
    devServer: {
        port: 3000,
        proxy: 'http://localhost:8080'
    },
    configureWebpack: {
        plugins: [
            new webpack.ProvidePlugin({
                $: 'jquery',
                jquery: 'jquery',
                jQuery: 'jquery',
                'window.jQuery': 'jquery',
                '_' : 'lodash'
            })
        ]
    },
    chainWebpack: config => {
        config.module.rule('md')
            .test(/\.md/)
            .use('vue-loader')
            .loader('vue-loader')
            .end()
            .use('vue-markdown-loader')
            .loader('vue-markdown-loader/lib/markdown-compiler')
            .options({
                raw: true
            })
    }
}