/* eslint-disable @typescript-eslint/ban-ts-comment */
/// @ts-ignore />
import { test, expect } from '@playwright/test';

test('home page init', async ({ page }) => {
  await page.goto('http://localhost:8080/');

  await expect(page.locator('.title')).toContainText('Anonichat');
  await expect(page.getByText('Anonichat, also called Five in a')).toBeVisible();
  await expect(page.locator('h3')).toContainText('Developed by:');
  await expect(page.getByRole('link', { name: 'Ricardo Costa' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Vasco Costa' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Diogo Almeida' })).toBeVisible();
  await expect(page.getByText('Also available on mobile!')).toBeVisible();
  await expect(page.locator('small')).toContainText(/Version/);
});

test('home page image is present', async ({ page }) => {
  await page.goto('http://localhost:8080/');
  await expect(page.locator('img')).toBeVisible();
});

test('loading spinner is displayed when home is fetching', async ({ page }) => {
  await page.goto('http://localhost:8080/');
  await expect(page.locator('.LoadingSpinner')).toBeVisible();
});

test('developer email links are correct', async ({ page }) => {
  await page.goto('http://localhost:8080/');
  const developerEmailLinks = [
    await page.getByRole('link', { name: 'Ricardo Costa' }).getAttribute('href'),
    await page.getByRole('link', { name: 'Vasco Costa' }).getAttribute('href'),
    await page.getByRole('link', { name: 'Diogo Almeida' }).getAttribute('href'),
  ];
  const expectedEmailLinks = [
    'mailto:rcosta.ms358@gmail.com',
    'mailto:vascosta15@gmail.com',
    'mailto:diogoalmeida107@hotmail.com',
  ];
  expect(developerEmailLinks).toEqual(expectedEmailLinks);
});
