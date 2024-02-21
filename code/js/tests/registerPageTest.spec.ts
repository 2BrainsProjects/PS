/* eslint-disable @typescript-eslint/ban-ts-comment */
/// @ts-ignore />
import { test, expect } from '@playwright/test';

test('login in initial state', async ({ page }) => {
  await page.goto('http://localhost:8080/register');

  await expect(page.locator('.title')).toContainText('Register');
  await expect(page.getByPlaceholder('Username')).toBeVisible();
  await expect(page.getByPlaceholder('Email')).toBeVisible();
  await expect(page.getByPlaceholder('Password', { exact: true })).toBeVisible();
  await expect(page.getByPlaceholder('Confirm Password')).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Already have an account? Login' }).getByRole('link')).toBeVisible();
  await expect(page.getByRole('button')).toBeVisible();
});

test('fail to register', async ({ page }) => {
  await page.goto('http://localhost:8080/register');

  const usernameInput = page.getByPlaceholder('Username');
  const emailInput = page.getByPlaceholder('Email');
  const passwordInput = page.getByPlaceholder('Password', { exact: true });
  const confPasswordInput = page.getByPlaceholder('Confirm Password');
  const registerButton = page.getByRole('button');
  await expect(usernameInput).toBeVisible();
  await expect(emailInput).toBeVisible();
  await expect(passwordInput).toBeVisible();
  await expect(confPasswordInput).toBeVisible();
  await expect(registerButton).toBeVisible();

  await usernameInput.fill('abcde');
  await emailInput.fill('abcde@mail.com');
  await passwordInput.fill('123');
  await passwordInput.fill('1234');
  await registerButton.click();

  await expect(usernameInput).toBeVisible();
});

test('can register', async ({ page }) => {
  await page.goto('http://localhost:8080/register');

  const usernameInput = page.getByPlaceholder('Username');
  const emailInput = page.getByPlaceholder('Email');
  const passwordInput = page.getByPlaceholder('Password', { exact: true });
  const confPasswordInput = page.getByPlaceholder('Confirm Password');
  const registerButton = page.getByRole('button');
  await expect(usernameInput).toBeVisible();
  await expect(emailInput).toBeVisible();
  await expect(passwordInput).toBeVisible();
  await expect(confPasswordInput).toBeVisible();
  await expect(registerButton).toBeVisible();

  await usernameInput.fill('abcde');
  await emailInput.fill('abcde@mail.com');
  await passwordInput.fill('1234567890!Aa');
  await passwordInput.fill('1234567890!Aa');
  await registerButton.click();
});
