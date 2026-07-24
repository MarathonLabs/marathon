#!/usr/bin/env tsx
/**
 * Take Playwright screenshots of every fixture page. Also collects console
 * errors and failed network requests so we can see broken hrefs before the
 * user does.
 */
import { existsSync, mkdirSync, readdirSync, statSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { chromium } from 'playwright';

const __dirname = resolve(fileURLToPath(import.meta.url), '..');
const projectRoot = resolve(__dirname, '..');
const repoRoot = resolve(projectRoot, '..');
const fixturesRoot = join(repoRoot, 'core', 'build', 'fixtures');
// Keep screenshots co-located with fixtures so a `git clean` on core/build
// wipes both. Playwright reads/writes here only, never publishes anywhere.
const shotsRoot = join(repoRoot, 'core', 'build', 'fixture-screenshots');

if (!existsSync(fixturesRoot)) {
  console.error(`No fixtures at ${fixturesRoot}. Run \`npm run fixtures\` first.`);
  process.exit(1);
}
mkdirSync(shotsRoot, { recursive: true });

const platforms = readdirSync(fixturesRoot).filter((entry) =>
  statSync(join(fixturesRoot, entry)).isDirectory(),
);

async function shoot(browser: import('playwright').Browser, fileUrl: string, target: string) {
  const page = await browser.newPage({
    viewport: { width: 1440, height: 900 },
    // Match the OS-level dark preference so the emitted report opens in dark
    // mode by default (fixture ThemeProvider defaults to `system`). Toggling
    // color-scheme here also exercises the dark-mode chip tokens.
    colorScheme: target.includes('.dark.') ? 'dark' : 'light',
  });
  const failed: string[] = [];
  const errors: string[] = [];
  page.on('requestfailed', (req) => {
    failed.push(`${req.failure()?.errorText ?? 'failed'} ${req.url()}`);
  });
  page.on('pageerror', (err) => errors.push(`${err.name}: ${err.message}`));
  page.on('console', (msg) => {
    if (msg.type() === 'error') errors.push(msg.text());
  });
  await page.goto(fileUrl);
  await page.waitForTimeout(1000);
  await page.screenshot({ path: target, fullPage: true });
  await page.close();
  return { failed, errors };
}

async function main() {
  const browser = await chromium.launch();
  try {
    for (const platform of platforms) {
      const htmlRoot = join(fixturesRoot, platform, 'html');
      const platformShots = join(shotsRoot, platform);
      mkdirSync(platformShots, { recursive: true });

      const cases: Array<[string, string]> = [];
      cases.push(['index', join(htmlRoot, 'index.html')]);
      const poolsDir = join(htmlRoot, 'pools');
      const firstPool = readdirSync(poolsDir).find((entry) => entry.endsWith('.html'));
      if (firstPool) {
        cases.push(['pool', join(poolsDir, firstPool)]);
        const poolId = firstPool.replace(/\.html$/, '');
        const firstDevice = readdirSync(join(poolsDir, poolId))[0];
        if (firstDevice) {
          const testDir = join(poolsDir, poolId, firstDevice);
          const firstTest = readdirSync(testDir).find((e) => e.endsWith('.html'));
          if (firstTest) cases.push(['test', join(testDir, firstTest)]);
          const firstLog = readdirSync(join(testDir, 'logs'))[0];
          if (firstLog) cases.push(['logs', join(testDir, 'logs', firstLog)]);
        }
      }

      for (const [label, filePath] of cases) {
        const url = pathToFileURL(filePath).toString();
        for (const theme of ['light', 'dark'] as const) {
          const out = join(platformShots, `${label}.${theme}.png`);
          const { failed, errors } = await shoot(browser, url, out);
          console.log(`[${platform}/${label}/${theme}] → ${out}`);
          for (const f of failed) console.log(`  requestfailed: ${f}`);
          for (const e of errors) console.log(`  error: ${e}`);
        }
      }
    }
  } finally {
    await browser.close();
  }
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
