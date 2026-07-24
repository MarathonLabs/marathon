import type { HtmlPoolSummary, HtmlShortTest, Status } from '../../types/report';

export type SortKey =
  | 'name'
  | 'duration_desc'
  | 'duration_asc'
  | 'start_time_asc'
  | 'start_time_desc'
  | 'status'
  | 'attempts';

export const SORT_OPTIONS: Array<{ value: SortKey; label: string }> = [
  { value: 'name', label: 'Name' },
  { value: 'duration_desc', label: 'Duration (longest first)' },
  { value: 'duration_asc', label: 'Duration (shortest first)' },
  { value: 'start_time_asc', label: 'Started (oldest first)' },
  { value: 'start_time_desc', label: 'Started (newest first)' },
  { value: 'status', label: 'Status' },
  { value: 'attempts', label: 'Attempts (most first)' },
];

export type GroupKey = 'none' | 'device' | 'os_major' | 'class' | 'status' | 'batch';

export const GROUP_OPTIONS: Array<{ value: GroupKey; label: string }> = [
  { value: 'none', label: 'No grouping' },
  { value: 'device', label: 'Device' },
  { value: 'os_major', label: 'OS major' },
  { value: 'class', label: 'Class' },
  { value: 'status', label: 'Status' },
  { value: 'batch', label: 'Batch' },
];

export interface PoolFilters {
  q: string;
  useRegex: boolean;
  statuses: Set<Status>;
  devices: Set<string>;
  osMajors: Set<string>;
  manufacturers: Set<string>;
  onlyRetried: boolean;
  onlyFlaky: boolean;
  requireScreenshot: boolean;
  requireVideo: boolean;
  requireLog: boolean;
  minDurationMs: number | null;
  maxDurationMs: number | null;
  sort: SortKey;
  group: GroupKey;
}

export function readFilters(params: URLSearchParams): PoolFilters {
  const toSet = (v: string | null) => new Set(v?.split(',').filter(Boolean) ?? []);
  return {
    q: params.get('q') ?? '',
    useRegex: params.get('re') === '1',
    statuses: toSet(params.get('status')) as Set<Status>,
    devices: toSet(params.get('device')),
    osMajors: toSet(params.get('os')),
    manufacturers: toSet(params.get('mfr')),
    onlyRetried: params.get('retried') === '1',
    onlyFlaky: params.get('flaky') === '1',
    requireScreenshot: params.get('screen') === '1',
    requireVideo: params.get('video') === '1',
    requireLog: params.get('log') === '1',
    minDurationMs: numOrNull(params.get('minMs')),
    maxDurationMs: numOrNull(params.get('maxMs')),
    sort: (params.get('sort') as SortKey) || 'name',
    group: (params.get('group') as GroupKey) || 'none',
  };
}

export function applyFilters(pool: HtmlPoolSummary, filters: PoolFilters): HtmlShortTest[] {
  const rx = filters.useRegex && filters.q ? safeRegex(filters.q) : null;
  const lowerQ = filters.q.toLowerCase();
  return pool.tests.filter((t) => {
    if (filters.statuses.size && !filters.statuses.has(t.status)) return false;
    if (filters.onlyRetried && t.attempt_count < 2) return false;
    if (filters.onlyFlaky && !t.is_flaky) return false;
    if (filters.minDurationMs !== null && t.duration_millis < filters.minDurationMs) return false;
    if (filters.maxDurationMs !== null && t.duration_millis > filters.maxDurationMs) return false;

    if (filters.devices.size && !t.devices.some((d) => filters.devices.has(d))) return false;
    if (filters.osMajors.size) {
      const hits = t.os_versions.some((v) => {
        const major = v.split('.')[0];
        return major !== undefined && filters.osMajors.has(major);
      });
      if (!hits) return false;
    }
    if (filters.manufacturers.size && !filters.manufacturers.has(t.device.manufacturer)) return false;

    if (filters.q) {
      const hay = `${t.package_name}.${t.class_name}.${t.name}`;
      if (rx) {
        if (!rx.test(hay)) return false;
      } else if (!hay.toLowerCase().includes(lowerQ)) {
        return false;
      }
    }

    if (filters.requireScreenshot && !t.device.features.includes('SCREENSHOT')) return false;
    if (filters.requireVideo && !t.device.features.includes('VIDEO')) return false;
    if (filters.requireLog && !t.device.features.length) return false;

    return true;
  });
}

export function sortTests(tests: HtmlShortTest[], sort: SortKey): HtmlShortTest[] {
  const cmpName = (a: HtmlShortTest, b: HtmlShortTest) =>
    `${a.class_name}.${a.name}`.localeCompare(`${b.class_name}.${b.name}`);
  const copy = [...tests];
  switch (sort) {
    case 'duration_desc':
      return copy.sort((a, b) => b.duration_millis - a.duration_millis);
    case 'duration_asc':
      return copy.sort((a, b) => a.duration_millis - b.duration_millis);
    case 'start_time_asc':
      return copy.sort((a, b) => a.start_time_ms - b.start_time_ms);
    case 'start_time_desc':
      return copy.sort((a, b) => b.start_time_ms - a.start_time_ms);
    case 'status':
      return copy.sort((a, b) => a.status.localeCompare(b.status) || cmpName(a, b));
    case 'attempts':
      return copy.sort((a, b) => b.attempt_count - a.attempt_count || cmpName(a, b));
    case 'name':
    default:
      return copy.sort(cmpName);
  }
}

export function groupTests(tests: HtmlShortTest[], group: GroupKey): Array<{ label: string; items: HtmlShortTest[] }> {
  if (group === 'none') return [{ label: '', items: tests }];
  const buckets = new Map<string, HtmlShortTest[]>();
  for (const test of tests) {
    const key = bucketKey(test, group);
    const bucket = buckets.get(key) ?? [];
    bucket.push(test);
    buckets.set(key, bucket);
  }
  return [...buckets.entries()]
    .sort(([a], [b]) => a.localeCompare(b))
    .map(([label, items]) => ({ label, items }));
}

function bucketKey(test: HtmlShortTest, group: GroupKey): string {
  switch (group) {
    case 'device':
      return test.device.serial;
    case 'os_major':
      return test.device.os_major !== null ? `api ${test.device.os_major}` : test.device.os_version;
    case 'class':
      return `${test.package_name}.${test.class_name}`;
    case 'status':
      return test.status;
    case 'batch':
      return test.batch_id;
    case 'none':
    default:
      return '';
  }
}

function numOrNull(raw: string | null): number | null {
  if (raw === null || raw === '') return null;
  const n = Number(raw);
  return Number.isFinite(n) ? n : null;
}

function safeRegex(pattern: string): RegExp | null {
  try {
    return new RegExp(pattern, 'i');
  } catch {
    return null;
  }
}

/**
 * Encode a `Set<string>` filter into the hash query. Empty sets clear the key
 * (keeps the hash string short + shareable).
 */
export function serializeSet(params: URLSearchParams, key: string, values: Set<string>) {
  if (values.size === 0) params.delete(key);
  else params.set(key, [...values].join(','));
}
