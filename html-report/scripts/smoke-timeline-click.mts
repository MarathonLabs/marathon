#!/usr/bin/env tsx
/**
 * Regression check: opening the fixture HomePage and clicking a test bar in
 * the timeline iframe should navigate the parent frame to the test detail
 * page. Prints the final URL after the click so we can catch silent
 * postMessage / navigation regressions.
 */
import { existsSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { chromium } from 'playwright';

const __dirname = resolve(fileURLToPath(import.meta.url), '..');
const projectRoot = resolve(__dirname, '..');
const repoRoot = resolve(projectRoot, '..');
const fixtureIndex = join(repoRoot, 'core', 'build', 'fixtures', 'android', 'html', 'index.html');

if (!existsSync(fixtureIndex)) {
  console.error(`Fixture index missing: ${fixtureIndex}`);
  process.exit(1);
}

async function main() {
  const browser = await chromium.launch();
  try {
    const page = await browser.newPage({ viewport: { width: 1440, height: 1400 } });
    await page.goto(pathToFileURL(fixtureIndex).toString());
    // Timeline now renders inline (no iframe). Wait for the first clickable
    // bar to mount + first draw.
    const clickableBar = page.locator('button.bar-clickable').first();
    await clickableBar.waitFor({ state: 'visible', timeout: 15_000 });
    const before = page.url();
    await Promise.all([
      page.waitForURL((url) => url.href !== before, { timeout: 5_000 }),
      clickableBar.click(),
    ]);
    console.log(`nav ok: ${before}\n     → ${page.url()}`);
    process.exit(0);
  } catch (err) {
    console.error('click failed:', err);
    process.exit(1);
  } finally {
    await browser.close();
  }
}

main();
