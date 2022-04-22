const path = require('path');
 
const outPath = path.join(__dirname, "..", "..", "resources", "static", "js");
 
module.exports = {
  entry: './js/index.jsx',
  output: {
    path: outPath,
    filename: 'bundle.js'
  },
  module: {
    rules: [
      {
        test: [/\.js$/, /.jsx$/],
        exclude: /node_modules/,
        loader: 'babel-loader'
      }
    ]
  },
  devtool: "eval-source-map"
};
