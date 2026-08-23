import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import clsx from 'clsx';
import { scaleTime } from 'd3-scale';
import { timeFormat } from 'd3-time-format';
import type {
  MetricType,
  TimelineData,
  TimelineExecutionResult,
  TimelineMeasure,
} from '../types/timeline';
import { useHashState } from '../hooks/useHashState';

interface TimelineChartProps {
  data: TimelineExecutionResult;
  onOpenTest?: (payload: { measure: string; data: TimelineData }) => void;
  className?: string;
}

const LEGEND: Array<{ type: MetricType; label: string }> = [
  { type: 'PASSED', label: 'Passed test' },
  { type: 'FAILURE', label: 'Failed test' },
  { type: 'IGNORED', label: 'Ignored / assumption' },
  { type: 'INCOMPLETE', label: 'Incomplete' },
  { type: 'DEVICE_PREPARE', label: 'Device prepare' },
  { type: 'DEVICE_PROVIDER_INIT', label: 'Provider init' },
];

const formatClock = timeFormat('%H:%M:%S');

// Layout constants — kept in sync with the CSS in `src/styles.css`.
// Label column width is measured at render time (see `useLabelColumnWidth`);
// these are the floor and ceiling. Floor keeps the column readable when every
// label is short (short serials + no meta line); ceiling stops one pathological
// device (an emulator with a fully-qualified host chain like
// `127.0.0.1:5037:localhost:45071`) from starving the chart area.
const LABEL_COLUMN_MIN = 200;
const LABEL_COLUMN_MAX = 360;
const LABEL_COLUMN_PADDING = 44; // matches .timeline-label-cell padding+bracket
const ROW_HEIGHT = 50;
const BAR_HEIGHT = 16;
const HEADER_HEIGHT = 36;
const MIN_CHART_WIDTH = 480;
const BRACKET_INSET = 12;
const BRACKET_HALF = 18;
const BRACKET_TICK = 8;

interface Filters {
  osMajors: Set<string>;
  manufacturers: Set<string>;
}
const EMPTY_FILTERS: Filters = {
  osMajors: new Set(),
  manufacturers: new Set(),
};

// URL-hash query keys. Prefixed so they don't collide with page-level state
// (`q`, `status`, etc. on PoolPage) if a user copies a full URL between pages.
//
// Device serial isn't filterable — each device is its own row, so single-serial
// filtering just dims every-other row with no analytical value.
const HASH_KEYS = {
  osMajors: 'tl_os',
  manufacturers: 'tl_mfg',
} as const;

/**
 * HTML timeline. Replaces the earlier imperative D3/SVG chart. Layout is a
 * simple two-column grid — label column on the left, chart column on the
 * right — with bars rendered as absolutely-positioned buttons over each row.
 *
 * Why HTML: no more `container.innerHTML = ''` re-render pattern (which was
 * clamping page scroll every time a filter chip changed), real `<button>`
 * semantics on bars (keyboard nav + focus rings + a11y), and the filter box
 * height stays stable across React re-renders because the layout is
 * declarative.
 */
