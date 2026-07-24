/**
 * Format a duration in milliseconds as `H:MM:SS.mmm` or `M:SS.mmm`.
 * Hours dropped when zero. Matches the format the pre-2.0 report used, so
 * users comparing archived and freshly-generated reports see consistent rows.
 */
export function formatDuration(ms: number): string {
  if (!Number.isFinite(ms) || ms < 0) return '—';
  const millis = Math.floor(ms % 1000);
  const totalSecs = Math.floor(ms / 1000);
  const secs = totalSecs % 60;
  const totalMins = Math.floor(totalSecs / 60);
  const mins = totalMins % 60;
  const hrs = Math.floor(totalMins / 60);
  const pad2 = (n: number) => n.toString().padStart(2, '0');
  const pad3 = (n: number) => n.toString().padStart(3, '0');
  if (hrs > 0) return `${hrs}:${pad2(mins)}:${pad2(secs)}.${pad3(millis)}`;
  return `${mins}:${pad2(secs)}.${pad3(millis)}`;
}

/** Absolute wall-clock timestamp, UTC-locale-agnostic. */
export function formatTimestamp(ms: number): string {
  if (!ms) return '—';
  return new Date(ms).toISOString().replace('T', ' ').replace('Z', ' UTC');
}
