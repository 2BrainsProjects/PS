/* eslint-disable @typescript-eslint/ban-ts-comment */
/// @ts-ignore />
import { test, expect } from '@playwright/test';

test('lobby needs authorization so it goes to login', async ({ page }) => {
  await page.goto('http://localhost:8080/lobby');

  await expect(page.locator('.title')).toContainText('Login');
});

test('lobby in initial state is the game configuration', async ({ page }) => {
  await page.goto('http://localhost:8080/lobby');

  const usernameInput = page.getByPlaceholder('Username or E-mail');
  const passwordInput = page.getByPlaceholder('Password');
  const loginButton = page.getByRole('button');
  await expect(usernameInput).toBeVisible();
  await expect(passwordInput).toBeVisible();
  await expect(loginButton).toBeVisible();

  await usernameInput.fill('Ricardo');
  await passwordInput.fill('1234567890!Aa');
  await loginButton.click();

  await expect(page.locator('.title')).toContainText('Game Configuration');
  await expect(page.locator('.container')).toBeVisible();
  await expect(page.getByText(/Board Size/)).toBeVisible();
  await expect(page.getByText(/Variant/)).toBeVisible();
  await expect(page.getByText(/Opening/)).toBeVisible();
  await expect(page.locator('.play-button')).toBeVisible();
});
