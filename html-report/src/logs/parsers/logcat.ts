import type { LogEntry, LogParser } from '../types';

// Marathon's Android runner captures logcat via `adb logcat -v long` and
// re-serializes each message as (see AdamAndroidDevice.onLogcat):
//
//   MM-DD HH:MM:SS.mmm  PID-TID/appName L/tag: message
//   07-25 16:12:05.006  12345-12346/?      I/AndroidRuntime: bootstrap ok
//
// The `-v threadtime` shape (what `adb logcat` prints from the command line
// by default on modern Android) uses whitespace-separated PID/TID columns
// instead:
//
//   MM-DD HH:MM:SS.mmm  PID  TID L TAG    : MSG
//
// Both fall through the same parser so hand-captured `logcat > file.log`
// dumps also render cleanly.
const LOGCAT_MARATHON =
  /^(\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)-(\w+)\/(\S*)\s+([VDIWEAF])\/([^:]{1,128}?):\s?(.*)$/;

const LOGCAT_THREADTIME =
  /^(\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\s+(\d+)\s+(\d+)\s+([VDIWEAF])\s+([^:]{1,128}?)\s*:\s?(.*)$/;

export const logcatParser: LogParser = {
  id: 'logcat',
  name: 'Android logcat',
  detect(sampleLines) {
    if (sampleLines.length === 0) return 0;
    let hits = 0;
    for (const line of sampleLines) {
      if (LOGCAT_MARATHON.test(line) || LOGCAT_THREADTIME.test(line)) hits++;
    }
    return hits / sampleLines.length;
  },
  parse(line, index): LogEntry {
    const m = LOGCAT_MARATHON.exec(line);
    if (m) {
      const [, timestamp, pid, tid, , level, tag, message] = m;
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
    }
    const t = LOGCAT_THREADTIME.exec(line);
    if (t) {
      const [, timestamp, pid, tid, level, tag, message] = t;
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
    }
    return { index, raw: line, message: line };
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
