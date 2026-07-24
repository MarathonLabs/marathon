import type { LogEntry, LogParser } from '../types';

// Apple `os_log` / `xcrun simctl spawn ... log stream` default output:
//   2026-01-15 09:12:34.567123-0800  0x1c2d3     Default     0x0            123     0    myApp: (subsystem/category) message
// Marathon's iOS vendor captures a superset shaped like this; we look for the
// leading absolute timestamp + short level column ("Default", "Error", ...).
const OSLOG_LINE =
  /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3,6}[+-]\d{4})\s+0x[0-9a-fA-F]+\s+(Default|Info|Debug|Error|Fault|Notice|Activity)\s+0x[0-9a-fA-F]+\s+(\d+)\s+(\d+)\s+([^:]+?):\s?(.*)$/;

const LEVEL_MAP: Record<string, string> = {
  Default: 'I',
  Info: 'I',
  Notice: 'I',
  Debug: 'D',
  Error: 'E',
  Fault: 'A',
  Activity: 'V',
};

export const osLogParser: LogParser = {
  id: 'oslog',
  name: 'Apple os_log',
  detect(sampleLines) {
    if (sampleLines.length === 0) return 0;
    const hits = sampleLines.filter((line) => OSLOG_LINE.test(line)).length;
    return hits / sampleLines.length;
  },
  parse(line, index): LogEntry {
    const match = OSLOG_LINE.exec(line);
    if (!match) return { index, raw: line, message: line };
    const [, timestamp, levelWord, pid, tid, tag, message] = match;
    return {
      index,
      raw: line,
      timestamp,
      timestampMs: Date.parse(timestamp!),
      level: LEVEL_MAP[levelWord ?? ''] ?? 'I',
      pid,
      tid,
      tag: tag?.trim(),
      message: message ?? '',
    };
  },
};
