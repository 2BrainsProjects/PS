/* eslint-disable @typescript-eslint/ban-ts-comment */
/// @ts-ignore />
import { test, expect } from '@playwright/test';

test('game component renders correctly', async ({ page }) => {
  await page.goto('http://localhost:8080//game/0');

  expect(page.locator('.Game')).toBeDefined();
});

test('loading spinner is displayed when game is fetching', async ({ page }) => {
  await page.goto('http://localhost:8080//game/0');
  await expect(page.locator('.LoadingSpinner')).toBeVisible();
});
