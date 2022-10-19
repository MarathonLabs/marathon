const path = require('path');
var CopyWebpackPlugin = require('copy-webpack-plugin');


module.exports = function() {
  return {
    mode: 'production',
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
    optimization: {
      minimize: true,
    },
    plugins: [
      new CopyWebpackPlugin({
        patterns: [
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
      ]}),
    ]
  };
}
