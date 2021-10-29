module.exports = {
    fromIndexToPool: (poolId) => `./pools/${poolId}.html`,
    fromPoolToIndex: '../index.html',
    fromTestToPool: (poolId) => `../../${poolId}.html`,
    fromTestToIndex: '../../../index.html',
    fromLogsToIndex: '../../../../index.html',
    fromTestToLogs: (filename) => `./logs/${filename}`,
    fromLogsToTest: (testId) => `../${testId}.html`,
    fromLogsToPool: (poolId) => `../../../${poolId}.html`
};
