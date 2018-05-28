module.exports = {
  fromIndexToSuite: (suiteId) => `./suites/${suiteId}.html`,
  fromSuiteToIndex: '../index.html',
  fromTestToSuite: (suiteId) => `../../${suiteId}.html`,
  fromTestToIndex: '../../../index.html',
};
