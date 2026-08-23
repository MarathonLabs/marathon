// Wire types for the payloads emitted by `HtmlSummaryReporter` (Kotlin).
// Field names mirror the JSON keys exactly — snake_case throughout — because
// the payload is inlined verbatim into `window.<global>` by the Kotlin side.
//
// Keep this file in sync with:
//   report/html-report/src/main/kotlin/com/malinskiy/marathon/report/*.kt
// Schema is versioned via `HtmlIndex.report_schema_version`.

export type Status = 'passed' | 'failed' | 'ignored';

export interface HtmlDevice {
  serial: string;
  model_name: string;
  manufacturer: string;
  /** Raw OS version string, e.g. "34" (Android) or "17.0" (iOS). */
  os_version: string;
  /** First segment of os_version parsed as int; null when not numeric. */
  os_major: number | null;
  network_state: string;
  features: string[];
  is_tablet: boolean;
  /** Convenience label; usually mirrors os_version. */
  api_level: string;
}

export interface HtmlAttempt {
  attempt_index: number;
  final: boolean;
  status: Status;
  start_time_ms: number;
  end_time_ms: number;
  duration_millis: number;
  batch_id: string;
  device: HtmlDevice;
  stacktrace: string | null;
  screenshot: string;
  videos: string[];
  log_file: string;
}

export interface HtmlShortTest {
  id: string;
  package_name: string;
  filename: string;
  class_name: string;
  name: string;
  duration_millis: number;
  status: Status;
  start_time_ms: number;
  end_time_ms: number;
  batch_id: string;
  device_id: string;
  device: HtmlDevice;
  attempt_count: number;
  is_flaky: boolean;
  /** Distinct device serials touched across attempts. */
  devices: string[];
  /** Distinct OS version strings touched across attempts. */
  os_versions: string[];
  /** True when any attempt of this test produced a linked screenshot. */
  has_screenshot: boolean;
  /** True when any attempt of this test produced a linked video. */
  has_video: boolean;
}

export interface HtmlFullTest {
  pool_id: string;
  package_name: string;
  class_name: string;
  name: string;
  id: string;
  filename: string;
  duration_millis: number;
  status: Status;
  stacktrace: string | null;
  start_time_ms: number;
  end_time_ms: number;
  batch_id: string;
  device: HtmlDevice;
  device_id: string;
  diagnostic_video: boolean;
  diagnostic_screenshots: boolean;
  screenshot: string;
  videos: string[];
  log_file: string;
  attempts: HtmlAttempt[];
  attempt_count: number;
  is_flaky: boolean;
  distinct_devices: HtmlDevice[];
}

export interface HtmlPoolSummary {
  id: string;
  tests: HtmlShortTest[];
  passed_count: number;
  failed_count: number;
  ignored_count: number;
  flaky_count: number;
  duration_millis: number;
  start_time_ms: number;
  end_time_ms: number;
  devices: HtmlDevice[];
}

export interface HtmlIndex {
  report_schema_version: number;
  generated_at_ms: number;
  title: string;
  total_failed: number;
  total_flaky: number;
  total_ignored: number;
  total_passed: number;
  total_duration_millis: number;
  average_duration_millis: number;
  max_duration_millis: number;
  min_duration_millis: number;
  pools: HtmlPoolSummary[];
}

export interface HtmlLogAttempt {
  attempt_index: number;
  final: boolean;
  status: Status;
  device_id: string;
  device: HtmlDevice;
  log_path: string;
  /**
   * Optional inlined log text. Preferred when present because `file://`-hosted
   * reports cannot always `fetch(log_path)` on Chromium. Kotlin emits this on
   * generate; static fixtures always inline it.
   */
  log_body?: string;
}

export interface HtmlTestLogDetails {
  pool_id: string;
  test_id: string;
  display_name: string;
  attempts: HtmlLogAttempt[];
}

/**
 * Latest schema version this UI understands. Bump alongside
 * `HtmlIndex.REPORT_SCHEMA_VERSION` on the Kotlin side.
 */
export const SUPPORTED_SCHEMA_VERSION = 2;
