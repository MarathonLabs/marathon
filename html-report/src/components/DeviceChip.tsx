import clsx from 'clsx';
import type { HtmlDevice } from '../types/report';
import { useTheme } from '../theme/ThemeProvider';
import { deviceColor } from '../util/deviceColor';

interface DeviceChipProps {
  device: HtmlDevice;
  compact?: boolean;
  className?: string;
}

/**
 * Compact device pill. Displays serial + os label; the color is a
 * deterministic hash of the serial so the same device gets the same shade
 * across runs (unlike the pre-2.0 `randomcolor` scheme).
 */
export function DeviceChip({ device, compact = false, className }: DeviceChipProps) {
  const { resolved } = useTheme();
  const color = deviceColor(device.serial, resolved === 'dark');
  const label = osLabel(device);
  return (
    <span
      className={clsx(
        'inline-flex items-center gap-1 rounded-full border border-surface-border bg-surface-alt px-2 py-0.5 text-xs',
        className,
      )}
      title={`${device.manufacturer} ${device.model_name} · ${label} · ${device.serial}`}
    >
      <span
        aria-hidden
        className="inline-block h-2 w-2 rounded-full"
        style={{ backgroundColor: color }}
      />
      <span className="font-mono">{device.serial}</span>
      {!compact && <span className="text-slate-500 dark:text-slate-400">{label}</span>}
    </span>
  );
}

/**
 * `api 34` for Android (numeric os_major), otherwise the raw version prefixed
 * by manufacturer hint (e.g. `iOS 17.0`, `macOS 14.2`). Falls back to the raw
 * version when we can't guess a family.
 */
export function osLabel(device: HtmlDevice): string {
  const manufacturer = device.manufacturer.toLowerCase();
  if (manufacturer === 'apple') {
    const family = device.model_name.toLowerCase().includes('mac') ? 'macOS' : 'iOS';
    return `${family} ${device.os_version}`;
  }
  if (device.os_major !== null) return `api ${device.os_major}`;
  return device.os_version;
}
