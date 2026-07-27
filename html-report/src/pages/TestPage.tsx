import { useEffect, useRef, useState } from 'react';
import clsx from 'clsx';
import { AppShell } from '../components/AppShell';
import { DeviceChip } from '../components/DeviceChip';
import { DeviceDetails } from '../components/DeviceDetails';
import { Lightbox } from '../components/Lightbox';
import { FlakyBadge, StatusBadge, toneClasses } from '../components/StatusBadge';
import type { HtmlAttempt, HtmlFullTest } from '../types/report';
import { paths } from '../util/paths';
import { formatDuration, formatTimestamp } from '../util/time';

export function TestPage({ data }: { data: HtmlFullTest }) {
  useEffect(() => {
    document.title = `${data.name} — Marathon`;
  }, [data.name]);

  return (
    <AppShell
      title={data.name}
      breadcrumbs={
        <>
          <a href={paths.fromTestToIndex}>Pools list</a> /{' '}
          <a href={paths.fromTestToPool(data.pool_id)}>Pool {data.pool_id}</a> / {data.device_id}
        </>
      }
    >
      <Header data={data} />
      <DistinctDevicesSummary devices={data.distinct_devices} />
      <AttemptStrip attempts={data.attempts} logFilename={data.filename} />
    </AppShell>
  );
}

function Header({ data }: { data: HtmlFullTest }) {
  return (
    <section className="mb-4 rounded-lg border border-surface-border bg-surface-alt p-4">
      <div className="flex flex-wrap items-center gap-3">
        <StatusBadge status={data.status} />
        {data.is_flaky && <FlakyBadge />}
        <div className="flex-1 min-w-0">
          <div className="text-xl font-semibold truncate">{data.name}</div>
          <div className="truncate text-sm text-slate-500 dark:text-slate-400">
            {data.class_name}
            <span className="mx-1 opacity-60">·</span>
            {data.package_name}
          </div>
        </div>
        <div className="text-right text-sm">
          <div className="tabular-nums">{formatDuration(data.duration_millis)}</div>
          <div className="text-xs text-slate-500 dark:text-slate-400">
            {data.attempt_count} attempt{data.attempt_count === 1 ? '' : 's'}
          </div>
        </div>
      </div>
    </section>
  );
}

function DistinctDevicesSummary({ devices }: { devices: HtmlFullTest['distinct_devices'] }) {
  if (devices.length <= 1) return null;
  return (
    <section className="mb-4 rounded-lg border border-surface-border bg-surface p-3">
      <div className="mb-2 text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">
        Ran on
      </div>
      <div className="flex flex-wrap gap-1.5">
        {devices.map((d) => (
          <DeviceChip key={d.serial} device={d} />
        ))}
      </div>
    </section>
  );
}

function AttemptStrip({
  attempts,
  logFilename,
}: {
  attempts: HtmlAttempt[];
  logFilename: string;
}) {
  // Read `#/attempt/N` on load / hashchange so deep-links from the timeline
  // (or shared URLs) highlight and scroll the requested attempt into view.
  const focused = useFocusedAttempt();
  const showHighlight = focused !== null && attempts.length > 1;
  return (
    <section aria-label="Attempts">
      <h2 className="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">
        Attempts
      </h2>
      <ol className="flex flex-col gap-2">
        {attempts.map((attempt) => (
          <AttemptCard
            key={attempt.attempt_index}
            attempt={attempt}
            defaultExpanded={attempt.status !== 'passed' || attempt.attempt_index === focused}
            logFilename={logFilename}
            highlighted={showHighlight && attempt.attempt_index === focused}
          />
        ))}
      </ol>
    </section>
  );
}

/**
 * Track the attempt index encoded as `#/attempt/N` on the URL. Updates on
 * `hashchange` so timeline-driven navigation between attempts re-focuses
 * without a full reload.
 */
function useFocusedAttempt(): number | null {
  const [index, setIndex] = useState<number | null>(readAttemptHash);
  useEffect(() => {
    function onChange() {
      setIndex(readAttemptHash());
    }
    window.addEventListener('hashchange', onChange);
    return () => window.removeEventListener('hashchange', onChange);
  }, []);
  return index;
}

