import { test, expect } from '@playwright/test';

test.describe('Internationalization', () => {
  test('should load landing page with English text by default', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('body')).toContainText('EU AI Act Compliance');
  });

  test('should display login page text in English', async ({ page }) => {
    await page.goto('/auth/login');
    await expect(page.locator('body')).toContainText('Welcome back');
    await expect(page.locator('body')).toContainText('Sign in');
  });

  test('should display register page text', async ({ page }) => {
    await page.goto('/auth/register');
    await expect(page.locator('body')).toContainText('Create your account');
    await expect(page.locator('body')).toContainText('compliance journey');
  });

  test('should display forgot password page text', async ({ page }) => {
    await page.goto('/auth/forgot-password');
    await expect(page.locator('body')).toContainText('Reset your password');
  });

  test('should load translations for public classifier', async ({ page }) => {
    await page.goto('/classify');
    // Wait for translations to load
    await page.waitForTimeout(1000);
    await expect(page.locator('body')).not.toContainText('classification.public_title');
  });
});
