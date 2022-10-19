const path = require('path');

module.exports = function() {
  return {
    mode: 'development',
    entry: './src/index.js',
    output: {
      path: path.join(__dirname, 'build'),
      filename: 'app.js'
    },
    resolve: {
      extensions: ['.js', '.json']
    },
    module: {
      rules: [
        { test: /\.js$/, loader: 'babel-loader', exclude: /node_modules/ }
      ]
    },
    optimization: {
      minimize: false,
    },
  };
}
