import type { HtmlDevice } from '../types/report';
import { DeviceChip, osLabel } from './DeviceChip';

/**
 * Compact key/value table used inside expanded attempt cards. Renders every
 * pertinent device attribute Kotlin exposes; missing values render `—`.
 */
export function DeviceDetails({ device }: { device: HtmlDevice }) {
  const rows: Array<[label: string, value: string]> = [
    ['Serial', device.serial],
    ['Model', device.model_name || '—'],
    ['Manufacturer', device.manufacturer || '—'],
    ['OS', osLabel(device)],
    ['OS version', device.os_version || '—'],
    ['Network', device.network_state || '—'],
    ['Form factor', device.is_tablet ? 'Tablet' : 'Phone / other'],
    ['Features', device.features.length ? device.features.join(', ') : 'none'],
  ];
  return (
    <div>
      <div className="mb-2 flex items-center gap-2">
        <DeviceChip device={device} />
      </div>
      <dl className="grid grid-cols-[max-content_1fr] gap-x-4 gap-y-1 text-xs">
        {rows.map(([label, value]) => (
          <div key={label} className="contents">
            <dt className="text-slate-500 dark:text-slate-400">{label}</dt>
            <dd className="font-mono break-all">{value}</dd>
          </div>
        ))}
      </dl>
    </div>
  );
}
