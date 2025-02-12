const path = require('path');

const pkg = require('../package.json');

module.exports = {
  dependencies: {
    [pkg.name]: {
      platforms: {
        android: {},
        // Codegen script incorrectly fails without this
        // So we explicitly specify the platforms with empty object
        ios: {},
      },
      root: path.join(__dirname, '..'),
    },
  },
  project: {
    ios: {
      automaticPodsInstallation: true,
    },
  },
};
