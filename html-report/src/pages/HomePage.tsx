import { useCallback, useEffect, useMemo } from 'react';
import { AppShell } from '../components/AppShell';
import { TimelineChart } from '../components/TimelineChart';
import type { HtmlIndex, HtmlPoolSummary } from '../types/report';
import type { TimelineData } from '../types/timeline';
import { paths } from '../util/paths';
import { formatDuration, formatTimestamp } from '../util/time';

export function HomePage({ data }: { data: HtmlIndex }) {
  useEffect(() => {
    document.title = `${data.title} — Marathon`;
  }, [data.title]);

  const totals = useMemo(() => summarizeTotals(data), [data]);
  const timeline = typeof window !== 'undefined' ? window.timeline : undefined;

  // Timeline bar click → same test detail page the pool list links to.
  const onOpenTest = useCallback(
    (payload: { measure: string; data: TimelineData }) => {
      const { poolId, deviceSerial, testFilename, attemptIndex } = payload.data;
      if (!poolId || !deviceSerial || !testFilename) return;
      const href = `./pools/${encodeURIComponent(poolId)}/${encodeURIComponent(deviceSerial)}/${testFilename}`;
      const hash = typeof attemptIndex === 'number' ? `#/attempt/${attemptIndex}` : '';
      window.location.assign(href + hash);
    },
    [],
  );

  return (
    <AppShell
      title={data.title}
      breadcrumbs={<>Generated {formatTimestamp(data.generated_at_ms)}</>}
    >
      <section aria-label="Run totals" className="mb-6 grid grid-cols-2 gap-3 md:grid-cols-6">
        <StatCard label="Passed" value={data.total_passed} tone="passed" />
        <StatCard label="Failed" value={data.total_failed} tone="failed" />
        <StatCard label="Ignored" value={data.total_ignored} tone="ignored" />
        <StatCard label="Flaky" value={data.total_flaky} tone="flaky" />
        <StatCard label="Duration" value={formatDuration(data.total_duration_millis)} />
        <StatCard label="Devices" value={totals.distinctDevices} />
      </section>

      <section aria-label="Pools" className="mb-6">
        <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
          Pools
        </h2>
        <div className="grid grid-cols-1 gap-3 lg:grid-cols-2">
          {data.pools.map((pool) => (
            <PoolCard key={pool.id} pool={pool} />
          ))}
        </div>
      </section>

      <section aria-label="Execution timeline">
        <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
          Execution timeline
        </h2>
        {timeline ? (
          <TimelineChart
            data={timeline}
            onOpenTest={onOpenTest}
            className="rounded-lg border border-surface-border bg-surface-alt p-3"
          />
        ) : (
          <div className="rounded-lg border border-dashed border-surface-border p-6 text-sm text-slate-500 dark:text-slate-400">
            No timeline data available for this report.
          </div>
        )}
      </section>
    </AppShell>
  );
}

function summarizeTotals(data: HtmlIndex) {
  const serials = new Set<string>();
  for (const pool of data.pools) {
    for (const device of pool.devices) serials.add(device.serial);
  }
  return { distinctDevices: serials.size };
}

type StatTone = 'passed' | 'failed' | 'ignored' | 'flaky' | undefined;

function StatCard({
  label,
  value,
  tone,
}: {
  label: string;
  value: number | string;
  tone?: StatTone;
}) {
  const toneClass =
    tone === 'passed'
      ? 'text-status-passed'
      : tone === 'failed'
        ? 'text-status-failed'
        : tone === 'ignored'
          ? 'text-status-ignored'
          : tone === 'flaky'
            ? 'text-status-flaky'
            : '';
  return (
    <div className="rounded-lg border border-surface-border bg-surface-alt p-3">
      <div className="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">
        {label}
      </div>
      <div className={`mt-1 text-2xl font-semibold tabular-nums ${toneClass}`}>{value}</div>
    </div>
  );
}

function PoolCard({ pool }: { pool: HtmlPoolSummary }) {
  return (
    <a
      href={paths.fromIndexToPool(pool.id)}
      className="block rounded-lg border border-surface-border bg-surface-alt p-4 hover:border-slate-400 hover:no-underline dark:hover:border-slate-500"
    >
      <div className="mb-3 flex items-center justify-between gap-2">
        <div className="text-base font-semibold truncate">Pool {pool.id}</div>
        <div className="text-xs text-slate-500 dark:text-slate-400">
          {pool.devices.length} device{pool.devices.length === 1 ? '' : 's'}
        </div>
      </div>
      <div className="grid grid-cols-5 gap-2 text-sm">
        <MiniStat label="Pass" value={pool.passed_count} tone="passed" />
        <MiniStat label="Fail" value={pool.failed_count} tone="failed" />
        <MiniStat label="Ignore" value={pool.ignored_count} tone="ignored" />
        <MiniStat label="Flaky" value={pool.flaky_count} tone="flaky" />
        <MiniStat label="Duration" value={formatDuration(pool.duration_millis)} />
      </div>
    </a>
  );
}

function MiniStat({ label, value, tone }: { label: string; value: number | string; tone?: StatTone }) {
  const toneClass =
    tone === 'passed'
      ? 'text-status-passed'
      : tone === 'failed'
        ? 'text-status-failed'
        : tone === 'ignored'
          ? 'text-status-ignored'
          : tone === 'flaky'
            ? 'text-status-flaky'
            : '';
  return (
    <div>
      <div className="text-[10px] uppercase tracking-wide text-slate-500 dark:text-slate-400">
        {label}
      </div>
      <div className={`text-base font-semibold tabular-nums ${toneClass}`}>{value}</div>
    </div>
  );
}
