#!/usr/bin/env tsx
/**
 * Headless render smoke test for the emitted fixture. Opens each page under
 * `file://` in a Playwright-driven Chromium, waits for the app root to mount,
 * and prints any console errors / page errors it collects.
 *
 * Run with `npm run smoke:fixtures` after `npm run fixtures`.
 */
import { readdirSync, existsSync, statSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { chromium, type ConsoleMessage } from 'playwright';

const __dirname = resolve(fileURLToPath(import.meta.url), '..');
const projectRoot = resolve(__dirname, '..');
// The fixture generator lives in Kotlin land now (see
// `HtmlReportFixtureGenerator`). Output lands under `core/build/fixtures/…`,
// resolved relative to the repo root two levels up from this html-report/
// project.
const repoRoot = resolve(projectRoot, '..');
const fixturesRoot = join(repoRoot, 'core', 'build', 'fixtures');

if (!existsSync(fixturesRoot)) {
  console.error(`No fixtures at ${fixturesRoot}. Run \`npm run fixtures\` first.`);
  process.exit(1);
}

const platforms = readdirSync(fixturesRoot).filter((entry) =>
  statSync(join(fixturesRoot, entry)).isDirectory(),
);

interface PageOutcome {
  url: string;
  ok: boolean;
  rootChildren: number;
  consoleErrors: string[];
  pageErrors: string[];
}

async function checkPage(browser: import('playwright').Browser, fileUrl: string): Promise<PageOutcome> {
  const page = await browser.newPage();
  const consoleErrors: string[] = [];
  const pageErrors: string[] = [];
  page.on('console', (msg: ConsoleMessage) => {
    if (msg.type() === 'error') consoleErrors.push(msg.text());
  });
  page.on('pageerror', (err) => {
    pageErrors.push(`${err.name}: ${err.message}`);
  });
  await page.goto(fileUrl);
  // Give the SPA a beat to mount before we sample #root.
  await page.waitForTimeout(500);
  const rootChildren = await page.evaluate(() => document.getElementById('root')?.childElementCount ?? 0);
  await page.close();
  return {
    url: fileUrl,
    ok: rootChildren > 0 && pageErrors.length === 0,
    rootChildren,
    consoleErrors,
    pageErrors,
  };
}

async function main() {
  const browser = await chromium.launch();
  try {
    const pages: string[] = [];
    for (const platform of platforms) {
      // Kotlin fixture generator mirrors the runtime marathon layout:
      // <platform>/html/{index.html, pools/…}.
      const htmlRoot = join(fixturesRoot, platform, 'html');
      pages.push(join(htmlRoot, 'index.html'));
      const poolsDir = join(htmlRoot, 'pools');
      const poolEntries = readdirSync(poolsDir);
      const firstPool = poolEntries.find((entry) => entry.endsWith('.html'));
      if (!firstPool) continue;
      pages.push(join(poolsDir, firstPool));
      const poolId = firstPool.replace(/\.html$/, '');
      const deviceDir = readdirSync(join(poolsDir, poolId))[0];
      if (!deviceDir) continue;
      const testDir = join(poolsDir, poolId, deviceDir);
      const firstTest = readdirSync(testDir).find((entry) => entry.endsWith('.html'));
      if (firstTest) pages.push(join(testDir, firstTest));
      const firstLog = readdirSync(join(testDir, 'logs'))[0];
      if (firstLog) pages.push(join(testDir, 'logs', firstLog));
    }

    let anyFailed = false;
    for (const filePath of pages) {
      const fileUrl = pathToFileURL(filePath).toString();
      const outcome = await checkPage(browser, fileUrl);
      const status = outcome.ok ? 'ok' : 'FAIL';
      console.log(`[${status}] children=${outcome.rootChildren} ${outcome.url}`);
      if (outcome.consoleErrors.length) {
        for (const err of outcome.consoleErrors) console.log(`  console: ${err}`);
      }
      if (outcome.pageErrors.length) {
        for (const err of outcome.pageErrors) console.log(`  page: ${err}`);
      }
      if (!outcome.ok) anyFailed = true;
    }
    process.exit(anyFailed ? 1 : 0);
  } finally {
    await browser.close();
  }
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
