/** Normalized log record — same shape regardless of source format. */
export interface LogEntry {
  /** Absolute line index in the source (0-based). */
  index: number;
  /** Raw line as fetched. Preserved verbatim for copy-paste and fallback rendering. */
  raw: string;
  /**
   * ISO-ish timestamp if the parser could extract one. Not always wall-clock;
   * android logcat's default format is `MM-DD HH:MM:SS.SSS`, treated as
   * "same day" for sorting purposes.
   */
  timestamp?: string;
  /** Millisecond timestamp when derivable; used for time-range filtering. */
  timestampMs?: number;
  /** Level letter or short name: V, D, I, W, E, A (or short strings). */
  level?: string;
  /** Log tag / subsystem / category. */
  tag?: string;
  /** Process ID (numeric on logcat, may be string on other formats). */
  pid?: string;
  /** Thread ID. */
  tid?: string;
  /** Body of the line minus the parsed metadata. */
  message: string;
}

export interface LogParser {
  /** Stable id used for user-facing dropdown + URL hash param. */
  id: string;
  /** Display name. */
  name: string;
  /**
   * Given up to `sampleLines` lines from the top of a log, return a rough
   * confidence between 0 and 1 for whether this parser applies. The detector
   * picks the highest-scoring parser; ties resolve in registration order.
   */
  detect: (sampleLines: string[]) => number;
  parse: (line: string, index: number) => LogEntry;
}
