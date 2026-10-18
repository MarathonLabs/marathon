import { describe, it, expect, beforeAll, beforeEach } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { TimelineChart } from './TimelineChart';
import type { TimelineData, TimelineDevice, TimelineExecutionResult } from '../types/timeline';

beforeAll(() => {
  // jsdom ships no ResizeObserver; the chart only needs it to exist.
  globalThis.ResizeObserver ??= class {
    observe() {}
    unobserve() {}
    disconnect() {}
  } as unknown as typeof ResizeObserver;
});

beforeEach(() => {
  history.replaceState(null, '', '#');
});

const T0 = Date.UTC(2026, 9, 17, 18, 0, 0);

function testBar(
  testName: string,
  startOffsetMs: number,
  overrides: Partial<TimelineData> = {},
): TimelineData {
  return {
    testName,
    metricType: 'PASSED',
    startDate: T0 + startOffsetMs,
    endDate: T0 + startOffsetMs + 10_000,
    expectedValue: 0,
    variance: 0,
    batchId: `batch-${startOffsetMs}`,
    attemptIndex: 0,
    poolId: 'omni',
    testFilename: `com.example.${testName}.html`,
    deviceSerial: 'final-device',
    ...overrides,
  };
}

function deviceBar(metricType: 'DEVICE_PREPARE' | 'DEVICE_PROVIDER_INIT' | 'TEST_PARSING', startOffsetMs: number): TimelineData {
  return {
    testName: metricType,
    metricType,
    startDate: T0 + startOffsetMs,
    endDate: T0 + startOffsetMs + 5_000,
    expectedValue: 0,
    variance: 0,
  };
}

const google37: TimelineDevice = { manufacturer: 'Google', modelName: 'sdk_gphone', osMajor: 37, osVersion: '37' };

function timeline(
  rows: Array<{ serial: string; data: TimelineData[]; device?: TimelineDevice }>,
): TimelineExecutionResult {
  return {
    reportSchemaVersion: 2,
    passedTests: 0,
    failedTests: 0,
    ignoredTests: 0,
    executionStats: { idleTimeMillis: 0, averageTestExecutionTimeMillis: 0 },
    measures: rows.map((r) => ({
      measure: r.serial,
      stats: { idleTimeMillis: 0, averageTestExecutionTimeMillis: 0 },
      data: r.data,
      device: r.device ?? { ...google37, serial: r.serial },
    })),
  };
}

/** Three devices; `Suite.flaky` retried on each, plus look-alike decoys. */
function flakyFixture() {
  return timeline([
    {
      serial: 'dev-a',
      data: [
        deviceBar('DEVICE_PROVIDER_INIT', 0),
        deviceBar('DEVICE_PREPARE', 1_000),
        testBar('Suite.flaky', 10_000, { metricType: 'FAILURE', attemptIndex: 0 }),
        testBar('Suite.stable', 30_000),
      ],
    },
    {
      serial: 'dev-b',
      data: [
        deviceBar('DEVICE_PREPARE', 1_000),
        testBar('Suite.flaky', 50_000, { metricType: 'FAILURE', attemptIndex: 1 }),
        // Same display name, different class package → different test.
        testBar('Suite.flaky', 70_000, { testFilename: 'com.other.Suite.flaky.html' }),
      ],
    },
    {
      serial: 'dev-c',
      data: [
        testBar('Suite.flaky', 90_000, { attemptIndex: 2 }),
        // Same test identity in another pool → different test.
        testBar('Suite.flaky', 110_000, { poolId: 'other-pool' }),
      ],
    },
  ]);
}

const bars = (c: HTMLElement) => [...c.querySelectorAll<HTMLElement>('.bar')];
const haloed = (c: HTMLElement) => bars(c).filter((b) => b.classList.contains('bar-attempt'));
const flakyAttempts = (c: HTMLElement) =>
  bars(c).filter(
    (b) =>
      b.getAttribute('aria-label')?.startsWith('Suite.flaky') &&
      ['10000', '50000', '90000'].some((o) => b.dataset.testStart === o),
  );
