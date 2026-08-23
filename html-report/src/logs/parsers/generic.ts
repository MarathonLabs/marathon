import type { LogEntry, LogParser } from '../types';

/**
 * Fallback parser. Every line becomes a `message` with no metadata; keeps the
 * viewer usable when the format is unknown or freeform. Always matches with
 * confidence just above zero so the detector uses it when nothing else fits.
 */
export const genericParser: LogParser = {
  id: 'generic',
  name: 'Generic (plain text)',
  detect() {
    return 0.01;
  },
  parse(line, index): LogEntry {
    return { index, raw: line, message: line };
  },
};
