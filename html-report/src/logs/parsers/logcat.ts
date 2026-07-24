import type { LogEntry, LogParser } from '../types';

// Android logcat -v threadtime (default marathon captures use this shape):
//   MM-DD HH:MM:SS.mmm  PID  TID L TAG    : MSG
// Level letter is a single char: V D I W E A F S.
const LOGCAT_LINE = /^(\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+(\d+)\s+([VDIWEAF])\s+([^:]{1,64}?)\s*:\s?(.*)$/;

export const logcatParser: LogParser = {
  id: 'logcat',
  name: 'Android logcat',
  detect(sampleLines) {
    if (sampleLines.length === 0) return 0;
    const hits = sampleLines.filter((line) => LOGCAT_LINE.test(line)).length;
    return hits / sampleLines.length;
  },
  parse(line, index): LogEntry {
    const match = LOGCAT_LINE.exec(line);
    if (!match) return { index, raw: line, message: line };
    const [, timestamp, pid, tid, level, tag, message] = match;
    return {
      index,
      raw: line,
      timestamp,
      timestampMs: parseLogcatTimestamp(timestamp!),
      pid,
      tid,
      level,
      tag: tag?.trim(),
      message: message ?? '',
    };
  },
};

/**
 * logcat's default format omits the year; assume the log was captured "this
 * year" for month/day/hour/minute/second/millis. Good enough for time-range
 * filtering within a single test run; not accurate across year boundaries.
 */
function parseLogcatTimestamp(ts: string): number | undefined {
  const match = /^(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})\.(\d{3})$/.exec(ts);
  if (!match) return undefined;
  const [, mm, dd, hh, mi, ss, ms] = match;
  const now = new Date();
  const year = now.getUTCFullYear();
  return Date.UTC(year, Number(mm) - 1, Number(dd), Number(hh), Number(mi), Number(ss), Number(ms));
}
