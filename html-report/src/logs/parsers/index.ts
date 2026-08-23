import { genericParser } from './generic';
import { logcatParser } from './logcat';
import { osLogParser } from './oslog';
import type { LogParser } from '../types';

/**
 * Registration order matters: the detector picks the highest scorer and
 * breaks ties in favor of the earlier entry. `generic` is always last so it
 * only wins when nothing else does.
 */
export const PARSERS: LogParser[] = [logcatParser, osLogParser, genericParser];

export function findParser(id: string): LogParser {
  return PARSERS.find((p) => p.id === id) ?? genericParser;
}

/**
 * Pick a parser by scoring the first `SAMPLE_LINES` non-empty lines against
 * every registered parser. Detector ignores the tail so a 10k-line log stays
 * cheap to bootstrap.
 */
const SAMPLE_LINES = 32;
export function detectParser(text: string): LogParser {
  const lines = text.split(/\r?\n/).filter(Boolean).slice(0, SAMPLE_LINES);
  let best: LogParser = genericParser;
  let bestScore = -Infinity;
  for (const parser of PARSERS) {
    const score = parser.detect(lines);
    if (score > bestScore) {
      best = parser;
      bestScore = score;
    }
  }
  return best;
}
