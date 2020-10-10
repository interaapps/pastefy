const { VueLoaderPlugin } = require('vue-loader')
const MiniCssExtractPlugin = require('mini-css-extract-plugin')
const webpack = require('webpack');

const options = {
    appJs: './resources/js/index.js',
    outputPath: `${__dirname}/public/assets/`,
    outputNames: {
        js: "js/app.compiled.js",
        css: 'css/app.compiled.css',
        cssChunked: 'css/[id].[hash].compiled.css',
    },

    isDevelopment: false
}

module.exports = {
    entry: options.appJs,
    output: {
        path: options.outputPath,
        filename: options.outputNames.js,
        publicPath: "/assets/"
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: options.isDevelopment ? '[name].css' : options.outputNames.css,
            chunkFilename: options.isDevelopment ? '[id].css' : options.outputNames.cssChunked,
        }),
        new VueLoaderPlugin(),
        new webpack.optimize.LimitChunkCountPlugin({
            maxChunks: 1,
        }),
    ],
    resolve: {
        alias: {
          'vue$': 'vue/dist/vue.esm.js'
        }
    },
    module: {
        rules: [
            {
                test: /\.vue$/,
                use: 'vue-loader'
            },
            {
                test: /\.module\.s(a|c)ss$/,
                loader: [
                    options.isDevelopment ? 'style-loader' : MiniCssExtractPlugin.loader,
                    {
                        loader: 'css-loader',
                        options: {
                            modules: true,
                            sourceMap: options.isDevelopment
                        }
                    },
                    {
                        loader: 'sass-loader',
                        options: {
                            sourceMap: options.isDevelopment
                        }
                    }
                ]
            },
            {
                test: /\.s(a|c)ss$/,
                exclude: /\.module.(s(a|c)ss)$/,
                loader: [
                    options.isDevelopment ? 'style-loader' : MiniCssExtractPlugin.loader,
                    'css-loader',
                    {
                        loader: 'sass-loader',
                        options: {
                            sourceMap: options.isDevelopment
                        }
                    }
                ]
            },
            {
                test: /\.m?js$/,
                use: {
                  loader: 'babel-loader',
                  options: {
                    presets: ['@babel/preset-env']
                  }
                }
              }
        ]
    }
}