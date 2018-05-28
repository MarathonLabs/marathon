const UglifyJSPlugin = require('uglifyjs-webpack-plugin');
const path = require('path');
var CopyWebpackPlugin = require('copy-webpack-plugin');


module.exports = function() {
  return {
    entry: './src/index.js',
    output: {
      path: path.join(__dirname, 'build'),
      filename: 'app.min.js'
    },
    resolve: {
      extensions: ['.js', '.json']
    },
    module: {
      rules: [
        {
          test: /\.js$/,
          loader: 'babel-loader',
          exclude: /node_modules/
        }
      ]
    },
    plugins: [
      new UglifyJSPlugin(),
      new CopyWebpackPlugin([
        {
          from: 'layout/index.html',
          to: './'
        },
        {
          from: 'layout/log-container.html',
          to: './'
        },
        {
          from: 'layout/log-entry.html',
          to: './'
        }
      ])
    ]
  };
}
