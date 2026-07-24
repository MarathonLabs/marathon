import { useEffect, useMemo, useRef } from 'react';
import { useWindowVirtualizer } from '@tanstack/react-virtual';
import clsx from 'clsx';
import { AppShell } from '../../components/AppShell';
import { DeviceChip, osLabel } from '../../components/DeviceChip';
import { FlakyBadge, StatusBadge, toneClasses, type StatusTone } from '../../components/StatusBadge';
import { useHashState } from '../../hooks/useHashState';
import { useScrollMargin } from '../../hooks/useScrollMargin';
import type { HtmlPoolSummary, HtmlShortTest, Status } from '../../types/report';
import { paths } from '../../util/paths';
import { formatDuration } from '../../util/time';
import {
  applyFilters,
  GROUP_OPTIONS,
  groupTests,
  readFilters,
  serializeSet,
  SORT_OPTIONS,
  sortTests,
  type GroupKey,
  type PoolFilters,
  type SortKey,
} from './filters';

const STATUS_VALUES: Status[] = ['passed', 'failed', 'ignored'];

export function PoolPage({ data }: { data: HtmlPoolSummary }) {
  const [params, update] = useHashState();
  const filters = useMemo(() => readFilters(params), [params]);

  useEffect(() => {
    document.title = `Pool ${data.id} — Marathon`;
  }, [data.id]);

  const filtered = useMemo(() => applyFilters(data, filters), [data, filters]);
  const sorted = useMemo(() => sortTests(filtered, filters.sort), [filtered, filters.sort]);
  const grouped = useMemo(() => groupTests(sorted, filters.group), [sorted, filters.group]);

  const availableManufacturers = useMemo(
    () => uniqueSorted(data.tests.map((t) => t.device.manufacturer)),
    [data.tests],
  );
  const availableOsMajors = useMemo(
    () =>
      uniqueSorted(
        data.tests.flatMap((t) => t.os_versions.map((v) => v.split('.')[0] ?? v)),
      ),
    [data.tests],
  );

  const breadcrumbs = (
    <>
      <a href={paths.fromPoolToIndex}>Pools list</a> / Pool {data.id}
    </>
  );

  return (
    <AppShell
      title={`Pool ${data.id}`}
      breadcrumbs={breadcrumbs}
      actions={
        <span className="text-xs text-slate-500 dark:text-slate-400">
          {sorted.length} / {data.tests.length}
        </span>
      }
    >
      <FilterBar
        filters={filters}
        update={update}
        pool={data}
        manufacturers={availableManufacturers}
        osMajors={availableOsMajors}
      />

      <VirtualTestList groups={grouped} poolId={data.id} />
    </AppShell>
  );
}

