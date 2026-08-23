import * as Dialog from '@radix-ui/react-dialog';
import { useState } from 'react';

/**
 * Hand-rolled screenshot lightbox on Radix Dialog. Enough for our needs
 * (single image per attempt, click to expand, keyboard-dismissible) and
 * avoids pulling in a full gallery lib.
 */
export function Lightbox({ src, alt }: { src: string; alt: string }) {
  const [open, setOpen] = useState(false);
  return (
    <Dialog.Root open={open} onOpenChange={setOpen}>
      <Dialog.Trigger asChild>
        <button
          type="button"
          // Center the thumbnail in the pane and cap its display so tall
          // portrait screenshots (typical for android phone runs) don't
          // stretch the attempt card. Real-size render is the lightbox
          // dialog; the trigger is a preview only.
          className="mx-auto block max-h-96 max-w-full overflow-hidden rounded border border-surface-border bg-surface-alt"
        >
          <img
            src={src}
            alt={alt}
            loading="lazy"
            className="mx-auto max-h-96 w-auto object-contain"
          />
        </button>
      </Dialog.Trigger>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-40 bg-black/70 backdrop-blur-sm" />
        <Dialog.Content className="fixed inset-0 z-50 flex items-center justify-center p-6">
          <Dialog.Title className="sr-only">{alt}</Dialog.Title>
          <Dialog.Close asChild>
            <button
              type="button"
              aria-label="Close"
              className="absolute right-4 top-4 rounded-full bg-white/10 px-3 py-1 text-white hover:bg-white/20"
            >
              ✕
            </button>
          </Dialog.Close>
          <img
            src={src}
            alt={alt}
            className="max-h-full max-w-full rounded shadow-2xl"
          />
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
