import clsx from 'clsx';
import type { Status } from '../types/report';

export type StatusTone = Status | 'flaky';

/**
 * Chip tint tokens per status. Two flavors so a filter chip can render an
 * always-visible "at rest" tone plus a stronger "active" tone when selected,
 * without ever falling back to the neutral surface color that hides which
 * status a chip represents.
 */
const TONE_REST: Record<StatusTone, string> = {
  passed: 'border-status-passed/40 bg-status-passed/10 text-status-passed',
  failed: 'border-status-failed/40 bg-status-failed/10 text-status-failed',
  ignored: 'border-status-ignored/40 bg-status-ignored/10 text-status-ignored',
  flaky: 'border-status-flaky/40 bg-status-flaky/10 text-status-flaky',
};

const TONE_ACTIVE: Record<StatusTone, string> = {
  passed: 'border-status-passed bg-status-passed/25 text-status-passed ring-1 ring-status-passed/50',
  failed: 'border-status-failed bg-status-failed/25 text-status-failed ring-1 ring-status-failed/50',
  ignored: 'border-status-ignored bg-status-ignored/25 text-status-ignored ring-1 ring-status-ignored/50',
  flaky: 'border-status-flaky bg-status-flaky/25 text-status-flaky ring-1 ring-status-flaky/50',
};

export function toneClasses(tone: StatusTone, active = false): string {
  return active ? TONE_ACTIVE[tone] : TONE_REST[tone];
}

export function StatusBadge({ status, className }: { status: Status; className?: string }) {
  return (
    <span
      className={clsx(
        'inline-flex items-center rounded border px-1.5 py-0.5 text-xs font-medium uppercase tracking-wide',
        toneClasses(status),
        className,
      )}
    >
      {status}
    </span>
  );
}

export function FlakyBadge({ className }: { className?: string }) {
  return (
    <span
      className={clsx(
        'inline-flex items-center rounded border px-1.5 py-0.5 text-xs font-medium uppercase tracking-wide',
        toneClasses('flaky'),
        className,
      )}
    >
      flaky
    </span>
  );
}
