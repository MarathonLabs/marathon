import { useCallback, useEffect, useState } from 'react';

/**
 * Serialize UI state (filters / search / sort) into `location.hash` so links
 * to the current view are shareable and page reloads restore the view.
 *
 * The hash is treated as a URLSearchParams payload (leading `#` stripped).
 * Reads on mount, writes debounced-per-frame on updates, and syncs with
 * external `hashchange` events (back/forward navigation).
 */
export function useHashState(): [URLSearchParams, (mutate: (params: URLSearchParams) => void) => void] {
  const [params, setParams] = useState<URLSearchParams>(() => readHash());

  useEffect(() => {
    function onChange() {
      setParams(readHash());
    }
    window.addEventListener('hashchange', onChange);
    return () => window.removeEventListener('hashchange', onChange);
  }, []);

  const update = useCallback((mutate: (params: URLSearchParams) => void) => {
    setParams((prev) => {
      const next = new URLSearchParams(prev);
      mutate(next);
      const encoded = next.toString();
      // Preserve existing route prefix (`#/attempt/2`) — anything before
      // `?` is kept, params replace the query segment.
      const currentHash = window.location.hash.replace(/^#/, '');
      const routePart = currentHash.split('?')[0] ?? '';
      const rendered = encoded ? `#${routePart}?${encoded}` : `#${routePart}`;
      if (rendered !== window.location.hash) {
        history.replaceState(null, '', rendered.length > 1 ? rendered : ' ');
      }
      return next;
    });
  }, []);

  return [params, update];
}

function readHash(): URLSearchParams {
  const raw = window.location.hash.replace(/^#/, '');
  const [, query] = raw.split('?');
  return new URLSearchParams(query ?? '');
}

/** Convenience: get the pre-`?` part of the hash as a slash-delimited route. */
export function readHashRoute(): string {
  const raw = window.location.hash.replace(/^#/, '');
  return raw.split('?')[0] ?? '';
}

/** Convenience: replace the pre-`?` part of the hash while keeping query params intact. */
export function setHashRoute(route: string) {
  const raw = window.location.hash.replace(/^#/, '');
  const [, query] = raw.split('?');
  const rendered = query ? `#${route}?${query}` : `#${route}`;
  if (rendered !== window.location.hash) history.replaceState(null, '', rendered);
}
