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
          className="block max-h-64 overflow-hidden rounded border border-surface-border bg-surface-alt"
        >
          <img src={src} alt={alt} className="h-full w-full object-contain" />
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
