/**
 * Deterministic hash → HSL color mapping. Two runs of the same set of device
 * serials produce the same colors, and identical serials across sessions
 * always render the same shade. Replaces the old `randomcolor` dep which
 * drifted between renders.
 */
function hashString(input: string): number {
  let h = 2166136261 >>> 0;
  for (let i = 0; i < input.length; i++) {
    h ^= input.charCodeAt(i);
    h = Math.imul(h, 16777619) >>> 0;
  }
  return h;
}

/**
 * @param seed device serial (or any stable identifier)
 * @param dark caller's current theme; darker background → brighter chip
 */
export function deviceColor(seed: string, dark: boolean): string {
  const hue = hashString(seed) % 360;
  const saturation = 65;
  const lightness = dark ? 65 : 45;
  return `hsl(${hue}deg ${saturation}% ${lightness}%)`;
}
