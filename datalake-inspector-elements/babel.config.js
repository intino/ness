module.exports = function (api) {
  api.cache(true);

  const presets = [ "@babel/preset-env", "@babel/preset-react" ];
  const plugins = [ "@babel/plugin-syntax-dynamic-import", "dynamic-import-webpack", "babel-plugin-styled-components", "@babel/plugin-proposal-class-properties" ];

  return {
    presets,
    plugins
  };
};