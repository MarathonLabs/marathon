module.exports = {
  parser: 'postcss-scss',
  plugins: [
    require('postcss-import')({ path: ['src'] }),
    require('postcss-sass-colors'),
    require('postcss-mixins'),
    require('precss'),
    require('postcss-inline-svg'),
    require('autoprefixer')({
      browsers: [
        '> 5%',
        'last 2 versions',
        'IE 11'
      ]
    }),
    require('cssnano')({
      preset: ['default', {
        discardComments: {
          removeAll: true,
        },
      }]
    })
  ]
}