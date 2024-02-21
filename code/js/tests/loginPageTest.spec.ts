/* eslint-disable @typescript-eslint/ban-ts-comment */
/// @ts-ignore />
import { test, expect } from '@playwright/test';

test('login in initial state', async ({ page }) => {
  await page.goto('http://localhost:8080/login');

  await expect(page.locator('.Auth')).toBeVisible();
  await expect(page.locator('.title')).toContainText('Login');
  await expect(page.getByPlaceholder('Username or E-mail')).toBeVisible();
  await expect(page.getByPlaceholder('Password')).toBeVisible();
  await expect(page.locator('.alternative', { hasText: /Register/ })).toBeVisible();
  await expect(page.locator('.alternative')).toBeVisible();
  await expect(page.getByRole('button')).toBeVisible();
});

test('fail to login', async ({ page }) => {
  await page.goto('http://localhost:8080/login');

  const usernameInput = page.getByPlaceholder('Username or E-mail');
  const passwordInput = page.getByPlaceholder('Password');
  const loginButton = page.getByRole('button');
  await expect(usernameInput).toBeVisible();
  await expect(passwordInput).toBeVisible();
  await expect(loginButton).toBeVisible();

  await usernameInput.fill('coelho');
  await passwordInput.fill('123');
  await loginButton.click();

  await expect(usernameInput).toBeVisible();
});

test('can login', async ({ page }) => {
  await page.goto('http://localhost:8080/login');

  const usernameInput = page.getByPlaceholder('Username or E-mail');
  const passwordInput = page.getByPlaceholder('Password');
  const loginButton = page.getByRole('button');
  await expect(usernameInput).toBeVisible();
  await expect(passwordInput).toBeVisible();
  await expect(loginButton).toBeVisible();

  await usernameInput.fill('coelho');
  await passwordInput.fill('1234567890!Aa');
  await loginButton.click();

  await expect(page.locator('.Home')).toBeDefined();
});
