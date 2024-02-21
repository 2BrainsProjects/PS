/* eslint-disable @typescript-eslint/ban-ts-comment */
/// @ts-ignore />
import { test, expect } from '@playwright/test';

test('profile in initial state', async ({ page }) => {
  await page.goto('http://localhost:8080/user/0');

  await expect(page.locator('.Profile')).toBeDefined();
});

test('loading spinner is displayed when profile is fetching', async ({ page }) => {
  await page.goto('http://localhost:8080/user/0');
  await expect(page.locator('.LoadingSpinner')).toBeVisible();
});