export function TimelineChart({ data, onOpenTest, className }: TimelineChartProps) {
  const [params, updateHash] = useHashState();
  const filters = useMemo(() => filtersFromHash(params), [params]);
  const facets = useMemo(() => facetsFor(data), [data]);
  const dimmed = useMemo(() => dimSet(data, filters), [data, filters]);

  const chartRef = useRef<HTMLDivElement>(null);
  const [chartWidth, setChartWidth] = useState(0);
  useLayoutEffect(() => {
    const el = chartRef.current;
    if (!el) return;
    const measure = () => setChartWidth(el.clientWidth);
    measure();
    const ro = new ResizeObserver(measure);
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  // Auto-size label column to widest label text (both serial + meta lines).
  // Sized after mount from unclipped label text; column is capped at
  // LABEL_COLUMN_MAX to keep the chart area from being starved by one long serial.
  const labelColumnRef = useRef<HTMLDivElement>(null);
  const [labelColumnWidth, setLabelColumnWidth] = useState(LABEL_COLUMN_MIN);
  useLayoutEffect(() => {
    const container = labelColumnRef.current;
    if (!container) return;
    const measure = () => {
      let widest = 0;
      // Text nodes have `white-space: nowrap` — scrollWidth is the intrinsic
      // laid-out width, ignoring the column's clip. Grab both lines per row.
      container
        .querySelectorAll<HTMLElement>(
          '.timeline-label-cell__serial, .timeline-label-cell__meta',
        )
        .forEach((el) => {
          if (el.scrollWidth > widest) widest = el.scrollWidth;
        });
      const w = Math.max(
        LABEL_COLUMN_MIN,
        Math.min(LABEL_COLUMN_MAX, widest + LABEL_COLUMN_PADDING),
      );
      setLabelColumnWidth(w);
    };
    measure();
    // Re-measure on font load or resize — glyph metrics can shift after
    // web fonts swap in, and dark/light theme toggles restyle text weight.
    const ro = new ResizeObserver(measure);
    ro.observe(container);
    return () => ro.disconnect();
  }, [data]);

  const measures = useMemo(() => data.measures.filter((m) => m.data.length > 0), [data.measures]);
  const [domainStart, domainEnd] = useMemo(() => domainOf(measures), [measures]);
  const availableWidth = Math.max(MIN_CHART_WIDTH, chartWidth);
  const scale = useMemo(
    () => scaleTime().domain([domainStart, domainEnd]).range([0, availableWidth]),
    [domainStart, domainEnd, availableWidth],
  );
  const ticks = useMemo(
    () => scale.ticks(Math.max(4, Math.floor(availableWidth / 110))),
    [scale, availableWidth],
  );

  const toggle = useCallback(
    (key: keyof Filters, value: string) => {
      updateHash((p) => {
        const hashKey = HASH_KEYS[key];
        const current = new Set(filters[key]);
        if (current.has(value)) current.delete(value);
        else current.add(value);
        if (current.size === 0) p.delete(hashKey);
        else p.set(hashKey, [...current].join(','));
      });
    },
    [filters, updateHash],
  );

  const reset = useCallback(() => {
    updateHash((p) => {
      for (const key of Object.values(HASH_KEYS)) p.delete(key);
    });
  }, [updateHash]);

  const anyActive = filters.osMajors.size + filters.manufacturers.size > 0;

  const [tooltip, setTooltip] = useState<TooltipState | null>(null);

  const emptyChart = measures.length === 0;
  return (
    <div className={clsx('flex flex-col gap-3', className)}>
      <TimelineFilters
        facets={facets}
        filters={filters}
        onToggle={toggle}
        onReset={reset}
        anyActive={anyActive}
      />
      <div className="rounded-md border border-surface-border">
        <TimelineLegend />
        {emptyChart ? (
          <div className="p-6 text-sm text-slate-500 dark:text-slate-400">
            No timeline data recorded.
          </div>
        ) : (
          <div className="flex">
            <div
              ref={labelColumnRef}
              style={{ width: labelColumnWidth, flex: '0 0 auto' }}
            >
              {/* Spacer matching the axis header row over on the right */}
              <div style={{ height: HEADER_HEIGHT }} />
              {measures.map((m) => (
                <TimelineLabelCell key={m.measure} measure={m} dimmed={dimmed.has(m.measure)} />
              ))}
            </div>
            <div ref={chartRef} className="relative flex-1 overflow-hidden">
              <TimelineAxis ticks={ticks} scale={scale} height={HEADER_HEIGHT} />
              {measures.map((m) => (
                <TimelineRow
                  key={m.measure}
                  measure={m}
                  scale={scale}
                  dimmed={dimmed.has(m.measure)}
                  onOpenTest={onOpenTest}
                  onTooltip={setTooltip}
                />
              ))}
            </div>
          </div>
        )}
      </div>
      {tooltip && <TimelineTooltip state={tooltip} />}
    </div>
  );
}

// ---------- Facets + filters -----------------------------------------------

interface TimelineFacets {
  osMajors: string[];
  manufacturers: string[];
}

function facetsFor(data: TimelineExecutionResult): TimelineFacets {
  const os = new Set<string>();
  const mfg = new Set<string>();
  for (const measure of data.measures) {
    const d = measure.device;
    if (!d) continue;
    if (d.manufacturer) mfg.add(d.manufacturer);
    const label = osFacetLabel(d.osMajor, d.osVersion, d.manufacturer);
    if (label) os.add(label);
  }
  return {
    osMajors: [...os].sort(),
    manufacturers: [...mfg].sort(),
  };
}

function osFacetLabel(
  osMajor: number | null | undefined,
  osVersion: string | undefined,
  manufacturer: string | undefined,
): string | null {
  if (manufacturer?.toLowerCase() === 'apple') {
    return osVersion ? `iOS ${osVersion.split('.')[0]}` : null;
  }
  if (typeof osMajor === 'number') return `api ${osMajor}`;
  return osVersion ?? null;
}

function dimSet(data: TimelineExecutionResult, filters: Filters): Set<string> {
  const active = filters.osMajors.size + filters.manufacturers.size;
  const dimmed = new Set<string>();
  if (active === 0) return dimmed;
  for (const measure of data.measures) {
    const d = measure.device;
    const osLabel = d ? osFacetLabel(d.osMajor, d.osVersion, d.manufacturer) : null;
    const passesOs =
      filters.osMajors.size === 0 || (osLabel !== null && filters.osMajors.has(osLabel));
    const passesMfg =
      filters.manufacturers.size === 0 ||
      (!!d?.manufacturer && filters.manufacturers.has(d.manufacturer));
    if (!(passesOs && passesMfg)) dimmed.add(measure.measure);
  }
  return dimmed;
}

function filtersFromHash(params: URLSearchParams): Filters {
  const decode = (raw: string | null) => new Set(raw?.split(',').filter(Boolean) ?? []);
  return {
    osMajors: decode(params.get(HASH_KEYS.osMajors)),
    manufacturers: decode(params.get(HASH_KEYS.manufacturers)),
  };
}

// ---------- Row rendering --------------------------------------------------

function domainOf(measures: TimelineMeasure[]): [Date, Date] {
  let start = Number.POSITIVE_INFINITY;
  let end = 0;
  for (const measure of measures) {
    for (const point of measure.data) {
      if (point.startDate < start) start = point.startDate;
      if (point.endDate > end) end = point.endDate;
    }
  }
  if (!Number.isFinite(start)) start = Date.now();
  if (end <= start) end = start + 1;
  return [new Date(start), new Date(end)];
}

function TimelineLegend() {
  return (
    <div className="timeline-legend px-3 py-2 border-b border-surface-border">
      {LEGEND.map((entry) => (
        <span key={entry.type} className="timeline-legend__item">
          <span className={`timeline-legend__swatch bar-${entry.type}`} />
          {entry.label}
        </span>
      ))}
    </div>
  );
}

/**
 * `scaleTime()(t)` is typed as `number | undefined` in d3-scale's `.d.ts`
 * because the accessor supports coercion of unknown inputs. When we call it
 * with real Dates the value is always numeric — narrow at the call site.
 */
type TimeScale = ReturnType<typeof scaleTime>;
const scaleX = (scale: TimeScale, input: Date | number): number => scale(input) as number;

function TimelineAxis({
  ticks,
  scale,
  height,
}: {
  ticks: Date[];
  scale: TimeScale;
  height: number;
}) {
  return (
    <div className="relative border-b border-surface-border" style={{ height }}>
      {ticks.map((t) => {
        const x = scaleX(scale, t);
        return (
          <div
            key={t.getTime()}
            className="absolute -translate-x-1/2 text-[10px] text-slate-500 dark:text-slate-400 tabular-nums"
            style={{ left: x, top: height / 2 - 6 }}
          >
            {formatClock(t)}
          </div>
        );
      })}
    </div>
  );
}

function TimelineLabelCell({ measure, dimmed }: { measure: TimelineMeasure; dimmed: boolean }) {
  return (
    <div
      className="timeline-label-cell"
      style={{ height: ROW_HEIGHT, opacity: dimmed ? 0.2 : 1 }}
    >
      <div className="timeline-label-cell__text">
        <div className="timeline-label-cell__serial">{measure.measure}</div>
        {measure.device && (
          <div className="timeline-label-cell__meta">{deviceMeta(measure)}</div>
        )}
      </div>
      <Bracket />
    </div>
  );
}

function Bracket() {
  const w = BRACKET_TICK + 2;
  const h = BRACKET_HALF * 2;
  return (
    <svg
      className="timeline-label-cell__bracket"
      width={w}
      height={h}
      viewBox={`0 0 ${w} ${h}`}
      aria-hidden
    >
      <path
        d={`M 0 0 H ${BRACKET_TICK} V ${h} H 0`}
        fill="none"
        strokeWidth={1}
        style={{ stroke: 'var(--tl-grid)' }}
      />
    </svg>
  );
}

function TimelineRow({
  measure,
  scale,
  dimmed,
  onOpenTest,
  onTooltip,
}: {
  measure: TimelineMeasure;
  scale: TimeScale;
  dimmed: boolean;
  onOpenTest?: (payload: { measure: string; data: TimelineData }) => void;
  onTooltip: (state: TooltipState | null) => void;
}) {
  return (
    <div
      className="timeline-row-html"
      style={{ height: ROW_HEIGHT, opacity: dimmed ? 0.2 : 1 }}
    >
      <div className="timeline-row-html__guide" />
      {measure.data.map((point, i) => {
        const left = scaleX(scale, point.startDate);
        const width = Math.max(1, scaleX(scale, point.endDate) - left);
        const clickable = Boolean(point.poolId && point.testFilename && point.deviceSerial);
        const commonHandlers = {
          onMouseEnter: (e: React.MouseEvent) =>
            onTooltip({
              x: e.clientX + 12,
              y: e.clientY + 12,
              html: tooltipHtml(measure, point),
            }),
          onMouseMove: (e: React.MouseEvent) =>
            onTooltip({
              x: e.clientX + 12,
              y: e.clientY + 12,
              html: tooltipHtml(measure, point),
            }),
          onMouseLeave: () => onTooltip(null),
        };
        const barStyle = {
          left,
          width,
          top: (ROW_HEIGHT - BAR_HEIGHT) / 2,
          height: BAR_HEIGHT,
        } as const;
        if (clickable) {
          return (
            <button
              key={i}
              type="button"
              className={`bar bar-${point.metricType} bar-clickable`}
              style={barStyle}
              aria-label={`${point.testName} — ${point.metricType.toLowerCase()}`}
              onClick={() => onOpenTest?.({ measure: measure.measure, data: point })}
              {...commonHandlers}
            />
          );
        }
        return (
          <div
            key={i}
            className={`bar bar-${point.metricType}`}
            style={barStyle}
            aria-hidden
            {...commonHandlers}
          />
        );
      })}
    </div>
  );
}

// ---------- Tooltip ---------------------------------------------------------

interface TooltipState {
  x: number;
  y: number;
  html: string;
}

function TimelineTooltip({ state }: { state: TooltipState }) {
  return (
    <div
      className="timeline-tooltip"
      style={{ left: state.x, top: state.y }}
      // Tooltip is data-driven — trusted content generated in this file only.
      dangerouslySetInnerHTML={{ __html: state.html }}
    />
  );
}

function deviceMeta(measure: TimelineMeasure): string {
  const d = measure.device;
  if (!d) return '';
  const parts: string[] = [];
  if (d.modelName) parts.push(d.modelName);
  if (d.osMajor !== undefined && d.osMajor !== null) parts.push(`api ${d.osMajor}`);
  else if (d.osVersion) parts.push(d.osVersion);
  if (d.manufacturer) parts.push(d.manufacturer);
  return parts.join(' · ');
}

function tooltipHtml(measure: TimelineMeasure, data: TimelineData): string {
  const duration = data.endDate - data.startDate;
  const started = formatClock(new Date(data.startDate));
  const deviceLine = measure.device
    ? `<div>${escapeHtml(deviceMeta(measure) || measure.measure)}</div>`
    : '';
  return [
    `<div><strong>${escapeHtml(data.testName)}</strong></div>`,
    `<div>${escapeHtml(data.metricType)} · ${duration} ms · started ${started}</div>`,
    deviceLine,
    data.batchId ? `<div>batch: ${escapeHtml(data.batchId)}</div>` : '',
    data.attemptIndex !== undefined ? `<div>attempt: ${data.attemptIndex + 1}</div>` : '',
  ]
    .filter(Boolean)
    .join('');
}

function escapeHtml(input: string): string {
  return input
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

// ---------- Filter UI ------------------------------------------------------

function TimelineFilters({
  facets,
  filters,
  onToggle,
  onReset,
  anyActive,
}: {
  facets: TimelineFacets;
  filters: Filters;
  onToggle: (key: keyof Filters, value: string) => void;
  onReset: () => void;
  anyActive: boolean;
}) {
  // Reset button lives inline with the first facet row — no dedicated header
  // row eats vertical space. The button occupies a reserved-width slot so
  // adding/removing it doesn't reflow the surrounding chip layout.
  return (
    <div className="relative flex flex-col gap-2 rounded-md border border-surface-border bg-surface p-3 pr-16 text-xs">
      {facets.osMajors.length > 1 && (
        <FacetRow
          label="OS"
          values={facets.osMajors}
          active={filters.osMajors}
          onToggle={(v) => onToggle('osMajors', v)}
        />
      )}
      {facets.manufacturers.length > 1 && (
        <FacetRow
          label="Manufacturer"
          values={facets.manufacturers}
          active={filters.manufacturers}
          onToggle={(v) => onToggle('manufacturers', v)}
        />
      )}
      <div className="absolute right-3 top-3">
        {anyActive && (
          <button
            type="button"
            onClick={onReset}
            className="rounded border border-surface-border px-2 py-0.5 hover:bg-surface-alt"
          >
            reset
          </button>
        )}
      </div>
    </div>
  );
}

function FacetRow({
  label,
  values,
  active,
  onToggle,
}: {
  label: string;
  values: string[];
  active: Set<string>;
  onToggle: (value: string) => void;
}) {
  return (
    <div className="flex flex-wrap items-center gap-1">
      <span className="mr-1 text-slate-500 dark:text-slate-400">{label}:</span>
      {values.map((v) => (
        <button
          key={v}
          type="button"
          onClick={() => onToggle(v)}
          className={clsx(
            'rounded-full border px-2 py-0.5 transition',
            active.has(v)
              ? 'border-sky-500 bg-sky-500/10 text-sky-700 dark:text-sky-300'
              : 'border-surface-border bg-surface hover:border-slate-400 dark:hover:border-slate-500',
          )}
        >
          {v}
        </button>
      ))}
    </div>
  );
}
