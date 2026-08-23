// On-disk layout emitted by `HtmlSummaryReporter`:
//   html/index.html
//   html/pools/<pool>.html
//   html/pools/<pool>/<device>/<test>.html
//   html/pools/<pool>/<device>/logs/<test>.html
//
// Cross-page navigation uses these relative `href`s (works under file://);
// intra-page state lives in the URL hash.

export const paths = {
  fromIndexToPool: (poolId: string) => `./pools/${encodeURIComponent(poolId)}.html`,
  fromPoolToIndex: '../index.html',
  // Pool HTML lives at `pools/<pool>.html`; test HTML at
  // `pools/<pool>/<device>/<test>.html`. Relative href from pool → test is
  // `<pool>/<device>/<test>.html`.
  fromPoolToTest: (poolId: string, deviceId: string, filename: string) =>
    `${encodeURIComponent(poolId)}/${encodeURIComponent(deviceId)}/${filename}`,
  fromTestToIndex: '../../../index.html',
  fromTestToPool: (poolId: string) => `../../${encodeURIComponent(poolId)}.html`,
  fromTestToLogs: (filename: string) => `./logs/${filename}`,
  fromLogsToIndex: '../../../../index.html',
  fromLogsToPool: (poolId: string) => `../../../${encodeURIComponent(poolId)}.html`,
  fromLogsToTest: (filename: string) => `../${filename}`,
} as const;