function readAttemptHash(): number | null {
  const raw = window.location.hash.replace(/^#/, '');
  const route = raw.split('?')[0] ?? '';
  const match = /^\/attempt\/(\d+)/.exec(route);
  if (!match?.[1]) return null;
  const n = Number(match[1]);
  return Number.isFinite(n) ? n : null;
}

function AttemptCard({
  attempt,
  defaultExpanded,
  logFilename,
  highlighted = false,
}: {
  attempt: HtmlAttempt;
  defaultExpanded: boolean;
  logFilename: string;
  highlighted?: boolean;
}) {
  const [expanded, setExpanded] = useState(defaultExpanded);
  const ref = useRef<HTMLLIElement>(null);
  useEffect(() => {
    if (!highlighted) return;
    setExpanded(true);
    // Scroll into view once expanded — small timeout gives layout a beat so
    // the body content is measured before we snap.
    const t = window.setTimeout(() => {
      ref.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 30);
    return () => window.clearTimeout(t);
  }, [highlighted]);
  return (
    <li
      ref={ref}
      className={clsx(
        'overflow-hidden rounded-lg border transition',
        attempt.status === 'failed'
          ? 'border-status-failed/40'
          : attempt.status === 'passed'
            ? 'border-status-passed/40'
            : 'border-status-ignored/40',
        // Ring-pulse highlight only when the URL points at this attempt AND
        // there is more than one attempt to disambiguate (see `showHighlight`
        // gate in `AttemptStrip`).
        highlighted && 'ring-2 ring-sky-400 ring-offset-2 ring-offset-surface',
      )}
    >
      <button
        type="button"
        onClick={() => setExpanded(!expanded)}
        aria-expanded={expanded}
        // Header bar tints in the status tone (like a filter chip) so scanning
        // a long attempt list reads at a glance without landing on the badge.
        className={clsx(
          'flex w-full items-center gap-3 px-3 py-2 text-left transition hover:brightness-110',
          toneClasses(attempt.status),
        )}
      >
        <span className="inline-flex h-6 w-6 items-center justify-center rounded-full border border-current/40 text-xs font-semibold tabular-nums">
          {attempt.attempt_index + 1}
        </span>
        <span className="rounded border border-current/40 px-1.5 py-0.5 text-xs font-medium uppercase tracking-wide">
          {attempt.status}
        </span>
        {attempt.final && (
          <span className="rounded border border-current/40 px-1.5 py-0.5 text-[10px] uppercase tracking-wide opacity-80">
            final
          </span>
        )}
        <DeviceChip device={attempt.device} />
        <span className="tabular-nums text-sm opacity-80">
          {formatDuration(attempt.duration_millis)}
        </span>
        <div className="flex-1" />
        <span className="text-xs opacity-80">{expanded ? '▾' : '▸'}</span>
      </button>
      {expanded && <AttemptBody attempt={attempt} logFilename={logFilename} />}
    </li>
  );
}

function AttemptBody({ attempt, logFilename }: { attempt: HtmlAttempt; logFilename: string }) {
  const hasArtifacts = attempt.screenshot || attempt.videos.length > 0 || attempt.log_file;
  return (
    <div className="grid gap-4 border-t border-surface-border bg-surface p-4 md:grid-cols-2">
      <div className="min-w-0">
        <div className="mb-2 text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">
          Timing
        </div>
        <dl className="grid grid-cols-[max-content_1fr] gap-x-4 gap-y-1 text-xs">
          <dt className="text-slate-500 dark:text-slate-400">Started</dt>
          <dd className="font-mono">{formatTimestamp(attempt.start_time_ms)}</dd>
          <dt className="text-slate-500 dark:text-slate-400">Ended</dt>
          <dd className="font-mono">{formatTimestamp(attempt.end_time_ms)}</dd>
          <dt className="text-slate-500 dark:text-slate-400">Duration</dt>
          <dd className="font-mono">{formatDuration(attempt.duration_millis)}</dd>
          <dt className="text-slate-500 dark:text-slate-400">Batch</dt>
          <dd className="font-mono break-all">{attempt.batch_id}</dd>
        </dl>
        <div className="mt-4">
          <div className="mb-2 text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">
            Device
          </div>
          <DeviceDetails device={attempt.device} />
        </div>
      </div>
      <div className="min-w-0">
        {attempt.stacktrace && (
          <StacktraceBlock text={attempt.stacktrace} />
        )}
        {hasArtifacts && (
          <div className="mt-4 flex flex-col gap-3">
            {attempt.screenshot && <Lightbox src={attempt.screenshot} alt="Screenshot" />}
            {attempt.videos.map((v) => (
              // preload="none" — under `file://` Chromium doesn't honor Range
              // requests on local mp4s, and `preload="metadata"` fires a
              // Range: 0-N GET that returns 0 bytes, marking the source as
              // empty before the user ever clicks play. Deferring the fetch
              // until interaction avoids the truncated read.
              <video key={v} controls preload="none" playsInline src={v} className="w-full rounded border border-surface-border bg-black" />
            ))}
            {attempt.log_file && (
              <a
                href={paths.fromTestToLogs(logFilename) + `#/attempt/${attempt.attempt_index}`}
                className="inline-flex w-fit items-center gap-1 rounded border border-surface-border bg-surface-alt px-2 py-1 text-xs hover:bg-slate-100 dark:hover:bg-slate-700"
              >
                Log →
              </a>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

function StacktraceBlock({ text }: { text: string }) {
  const [copied, setCopied] = useState(false);
  const onCopy = async () => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    } catch {
      // Clipboard permission can fail under file://; user can select+copy manually.
    }
  };
  return (
    <div>
      <div className="mb-1 flex items-center justify-between">
        <span className="text-xs uppercase tracking-wide text-slate-500 dark:text-slate-400">
          Stacktrace
        </span>
        <button
          type="button"
          onClick={onCopy}
          className="rounded border border-surface-border bg-surface px-2 py-0.5 text-[10px] uppercase tracking-wide hover:bg-slate-100 dark:hover:bg-slate-700"
        >
          {copied ? 'copied' : 'copy'}
        </button>
      </div>
      <pre className="max-h-96 overflow-auto rounded border border-surface-border bg-surface-alt p-2 text-xs">
        {text}
      </pre>
    </div>
  );
}

