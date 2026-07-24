import { useEffect, useMemo, useRef, useState } from 'react';
import { useWindowVirtualizer } from '@tanstack/react-virtual';
import clsx from 'clsx';
import { AppShell } from '../../components/AppShell';
import { DeviceChip } from '../../components/DeviceChip';
import { toneClasses } from '../../components/StatusBadge';
import { useHashState } from '../../hooks/useHashState';
import { useScrollMargin } from '../../hooks/useScrollMargin';
import { detectParser, findParser, PARSERS } from '../../logs/parsers';
import type { LogEntry, LogParser } from '../../logs/types';
import type { HtmlLogAttempt, HtmlTestLogDetails } from '../../types/report';
import { paths } from '../../util/paths';
import { formatTimestamp } from '../../util/time';

const LEVEL_ORDER = ['V', 'D', 'I', 'W', 'E', 'A'];
const LEVEL_CLASS: Record<string, string> = {
  V: 'text-slate-400 dark:text-slate-500',
  D: 'text-slate-600 dark:text-slate-300',
  I: 'text-slate-800 dark:text-slate-100',
  W: 'text-status-ignored',
  E: 'text-status-failed',
  A: 'text-status-failed font-bold',
};

/**
 * Tone the level filter chip in the same palette as the row it filters —
 * warn maps to the ignored/yellow token, error and assert to the failed/red
 * token. Verbose/debug/info stay neutral because their rows do too.
 */
const LEVEL_CHIP_REST: Record<string, string> = {
  V: 'border-surface-border bg-surface text-slate-500 dark:text-slate-400',
  D: 'border-surface-border bg-surface text-slate-500 dark:text-slate-400',
  I: 'border-surface-border bg-surface',
  W: 'border-status-ignored/40 bg-status-ignored/10 text-status-ignored',
  E: 'border-status-failed/40 bg-status-failed/10 text-status-failed',
  A: 'border-status-failed/40 bg-status-failed/10 text-status-failed font-bold',
};
const LEVEL_CHIP_ACTIVE: Record<string, string> = {
  V: 'border-slate-400 bg-slate-400/20 text-slate-700 dark:text-slate-200 ring-1 ring-slate-400/40',
  D: 'border-slate-400 bg-slate-400/20 text-slate-700 dark:text-slate-200 ring-1 ring-slate-400/40',
  I: 'border-sky-500 bg-sky-500/15 text-sky-700 dark:text-sky-300 ring-1 ring-sky-500/40',
  W: 'border-status-ignored bg-status-ignored/25 text-status-ignored ring-1 ring-status-ignored/50',
  E: 'border-status-failed bg-status-failed/25 text-status-failed ring-1 ring-status-failed/50',
  A: 'border-status-failed bg-status-failed/25 text-status-failed font-bold ring-1 ring-status-failed/50',
};

export function LogsPage({ data }: { data: HtmlTestLogDetails }) {
  const [params, update] = useHashState();
  const [attemptIndex, setAttemptIndex] = useAttemptIndex(data.attempts);
  const attempt = data.attempts.find((a) => a.attempt_index === attemptIndex) ?? data.attempts[0];

  useEffect(() => {
    document.title = `${data.display_name} logs — Marathon`;
  }, [data.display_name]);

  return (
    <AppShell
      title={`${data.display_name} — logs`}
      breadcrumbs={
        <>
          <a href={paths.fromLogsToIndex}>Pools list</a> /{' '}
          <a href={paths.fromLogsToPool(data.pool_id)}>Pool {data.pool_id}</a> /{' '}
          <a href={paths.fromLogsToTest(pathToTestFilenameGuess(data))}>{data.display_name}</a> /
          Logs
        </>
      }
    >
      <AttemptPicker
        attempts={data.attempts}
        current={attempt}
        onSelect={(idx) => setAttemptIndex(idx)}
      />
      {attempt ? (
        <LogViewer attempt={attempt} params={params} update={update} />
      ) : (
        <EmptyState message="No attempts recorded for this test." />
      )}
    </AppShell>
  );
}

