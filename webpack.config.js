const ExtractTextPlugin = require('extract-text-webpack-plugin');

const extractSASS = new ExtractTextPlugin('../css/style.css');

module.exports = {
    entry: './resources/js/main.js',
    output: {
        path: `${__dirname}/public/assets/js`,
        filename: "script.js"
    },
    plugins: [
        extractSASS
    ],
    module: {
        rules: [{
            test: /\.scss$/,
            use: extractSASS.extract([
                "css-loader", 
                "sass-loader" 
            ])
        }]
    }
}
