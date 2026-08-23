// Mirror of `TimelineExecutionResult` (Kotlin, report/execution-timeline).
// Field names match the emitted JSON verbatim.

export type MetricType =
  | 'PASSED'
  | 'FAILURE'
  | 'IGNORED'
  | 'INCOMPLETE'
  | 'ASSUMPTION_FAILURE'
  | 'DEVICE_PROVIDER_INIT'
  | 'DEVICE_PREPARE';

export interface TimelineData {
  testName: string;
  metricType: MetricType;
  startDate: number;
  endDate: number;
  expectedValue: number;
  variance: number;
  // Populated when the timeline reporter is upgraded to schema >= 2:
  batchId?: string;
  attemptIndex?: number;
  poolId?: string;
  testFilename?: string;
  deviceSerial?: string;
}

export interface TimelineDevice {
  serial?: string;
  modelName?: string;
  manufacturer?: string;
  osVersion?: string;
  osMajor?: number | null;
}

export interface TimelineMeasure {
  measure: string; // device serial
  stats: {
    idleTimeMillis: number;
    averageTestExecutionTimeMillis: number;
  };
  data: TimelineData[];
  /** Optional in schema 2+; the chart uses this for tooltip enrichment. */
  device?: TimelineDevice;
}

export interface TimelineExecutionResult {
  reportSchemaVersion?: number;
  passedTests: number;
  failedTests: number;
  ignoredTests: number;
  executionStats: {
    idleTimeMillis: number;
    averageTestExecutionTimeMillis: number;
  };
  measures: TimelineMeasure[];
}