const axisLabels = (c: HTMLElement) =>
  [...c.querySelectorAll<HTMLElement>('.tabular-nums')].map((e) => e.textContent ?? '');
const timeButton = (name: 'absolute' | 'relative') => screen.getByRole('button', { name });

// Bars don't expose their start time in the DOM; tag them by left offset
// order instead, which matches start order for a monotonic scale.
function tagStarts(c: HTMLElement, data: TimelineExecutionResult) {
  const rows = [...c.querySelectorAll<HTMLElement>('.timeline-row-html')];
  rows.forEach((row, i) => {
    const rowBars = [...row.querySelectorAll<HTMLElement>('.bar')];
    data.measures[i]!.data.forEach((d, j) => {
      rowBars[j]!.dataset.testStart = String(d.startDate - T0);
    });
  });
}

describe('TimelineChart toolbar', () => {
  it('is never an empty box when every device shares one OS and manufacturer', () => {
    const { container } = render(<TimelineChart data={flakyFixture()} />);
    expect(screen.queryByText('OS:')).not.toBeInTheDocument();
    expect(screen.queryByText('Manufacturer:')).not.toBeInTheDocument();
    expect(screen.getByRole('group', { name: 'Time scale' })).toBeInTheDocument();
    expect(timeButton('absolute')).toBeVisible();
    expect(container.firstElementChild?.firstElementChild?.textContent).toContain('Time:');
  });

  it('still shows facet rows next to the time toggle when there is something to filter', () => {
    const data = timeline([
      { serial: 'a', data: [testBar('X.t', 0)], device: { ...google37, osMajor: 35 } },
      { serial: 'b', data: [testBar('X.u', 0)], device: { ...google37, manufacturer: 'Samsung' } },
    ]);
    render(<TimelineChart data={data} />);
    expect(screen.getByText('OS:')).toBeInTheDocument();
    expect(screen.getByText('Manufacturer:')).toBeInTheDocument();
    expect(screen.getByRole('group', { name: 'Time scale' })).toBeInTheDocument();
  });
});