function useAttemptIndex(
  attempts: HtmlLogAttempt[],
): [number, (next: number) => void] {
  const [attemptIndex, setAttemptIndex] = useState<number>(() => readAttemptRoute(attempts));

  useEffect(() => {
    function onChange() {
      setAttemptIndex(readAttemptRoute(attempts));
    }
    window.addEventListener('hashchange', onChange);
    return () => window.removeEventListener('hashchange', onChange);
  }, [attempts]);

  const set = (next: number) => {
    const raw = window.location.hash.replace(/^#/, '');
    const [, query] = raw.split('?');
    const route = `/attempt/${next}`;
    const rendered = query ? `#${route}?${query}` : `#${route}`;
    history.replaceState(null, '', rendered);
    setAttemptIndex(next);
  };

  return [attemptIndex, set];
}

function readAttemptRoute(attempts: HtmlLogAttempt[]): number {
  const raw = window.location.hash.replace(/^#/, '');
  const route = raw.split('?')[0] ?? '';
  const match = /^\/attempt\/(\d+)/.exec(route);
  if (match?.[1]) {
    const parsed = Number(match[1]);
    if (attempts.some((a) => a.attempt_index === parsed)) return parsed;
  }
  const finalAttempt = attempts.find((a) => a.final) ?? attempts[0];
  return finalAttempt?.attempt_index ?? 0;
}

/**
 * The log details payload doesn't carry the parent test HTML filename, but
 * every attempt log path ends with `.../logs/<filename>` and the parent test
 * file lives one directory up. Reconstruct the sibling href from that.
 */
function pathToTestFilenameGuess(data: HtmlTestLogDetails): string {
  const raw = data.attempts[0]?.log_path ?? '';
  const match = /([^/]+)$/.exec(raw);
  return match?.[1] ?? 'index.html';
}

function AttemptPicker({
  attempts,
  current,
  onSelect,
}: {
  attempts: HtmlLogAttempt[];
  current: HtmlLogAttempt | undefined;
  onSelect: (index: number) => void;
}) {
  if (attempts.length <= 1) {
    if (!current) return null;
    return (
      <div className="mb-3 flex items-center gap-2 rounded border border-surface-border bg-surface-alt px-3 py-2 text-sm">
        <span
          className={clsx(
            'inline-flex items-center rounded border px-1.5 py-0.5 text-xs font-medium uppercase tracking-wide',
            toneClasses(current.status),
          )}
        >
          {current.status}
        </span>
        <DeviceChip device={current.device} />
        <span className="text-xs text-slate-500 dark:text-slate-400">
          Attempt {current.attempt_index + 1}
          {current.final && ' (final)'}
        </span>
      </div>
    );
  }
  return (
    <div className="mb-3 flex flex-wrap items-center gap-2 rounded border border-surface-border bg-surface-alt px-3 py-2">
      <span className="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">
        Attempt
      </span>
      {attempts.map((a) => {
        const active = a.attempt_index === current?.attempt_index;
        return (
          <button
            key={a.attempt_index}
            type="button"
            onClick={() => onSelect(a.attempt_index)}
            className={clsx(
              'inline-flex items-center gap-2 rounded-full border px-2 py-0.5 text-xs transition',
              toneClasses(a.status, active),
              !active && 'hover:brightness-110',
            )}
          >
            <span className="font-semibold tabular-nums">{a.attempt_index + 1}</span>
            <span className="uppercase tracking-wide">{a.status}</span>
            <DeviceChip device={a.device} compact />
            {a.final && (
              <span className="rounded border border-current/40 px-1 py-0.5 text-[10px] uppercase tracking-wide opacity-80">
                final
              </span>
            )}
          </button>
        );
      })}
    </div>
  );
}

function EmptyState({ message }: { message: string }) {
  return (
    <div className="rounded-lg border border-dashed border-surface-border p-8 text-center text-sm text-slate-500 dark:text-slate-400">
      {message}
    </div>
  );
}

function LogViewer({
  attempt,
  params,
  update,
}: {
  attempt: HtmlLogAttempt;
  params: URLSearchParams;
  update: (mutate: (p: URLSearchParams) => void) => void;
}) {
  const [text, setText] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [wrap, setWrap] = useState(true);

  useEffect(() => {
    setText(null);
    setError(null);
    // Prefer the inlined body when the reporter provides it — Chromium blocks
    // `fetch()` under `file://`, so log_path alone doesn't work for users who
    // just double-click index.html. Kotlin inlines log_body on emit; static
    // fixtures always inline. Fall back to fetch for pre-v2 payloads.
    if (typeof attempt.log_body === 'string') {
      setText(attempt.log_body);
      return;
    }
    if (!attempt.log_path) {
      setError('This attempt did not capture any log output.');
      return;
    }
    fetch(attempt.log_path)
      .then((res) => {
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return res.text();
      })
      .then((body) => setText(body))
      .catch((e: unknown) => setError(String(e)));
  }, [attempt.log_body, attempt.log_path]);

  const forcedParser = params.get('parser');
  const parser: LogParser | null = useMemo(() => {
    if (text === null) return null;
    if (forcedParser) return findParser(forcedParser);
    return detectParser(text);
  }, [text, forcedParser]);

  const entries: LogEntry[] = useMemo(() => {
    if (text === null || parser === null) return [];
    return text
      .split(/\r?\n/)
      .map((line, index) => parser.parse(line, index))
      .filter((e) => e.raw.length > 0);
  }, [text, parser]);

  const filters = readLogFilters(params);
  const filtered = useMemo(() => applyLogFilters(entries, filters), [entries, filters]);

  if (error) return <EmptyState message={error} />;
  if (text === null) return <EmptyState message="Loading log…" />;
  if (text.length === 0) return <EmptyState message="Log is empty." />;

  return (
    <div className="flex flex-col gap-2">
      <FilterBar
        parser={parser!}
        entries={entries}
        params={params}
        update={update}
        wrap={wrap}
        onWrap={setWrap}
      />
      <RowStats total={entries.length} shown={filtered.length} />
      <VirtualLog entries={filtered} parser={parser!} wrap={wrap} />
    </div>
  );
}

function RowStats({ total, shown }: { total: number; shown: number }) {
  return (
    <div className="text-xs text-slate-500 dark:text-slate-400">
      {shown === total ? `${total} lines` : `${shown} / ${total} lines`}
    </div>
  );
}

interface LogFilters {
  levels: Set<string>;
  tags: Set<string>;
  pids: Set<string>;
  q: string;
  useRegex: boolean;
}

function readLogFilters(params: URLSearchParams): LogFilters {
  const toSet = (v: string | null) => new Set(v?.split(',').filter(Boolean) ?? []);
  return {
    levels: toSet(params.get('level')),
    tags: toSet(params.get('tag')),
    pids: toSet(params.get('pid')),
    q: params.get('q') ?? '',
    useRegex: params.get('re') === '1',
  };
}

function applyLogFilters(entries: LogEntry[], filters: LogFilters): LogEntry[] {
  const rx = filters.useRegex && filters.q ? tryRegex(filters.q) : null;
  const lowerQ = filters.q.toLowerCase();
  return entries.filter((e) => {
    if (filters.levels.size && (!e.level || !filters.levels.has(e.level))) return false;
    if (filters.tags.size && (!e.tag || !filters.tags.has(e.tag))) return false;
    if (filters.pids.size && (!e.pid || !filters.pids.has(e.pid))) return false;
    if (filters.q) {
      const hay = e.raw;
      if (rx) {
        if (!rx.test(hay)) return false;
      } else if (!hay.toLowerCase().includes(lowerQ)) {
        return false;
      }
    }
    return true;
  });
}

function tryRegex(pattern: string): RegExp | null {
  try {
    return new RegExp(pattern, 'i');
  } catch {
    return null;
  }
}

function FilterBar({
  parser,
  entries,
  params,
  update,
  wrap,
  onWrap,
}: {
  parser: LogParser;
  entries: LogEntry[];
  params: URLSearchParams;
  update: (mutate: (p: URLSearchParams) => void) => void;
  wrap: boolean;
  onWrap: (v: boolean) => void;
}) {
  const filters = readLogFilters(params);
  const availableLevels = useMemo(
    () =>
      LEVEL_ORDER.filter((level) => entries.some((e) => e.level === level)),
    [entries],
  );
  const availableTags = useMemo(
    () =>
      [...new Set(entries.map((e) => e.tag).filter((t): t is string => Boolean(t)))].sort().slice(0, 100),
    [entries],
  );
  const availablePids = useMemo(
    () =>
      [...new Set(entries.map((e) => e.pid).filter((p): p is string => Boolean(p)))].sort(),
    [entries],
  );

  const toggleSet = (key: string, values: Set<string>, value: string) =>
    update((p) => {
      const next = new Set(values);
      if (next.has(value)) next.delete(value);
      else next.add(value);
      if (next.size === 0) p.delete(key);
      else p.set(key, [...next].join(','));
    });

  return (
    <div className="flex flex-col gap-2 rounded-lg border border-surface-border bg-surface-alt p-3">
      <div className="flex flex-wrap items-center gap-2">
        <div className="flex items-center gap-1">
          <input
            value={filters.q}
            onChange={(e) => update((p) => (e.target.value ? p.set('q', e.target.value) : p.delete('q')))}
            placeholder="Filter (/)"
            className="w-64 rounded border border-surface-border bg-surface px-2 py-1 text-sm"
          />
          <button
            type="button"
            onClick={() =>
              update((p) => (filters.useRegex ? p.delete('re') : p.set('re', '1')))
            }
            className={clsx(
              'rounded border px-1.5 py-1 text-xs',
              filters.useRegex
                ? 'border-sky-500 bg-sky-500/10 text-sky-700 dark:text-sky-300'
                : 'border-surface-border bg-surface',
            )}
            title="Toggle regex"
          >
            .*
          </button>
        </div>
        <div className="flex-1" />
        <label className="flex items-center gap-1 text-xs">
          <span className="text-slate-500 dark:text-slate-400">Parser</span>
          <select
            value={parser.id}
            onChange={(e) => update((p) => p.set('parser', e.target.value))}
            className="rounded border border-surface-border bg-surface px-1 py-0.5"
          >
            {PARSERS.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
        </label>
        <label className="flex items-center gap-1 text-xs">
          <input type="checkbox" checked={wrap} onChange={(e) => onWrap(e.target.checked)} />
          <span>wrap</span>
        </label>
      </div>
      {availableLevels.length > 0 && (
        <div className="flex flex-wrap items-center gap-1 text-xs">
          <span className="mr-1 text-slate-500 dark:text-slate-400">Level:</span>
          {availableLevels.map((lvl) => (
            <FilterChip
              key={lvl}
              active={filters.levels.has(lvl)}
              onClick={() => toggleSet('level', filters.levels, lvl)}
              variantClass={filters.levels.has(lvl) ? LEVEL_CHIP_ACTIVE[lvl] : LEVEL_CHIP_REST[lvl]}
            >
              {lvl}
            </FilterChip>
          ))}
        </div>
      )}
      {availableTags.length > 0 && (
        <details className="text-xs">
          <summary className="cursor-pointer select-none text-slate-500 dark:text-slate-400">
            Tag ({availableTags.length})
          </summary>
          <div className="mt-1 flex flex-wrap gap-1">
            {availableTags.map((tag) => (
              <FilterChip
                key={tag}
                active={filters.tags.has(tag)}
                onClick={() => toggleSet('tag', filters.tags, tag)}
              >
                {tag}
              </FilterChip>
            ))}
          </div>
        </details>
      )}
      {availablePids.length > 0 && availablePids.length < 40 && (
        <div className="flex flex-wrap items-center gap-1 text-xs">
          <span className="mr-1 text-slate-500 dark:text-slate-400">PID:</span>
          {availablePids.map((pid) => (
            <FilterChip
              key={pid}
              active={filters.pids.has(pid)}
              onClick={() => toggleSet('pid', filters.pids, pid)}
            >
              {pid}
            </FilterChip>
          ))}
        </div>
      )}
    </div>
  );
}

function FilterChip({
  active,
  onClick,
  variantClass,
  children,
}: {
  active: boolean;
  onClick: () => void;
  /** Optional per-chip color override (level filter uses this to tint bytype). */
  variantClass?: string;
  children: React.ReactNode;
}) {
  const fallback = active
    ? 'border-sky-500 bg-sky-500/10 text-sky-700 dark:text-sky-300'
    : 'border-surface-border bg-surface hover:border-slate-400 dark:hover:border-slate-500';
  return (
    <button
      type="button"
      onClick={onClick}
      className={clsx(
        'inline-flex items-center rounded-full border px-2 py-0.5 transition',
        variantClass ?? fallback,
      )}
    >
      {children}
    </button>
  );
}

function VirtualLog({
  entries,
  parser,
  wrap,
}: {
  entries: LogEntry[];
  parser: LogParser;
  wrap: boolean;
}) {
  const parentRef = useRef<HTMLDivElement>(null);
  const scrollMargin = useScrollMargin(parentRef);
  // See PoolPage — one document-level scroll, no inner scroll region.
  const virtualizer = useWindowVirtualizer({
    count: entries.length,
    estimateSize: () => (wrap ? 32 : 20),
    overscan: 30,
    scrollMargin,
  });

  // With per-format columns, we skip empty columns entirely; makes the
  // generic parser produce a clean single-column view.
  const showTimestamp = entries.some((e) => e.timestamp);
  const showLevel = entries.some((e) => e.level);
  const showTag = entries.some((e) => e.tag);
  const showPid = entries.some((e) => e.pid);

  return (
    <div
      ref={parentRef}
      className="rounded-lg border border-surface-border bg-surface"
    >
      <div style={{ height: virtualizer.getTotalSize(), position: 'relative' }}>
        {virtualizer.getVirtualItems().map((v) => {
          const entry = entries[v.index];
          if (!entry) return null;
          const offset = v.start - scrollMargin;
          return (
            <LogRow
              key={v.key}
              index={v.index}
              measureRef={virtualizer.measureElement}
              style={{ transform: `translateY(${offset}px)` }}
              entry={entry}
              showTimestamp={showTimestamp}
              showLevel={showLevel}
              showTag={showTag}
              showPid={showPid}
              wrap={wrap}
            />
          );
        })}
      </div>
      <div className="sr-only" aria-hidden>
        Parsed with {parser.name}
      </div>
    </div>
  );
}

function LogRow({
  index,
  measureRef,
  style,
  entry,
  showTimestamp,
  showLevel,
  showTag,
  showPid,
  wrap,
}: {
  index: number;
  measureRef: (el: HTMLElement | null) => void;
  style: React.CSSProperties;
  entry: LogEntry;
  showTimestamp: boolean;
  showLevel: boolean;
  showTag: boolean;
  showPid: boolean;
  wrap: boolean;
}) {
  const levelClass = (entry.level && LEVEL_CLASS[entry.level]) ?? '';
  return (
    <div
      data-index={index}
      ref={measureRef}
      className={clsx(
        'absolute inset-x-0 top-0 flex gap-2 border-b border-surface-border/40 px-3 py-0.5 font-mono text-xs',
        levelClass,
      )}
      style={style}
      title={entry.timestamp ? formatTimestamp(entry.timestampMs ?? 0) : undefined}
    >
      {showTimestamp && (
        <span className="w-40 shrink-0 text-slate-500 dark:text-slate-400 tabular-nums">
          {entry.timestamp ?? ''}
        </span>
      )}
      {showPid && (
        <span className="w-14 shrink-0 text-right text-slate-500 dark:text-slate-400 tabular-nums">
          {entry.pid ?? ''}
        </span>
      )}
      {showLevel && (
        <span className="w-4 shrink-0 text-center font-bold">{entry.level ?? ''}</span>
      )}
      {showTag && (
        <span className="w-40 shrink-0 truncate text-slate-600 dark:text-slate-300">
          {entry.tag ?? ''}
        </span>
      )}
      <span className={clsx('min-w-0 flex-1', wrap ? 'whitespace-pre-wrap break-words' : 'truncate')}>
        {entry.message}
      </span>
    </div>
  );
}
