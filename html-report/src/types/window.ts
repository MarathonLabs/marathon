import type {
  HtmlFullTest,
  HtmlIndex,
  HtmlPoolSummary,
  HtmlTestLogDetails,
} from './report';
import type { TimelineExecutionResult } from './timeline';

/**
 * Kotlin `HtmlSummaryReporter` inlines one of these globals per emitted page.
 * The React app inspects `window` to pick which page component to render.
 * `timeline` rides alongside `mainData` on the index page only — it's the
 * data the inline timeline chart renders (no more iframe).
 */
declare global {
  interface Window {
    mainData?: HtmlIndex;
    pool?: HtmlPoolSummary;
    test?: HtmlFullTest;
    logs?: HtmlTestLogDetails;
    timeline?: TimelineExecutionResult;
    /**
     * Run generated-at timestamp emitted on every page so `AppShell` can
     * render the footer inside the React flow (matches the primary payload's
     * `generated_at_ms` on the index page).
     */
    reportGeneratedAt?: number;
  }
}

export type PageKind = 'index' | 'pool' | 'test' | 'logs';

export interface DetectedPage {
  kind: PageKind;
  payload: HtmlIndex | HtmlPoolSummary | HtmlFullTest | HtmlTestLogDetails;
}

/**
 * Detect which page this HTML shell was emitted as. Kotlin sets exactly one
 * of `window.mainData` / `window.pool` / `window.test` / `window.logs`.
 */
export function detectPage(): DetectedPage | null {
  if (window.mainData) return { kind: 'index', payload: window.mainData };
  if (window.pool) return { kind: 'pool', payload: window.pool };
  if (window.test) return { kind: 'test', payload: window.test };
  if (window.logs) return { kind: 'logs', payload: window.logs };
  return null;
}
