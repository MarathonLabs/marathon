import { useLayoutEffect, useState, type RefObject } from 'react';

/**
 * Distance in pixels between the referenced element's top edge and the top
 * of the document, kept fresh across layout shifts.
 *
 * `@tanstack/react-virtual`'s `useWindowVirtualizer` treats the window as
 * the scroll parent and expects `scrollMargin` to be the offset from the
 * document's top scroll position to where the virtualized list actually
 * starts. Without a correct value, the visible range calculation is off by
 * exactly `parentEl.offsetTop`, which manifests as the list rendering only
 * a partial slice at the top plus massive whitespace before the footer.
 *
 * Measures on mount, on `<body>` resize (chip rows wrapping, filter
 * expansion, breadcrumb reflow), and on window resize.
 */
export function useScrollMargin(ref: RefObject<HTMLElement>): number {
  const [margin, setMargin] = useState(0);
  useLayoutEffect(() => {
    const el = ref.current;
    if (!el) return;
    const measure = () => {
      let offset = 0;
      let cur: HTMLElement | null = el;
      while (cur) {
        offset += cur.offsetTop;
        cur = cur.offsetParent as HTMLElement | null;
      }
      setMargin(offset);
    };
    measure();
    const ro = new ResizeObserver(measure);
    ro.observe(document.body);
    window.addEventListener('resize', measure);
    return () => {
      ro.disconnect();
      window.removeEventListener('resize', measure);
    };
  }, [ref]);
  return margin;
}
