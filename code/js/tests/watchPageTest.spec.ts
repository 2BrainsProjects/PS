/* eslint-disable @typescript-eslint/ban-ts-comment */
/// @ts-ignore />
import { test, expect } from '@playwright/test';

test('watch in initial state', async ({ page }) => {
  await page.goto('http://localhost:8080/watch');

  await expect(page.locator('.Watch')).toBeVisible();
});

test('loading spinner is displayed when fetching games', async ({ page }) => {
  await page.goto('http://localhost:8080/watch');
  await expect(page.locator('.LoadingSpinner')).toBeVisible();
});
