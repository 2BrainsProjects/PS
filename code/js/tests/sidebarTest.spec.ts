/* eslint-disable @typescript-eslint/ban-ts-comment */
/// @ts-ignore />
import { test, expect } from '@playwright/test';

test('sidebar initial state', async ({ page }) => {
  await page.goto('http://localhost:8080/');

  await expect(page.locator('.Sidebar')).toBeVisible();
  await expect(page.locator('.header')).toBeVisible();
  await expect(page.locator('.menu')).toBeVisible();
  await expect(page.getByRole('link', { name: 'Login' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Home' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Watch' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Leaderboard' })).toBeVisible();
});

test('sidebar navigate to login', async ({ page }) => {
  await page.goto('http://localhost:8080/');

  const loginButton = page.getByRole('link', { name: 'Login' });

  await expect(loginButton).toBeVisible();
  await loginButton.click();
  await expect(page.locator('.title')).toContainText('Login');
});

test('sidebar navigate login and back to home', async ({ page }) => {
  await page.goto('http://localhost:8080/');

  const loginButton = page.getByRole('link', { name: 'Login' });

  await expect(loginButton).toBeVisible();
  await loginButton.click();
  await expect(page.locator('.title')).toContainText('Login');

  const homeButton = page.getByRole('link', { name: 'Home' });

  await expect(homeButton).toBeVisible();
  await homeButton.click();
  await expect(page.locator('.Home')).toBeVisible();
});

test('sidebar navigate to watch', async ({ page }) => {
  await page.goto('http://localhost:8080/');

  const watchButton = page.getByRole('link', { name: 'Watch' });

  await expect(watchButton).toBeVisible();
  await watchButton.click();
  await expect(page.locator('.Watch')).toBeVisible();
});

test('sidebar navigate to leaderboard', async ({ page }) => {
  await page.goto('http://localhost:8080/');

  const leaderboardButton = page.getByRole('link', { name: 'Leaderboard' });

  await expect(leaderboardButton).toBeVisible();
  await leaderboardButton.click();
  await expect(page.locator('.Leaderboard')).toBeVisible();
});