describe('TimelineChart time scale', () => {
  it('defaults to absolute wall-clock ticks', () => {
    const { container } = render(<TimelineChart data={flakyFixture()} />);
    expect(timeButton('absolute')).toHaveAttribute('aria-pressed', 'true');
    expect(timeButton('relative')).toHaveAttribute('aria-pressed', 'false');
    const labels = axisLabels(container);
    expect(labels.length).toBeGreaterThan(1);
    for (const l of labels) expect(l).toMatch(/^\d\d:\d\d:\d\d$/);
  });

  it('switches to elapsed ticks starting at +00:00:00 and records it in the hash', () => {
    const { container } = render(<TimelineChart data={flakyFixture()} />);
    fireEvent.click(timeButton('relative'));
    expect(timeButton('relative')).toHaveAttribute('aria-pressed', 'true');
    const labels = axisLabels(container);
    expect(labels[0]).toBe('+00:00:00');
    for (const l of labels) expect(l).toMatch(/^\+\d\d:\d\d:\d\d$/);
    expect(new URLSearchParams(location.hash.split('?')[1]).get('tl_time')).toBe('rel');
  });

  it('switching back to absolute removes the hash key', () => {
    render(<TimelineChart data={flakyFixture()} />);
    fireEvent.click(timeButton('relative'));
    fireEvent.click(timeButton('absolute'));
    expect(location.hash).not.toContain('tl_time');
    expect(timeButton('absolute')).toHaveAttribute('aria-pressed', 'true');
  });

  it('restores relative mode from a shared link', () => {
    history.replaceState(null, '', '#?tl_time=rel');
    const { container } = render(<TimelineChart data={flakyFixture()} />);
    expect(timeButton('relative')).toHaveAttribute('aria-pressed', 'true');
    expect(axisLabels(container)[0]).toBe('+00:00:00');
  });

  it('relative ticks land on round elapsed offsets, not round wall-clock times', () => {
    // Start 7s past the minute: absolute ticks would be :00-aligned clock
    // times, relative ticks must be whole elapsed steps from the run start.
    const off = 7_000;
    const data = timeline([{ serial: 'a', data: [testBar('X.t', off), testBar('X.u', off + 600_000)] }]);
    history.replaceState(null, '', '#?tl_time=rel');
    const { container } = render(<TimelineChart data={data} />);
    const secs = axisLabels(container).map((l) => {
      const [h = NaN, m = NaN, s = NaN] = l.slice(1).split(':').map(Number);
      return h * 3600 + m * 60 + s;
    });
    const step = secs[1]! - secs[0]!;
    expect(step).toBeGreaterThan(0);
    secs.forEach((s) => expect(s % step).toBe(0));
  });

  it('formats runs longer than an hour without wrapping or timezone offset', () => {
    const data = timeline([{ serial: 'a', data: [testBar('X.t', 0), testBar('X.u', 2 * 3600_000)] }]);
    history.replaceState(null, '', '#?tl_time=rel');
    const { container } = render(<TimelineChart data={data} />);
    expect(axisLabels(container)).toContain('+01:00:00');
  });

  it('tooltip start time follows the selected mode', () => {
    const data = flakyFixture();
    const { container } = render(<TimelineChart data={data} />);
    tagStarts(container, data);
    const bar = flakyAttempts(container).find((b) => b.dataset.testStart === '50000')!;

    fireEvent.mouseEnter(bar, { clientX: 1, clientY: 1 });
    expect(document.querySelector('.timeline-tooltip')?.textContent).toMatch(/started \d\d:\d\d:\d\d/);
    fireEvent.mouseLeave(bar);

    fireEvent.click(timeButton('relative'));
    fireEvent.mouseEnter(bar, { clientX: 1, clientY: 1 });
    expect(document.querySelector('.timeline-tooltip')?.textContent).toContain('started +00:00:50');
  });
});

describe('TimelineChart attempt halo', () => {
  function setup() {
    const data = flakyFixture();
    const utils = render(<TimelineChart data={data} />);
    tagStarts(utils.container, data);
    return utils;
  }

  it('nothing is haloed until a bar is hovered', () => {
    const { container } = setup();
    expect(haloed(container)).toHaveLength(0);
  });

  it('hovering one attempt haloes every attempt of that test across devices, and nothing else', () => {
    const { container } = setup();
    const attempts = flakyAttempts(container);
    expect(attempts).toHaveLength(3);

    fireEvent.mouseEnter(attempts[1]!, { clientX: 1, clientY: 1 });
    expect(haloed(container)).toEqual(attempts);
  });

  it('does not halo same-named tests from another class or another pool', () => {
    const { container } = setup();
    fireEvent.mouseEnter(flakyAttempts(container)[0]!, { clientX: 1, clientY: 1 });
    const decoys = bars(container).filter((b) => ['70000', '110000'].includes(b.dataset.testStart ?? ''));
    expect(decoys).toHaveLength(2);
    for (const d of decoys) expect(d).not.toHaveClass('bar-attempt');
  });

  it('a single-attempt test haloes only itself', () => {
    const { container } = setup();
    const stable = bars(container).find((b) => b.dataset.testStart === '30000')!;
    fireEvent.mouseEnter(stable, { clientX: 1, clientY: 1 });
    expect(haloed(container)).toEqual([stable]);
  });

  it('mouse leave clears the halo and the tooltip', () => {
    const { container } = setup();
    const a = flakyAttempts(container)[0]!;
    fireEvent.mouseEnter(a, { clientX: 1, clientY: 1 });
    fireEvent.mouseLeave(a);
    expect(haloed(container)).toHaveLength(0);
    expect(document.querySelector('.timeline-tooltip')).toBeNull();
  });

  it('moving between attempts keeps only the current test haloed', () => {
    const { container } = setup();
    const flaky = flakyAttempts(container)[0]!;
    const stable = bars(container).find((b) => b.dataset.testStart === '30000')!;
    fireEvent.mouseEnter(flaky, { clientX: 1, clientY: 1 });
    fireEvent.mouseLeave(flaky);
    fireEvent.mouseEnter(stable, { clientX: 1, clientY: 1 });
    expect(haloed(container)).toEqual([stable]);
  });

  it('keyboard focus haloes like hover, blur clears it', () => {
    const { container } = setup();
    const attempts = flakyAttempts(container);
    act(() => attempts[2]!.focus());
    expect(haloed(container)).toEqual(attempts);
    act(() => attempts[2]!.blur());
    expect(haloed(container)).toHaveLength(0);
  });

  it('device prepare / provider init bars never halo anything', () => {
    const { container } = setup();
    const deviceBars = bars(container).filter(
      (b) => b.classList.contains('bar-DEVICE_PREPARE') || b.classList.contains('bar-DEVICE_PROVIDER_INIT'),
    );
    expect(deviceBars).toHaveLength(3);
    for (const d of deviceBars) {
      fireEvent.mouseEnter(d, { clientX: 1, clientY: 1 });
      expect(haloed(container)).toHaveLength(0);
      fireEvent.mouseLeave(d);
    }
  });

  it('halo survives toggling the time scale', () => {
    const { container } = setup();
    const attempts = flakyAttempts(container);
    fireEvent.mouseEnter(attempts[0]!, { clientX: 1, clientY: 1 });
    fireEvent.click(timeButton('relative'));
    expect(haloed(container)).toEqual(attempts);
  });
});

