import type { Config } from 'tailwindcss';

const config: Config = {
  content: ['./src/**/*.{ts,tsx,html}', './index.html'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // Neutral surface tokens driven by the theme toggle. Concrete colors
        // live in Tailwind's slate/zinc scales so we don't hand-roll a palette.
        surface: {
          DEFAULT: 'rgb(var(--color-surface) / <alpha-value>)',
          alt: 'rgb(var(--color-surface-alt) / <alpha-value>)',
          border: 'rgb(var(--color-surface-border) / <alpha-value>)',
        },
        // Test status colors. Mapped in `styles.css` per-theme so contrast holds
        // in both light and dark mode.
        status: {
          passed: 'rgb(var(--color-passed) / <alpha-value>)',
          failed: 'rgb(var(--color-failed) / <alpha-value>)',
          ignored: 'rgb(var(--color-ignored) / <alpha-value>)',
          flaky: 'rgb(var(--color-flaky) / <alpha-value>)',
        },
      },
      fontFamily: {
        sans: ['system-ui', 'sans-serif'],
        mono: ['ui-monospace', 'SFMono-Regular', 'Menlo', 'monospace'],
      },
    },
  },
  plugins: [],
};

export default config;
