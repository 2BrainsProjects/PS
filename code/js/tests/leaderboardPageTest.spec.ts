/* eslint-disable @typescript-eslint/ban-ts-comment */
/// @ts-ignore />
import { test, expect } from '@playwright/test';

test('leaderboard in initial state without users', async ({ page }) => {
  await page.goto('http://localhost:8080/leaderboard');

  await expect(page.locator('.Leaderboard')).toBeVisible();
  await expect(page.locator('.title')).toContainText('Leaderboard');
  await expect(page.locator('.container')).toBeVisible();
  await expect(page.locator('.container > .header')).toBeVisible();
  await expect(page.locator('.container > .header > h3', { hasText: 'Rank' })).toBeVisible();
  await expect(page.locator('.container > .header > h3', { hasText: 'Name' })).toBeVisible();
  await expect(page.locator('.container > .header > h3', { hasText: 'Rating' })).toBeVisible();
});

test('loading spinner is displayed when leaderboard is fetching', async ({ page }) => {
  await page.goto('http://localhost:8080/leaderboard');
  await expect(page.locator('.LoadingSpinner')).toBeVisible();
});