describe('TimelineChart test parsing', () => {
  // Shape of a remote-parser run: the parse device installs, then parses; the rest only install.
  function parsingFixture() {
    return timeline([
      {
        serial: 'parser',
        data: [
          deviceBar('DEVICE_PREPARE', 0),
          deviceBar('TEST_PARSING', 40_000),
          testBar('Suite.first', 70_000),
        ],
      },
      { serial: 'other', data: [deviceBar('DEVICE_PREPARE', 0), testBar('Suite.second', 70_000)] },
    ]);
  }
  const parsingBars = (c: HTMLElement) => bars(c).filter((b) => b.classList.contains('bar-TEST_PARSING'));

  it('draws the parse as its own bar on the parsing device row only', () => {
    const { container } = render(<TimelineChart data={parsingFixture()} />);
    const rows = [...container.querySelectorAll<HTMLElement>('.timeline-row-html')];
    expect(rows.map((r) => r.querySelectorAll('.bar-TEST_PARSING').length)).toEqual([1, 0]);
  });

  it('is in the legend with its own swatch', () => {
    const { container } = render(<TimelineChart data={parsingFixture()} />);
    expect(screen.getByText('Test parsing')).toBeTruthy();
    expect(container.querySelector('.timeline-legend .timeline-legend__swatch.bar-TEST_PARSING')).not.toBeNull();
  });

  it('is not a link to a test and never haloes', () => {
    const { container } = render(<TimelineChart data={parsingFixture()} />);
    const [bar] = parsingBars(container);
    expect(bar!.tagName).toBe('DIV');
    fireEvent.mouseEnter(bar!, { clientX: 1, clientY: 1 });
    expect(haloed(container)).toHaveLength(0);
  });

  it('tooltip names it and gives its duration', () => {
    const { container } = render(<TimelineChart data={parsingFixture()} />);
    fireEvent.mouseEnter(parsingBars(container)[0]!, { clientX: 1, clientY: 1 });
    const text = document.querySelector('.timeline-tooltip')?.textContent ?? '';
    expect(text).toContain('TEST_PARSING');
    expect(text).toContain('5000 ms');
  });
});