function FilterBar({
  filters,
  update,
  pool,
  manufacturers,
  osMajors,
}: {
  filters: PoolFilters;
  update: (mutate: (params: URLSearchParams) => void) => void;
  pool: HtmlPoolSummary;
  manufacturers: string[];
  osMajors: string[];
}) {
  const toggleSet = (key: string, values: Set<string>, value: string) =>
    update((p) => {
      const next = new Set(values);
      if (next.has(value)) next.delete(value);
      else next.add(value);
      serializeSet(p, key, next);
    });

  const toggleBool = (key: string, current: boolean) =>
    update((p) => {
      if (current) p.delete(key);
      else p.set(key, '1');
    });

  return (
    <div className="mb-4 flex flex-col gap-3 rounded-lg border border-surface-border bg-surface-alt p-3">
      <div className="flex flex-wrap items-center gap-2">
        <SearchInput
          value={filters.q}
          onChange={(next) => update((p) => (next ? p.set('q', next) : p.delete('q')))}
          useRegex={filters.useRegex}
          onToggleRegex={() => toggleBool('re', filters.useRegex)}
        />
        <div className="flex-1" />
        <label className="flex items-center gap-1 text-xs">
          <span className="text-slate-500 dark:text-slate-400">Sort</span>
          <select
            value={filters.sort}
            onChange={(e) => update((p) => p.set('sort', e.target.value as SortKey))}
            className="rounded border border-surface-border bg-surface px-1 py-0.5"
          >
            {SORT_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
        </label>
        <label className="flex items-center gap-1 text-xs">
          <span className="text-slate-500 dark:text-slate-400">Group</span>
          <select
            value={filters.group}
            onChange={(e) => update((p) => p.set('group', e.target.value as GroupKey))}
            className="rounded border border-surface-border bg-surface px-1 py-0.5"
          >
            {GROUP_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="flex flex-wrap gap-2 text-xs">
        {STATUS_VALUES.map((s) => (
          <Chip
            key={s}
            active={filters.statuses.has(s)}
            tone={s}
            onClick={() => toggleSet('status', filters.statuses, s)}
          >
            <span className="font-medium uppercase tracking-wide">{s}</span>
            <span className="ml-1.5 tabular-nums opacity-80">{countByStatus(pool, s)}</span>
          </Chip>
        ))}
        <Chip
          active={filters.onlyFlaky}
          tone="flaky"
          onClick={() => toggleBool('flaky', filters.onlyFlaky)}
        >
          <span className="font-medium uppercase tracking-wide">flaky</span>
          <span className="ml-1.5 tabular-nums opacity-80">{pool.flaky_count}</span>
        </Chip>
        <Chip active={filters.onlyRetried} onClick={() => toggleBool('retried', filters.onlyRetried)}>
          retried
        </Chip>
        <Chip
          active={filters.requireScreenshot}
          onClick={() => toggleBool('screen', filters.requireScreenshot)}
        >
          screenshot
        </Chip>
        <Chip active={filters.requireVideo} onClick={() => toggleBool('video', filters.requireVideo)}>
          video
        </Chip>
      </div>

      <FilterCategory
        label="Device"
        values={pool.devices.map((d) => d.serial)}
        active={filters.devices}
        onToggle={(v) => toggleSet('device', filters.devices, v)}
      />
      <FilterCategory
        label="OS major"
        values={osMajors}
        active={filters.osMajors}
        onToggle={(v) => toggleSet('os', filters.osMajors, v)}
      />
      <FilterCategory
        label="Manufacturer"
        values={manufacturers}
        active={filters.manufacturers}
        onToggle={(v) => toggleSet('mfr', filters.manufacturers, v)}
      />
    </div>
  );
}

function countByStatus(pool: HtmlPoolSummary, status: Status): number {
  if (status === 'passed') return pool.passed_count;
  if (status === 'failed') return pool.failed_count;
  return pool.ignored_count;
}

function FilterCategory({
  label,
  values,
  active,
  onToggle,
}: {
  label: string;
  values: string[];
  active: Set<string>;
  onToggle: (value: string) => void;
}) {
  if (values.length === 0) return null;
  return (
    <div className="flex flex-wrap items-center gap-1.5 text-xs">
      <span className="mr-1 text-slate-500 dark:text-slate-400">{label}:</span>
      {values.map((v) => (
        <Chip key={v} active={active.has(v)} onClick={() => onToggle(v)}>
          {v}
        </Chip>
      ))}
    </div>
  );
}

function Chip({
  active,
  onClick,
  tone,
  children,
}: {
  active: boolean;
  onClick: () => void;
  /**
   * When set, the chip fills with the tone's tint at rest and darkens when
   * active — used for status/flaky filters so users can spot the category
   * without reading the label. Omit for neutral chips (device serials, tags,
   * `retried`, etc.) that just toggle on/off with a sky highlight.
   */
  tone?: StatusTone;
  children: React.ReactNode;
}) {
  const tonedRest = tone ? toneClasses(tone, false) : '';
  const tonedActive = tone ? toneClasses(tone, true) : '';
  return (
    <button
      type="button"
      onClick={onClick}
      className={clsx(
        'inline-flex items-center rounded-full border px-2 py-0.5 text-xs transition',
        tone
          ? active
            ? tonedActive
            : `${tonedRest} hover:brightness-110`
          : active
            ? 'border-sky-500 bg-sky-500/10 text-sky-700 dark:text-sky-300'
            : 'border-surface-border bg-surface hover:border-slate-400 dark:hover:border-slate-500',
      )}
    >
      {children}
    </button>
  );
}

function SearchInput({
  value,
  onChange,
  useRegex,
  onToggleRegex,
}: {
  value: string;
  onChange: (next: string) => void;
  useRegex: boolean;
  onToggleRegex: () => void;
}) {
  const ref = useRef<HTMLInputElement>(null);
  useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if (e.key === '/' && document.activeElement?.tagName !== 'INPUT') {
        e.preventDefault();
        ref.current?.focus();
        ref.current?.select();
      }
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);
  return (
    <div className="flex items-center gap-1">
      <input
        ref={ref}
        value={value}
        placeholder="Search (/)"
        onChange={(e) => onChange(e.target.value)}
        className="w-64 rounded border border-surface-border bg-surface px-2 py-1 text-sm"
      />
      <button
        type="button"
        onClick={onToggleRegex}
        className={clsx(
          'rounded border px-1.5 py-1 text-xs',
          useRegex
            ? 'border-sky-500 bg-sky-500/10 text-sky-700 dark:text-sky-300'
            : 'border-surface-border bg-surface',
        )}
        title="Toggle regex match"
      >
        .*
      </button>
    </div>
  );
}

/**
 * Flattens {group header, row} entries into a single virtualized list. Groups
 * with `label === ''` (the "no grouping" case) skip the header entry.
 */
type Row =
  | { kind: 'header'; label: string; count: number }
  | { kind: 'test'; test: HtmlShortTest };

function VirtualTestList({
  groups,
  poolId,
}: {
  groups: Array<{ label: string; items: HtmlShortTest[] }>;
  poolId: string;
}) {
  const rows: Row[] = useMemo(() => {
    const out: Row[] = [];
    for (const group of groups) {
      if (group.label) out.push({ kind: 'header', label: group.label, count: group.items.length });
      for (const test of group.items) out.push({ kind: 'test', test });
    }
    return out;
  }, [groups]);

  const parentRef = useRef<HTMLDivElement>(null);
  const scrollMargin = useScrollMargin(parentRef);
  // Window-scrolling virtualizer: the browser scrolls the whole document
  // rather than nesting an inner scroll region. Keeps the footer and page
  // chrome on a single scroll axis and drops the "scroll twice" feel.
  // `scrollMargin` offsets by the list's distance from the top of the
  // document so overscan lines up with the on-screen viewport.
  const virtualizer = useWindowVirtualizer({
    count: rows.length,
    estimateSize: (index) => (rows[index]?.kind === 'header' ? 32 : 68),
    overscan: 12,
    scrollMargin,
  });

  if (rows.length === 0) {
    return (
      <div className="rounded-lg border border-dashed border-surface-border p-8 text-center text-sm text-slate-500 dark:text-slate-400">
        No tests match the current filters.
      </div>
    );
  }

  return (
    <div
      ref={parentRef}
      className="rounded-lg border border-surface-border bg-surface"
    >
      <div style={{ height: virtualizer.getTotalSize(), position: 'relative' }}>
        {virtualizer.getVirtualItems().map((v) => {
          const row = rows[v.index];
          if (!row) return null;
          // Window virtualizer emits `v.start` in document coordinates; back
          // out the container's own scrollMargin to place items relative to
          // the sizer div.
          const commonStyle = { transform: `translateY(${v.start - scrollMargin}px)` } as const;
          if (row.kind === 'header') {
            return (
              <div
                key={v.key}
                data-index={v.index}
                ref={virtualizer.measureElement}
                className="absolute inset-x-0 top-0 border-b border-surface-border bg-surface-alt px-3 py-1 text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400"
                style={commonStyle}
              >
                {row.label} <span className="ml-1 text-slate-400">({row.count})</span>
              </div>
            );
          }
          return (
            <TestRow
              key={v.key}
              index={v.index}
              measureRef={virtualizer.measureElement}
              style={commonStyle}
              test={row.test}
              poolId={poolId}
            />
          );
        })}
      </div>
    </div>
  );
}

function TestRow({
  index,
  measureRef,
  style,
  test,
  poolId,
}: {
  index: number;
  measureRef: (el: HTMLElement | null) => void;
  style: React.CSSProperties;
  test: HtmlShortTest;
  poolId: string;
}) {
  return (
    <a
      data-index={index}
      ref={measureRef}
      href={paths.fromPoolToTest(poolId, test.device_id, test.filename)}
      className="absolute inset-x-0 top-0 flex items-center gap-3 border-b border-surface-border px-3 py-2 hover:bg-surface-alt hover:no-underline"
      style={style}
    >
      <StatusBadge status={test.status} />
      <div className="min-w-0 flex-1">
        <div className="truncate text-sm font-medium">{test.name}</div>
        <div className="truncate text-xs text-slate-500 dark:text-slate-400">
          {test.class_name}
          <span className="mx-1 opacity-60">·</span>
          {test.package_name}
        </div>
      </div>
      <div className="flex flex-wrap items-center gap-1.5">
        <DeviceChip device={test.device} compact />
        <span className="rounded border border-surface-border bg-surface-alt px-1.5 py-0.5 text-[10px] uppercase tracking-wide">
          {osLabel(test.device)}
        </span>
        {test.is_flaky && <FlakyBadge />}
        {test.attempt_count > 1 && (
          <span className="rounded border border-surface-border bg-surface-alt px-1.5 py-0.5 text-[10px]">
            {test.attempt_count} attempts
          </span>
        )}
        <span className="rounded border border-surface-border bg-surface-alt px-1.5 py-0.5 text-xs tabular-nums">
          {formatDuration(test.duration_millis)}
        </span>
      </div>
    </a>
  );
}

function uniqueSorted(values: string[]): string[] {
  return [...new Set(values.filter(Boolean))].sort();
}
