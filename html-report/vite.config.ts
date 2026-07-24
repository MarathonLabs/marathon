import { defineConfig, type UserConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { resolve } from 'node:path';

/**
 * Single entry — the React SPA loaded by every emitted marathon HTML file
 * (index / pool / test / logs). Output: `app.min.js` + `app.min.css`.
 *
 * Timeline chart is now inline in the SPA (React component), so there's no
 * separate iframe target and no second Vite build. `file://`-hosted reports
 * still work because IIFE output loads via plain `<script src>` — Chromium
 * blocks `<script type="module">` from a null origin.
 *
 * File output names are fixed (no content hash) because the Kotlin reporter
 * writes literal `app.min.js` / `app.min.css` references into the template
 * and expects those exact filenames at emit time.
 */
export default defineConfig({
  plugins: [react()],
  base: './',
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    sourcemap: false,
    cssCodeSplit: false,
    modulePreload: false,
    rollupOptions: {
      input: resolve(__dirname, 'src/main.tsx'),
      output: {
        format: 'iife',
        inlineDynamicImports: true,
        entryFileNames: 'app.min.js',
        assetFileNames: (asset) => {
          const name = asset.name ?? '';
          if (name.endsWith('.css')) return 'app.min.css';
          return '[name][extname]';
        },
      },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./vitest.setup.ts'],
    css: false,
  },
} satisfies UserConfig);
