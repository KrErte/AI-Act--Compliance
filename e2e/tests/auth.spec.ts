import { test, expect } from '@playwright/test';

test.describe('Authentication', () => {
  test('should display login page', async ({ page }) => {
    await page.goto('/auth/login');
    await expect(page.locator('h2')).toContainText('Welcome back');
    await expect(page.locator('input[type="email"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
  });

  test('should display register page', async ({ page }) => {
    await page.goto('/auth/register');
    await expect(page.locator('h2')).toContainText('Create your account');
  });

  test('should show validation errors on empty login', async ({ page }) => {
    await page.goto('/auth/login');
    await page.locator('button[type="submit"]').click();
    // Form should not navigate away
    await expect(page).toHaveURL(/auth\/login/);
  });

  test('should navigate to register from login', async ({ page }) => {
    await page.goto('/auth/login');
    await page.locator('a[href*="register"]').click();
    await expect(page).toHaveURL(/auth\/register/);
  });

  test('should navigate to forgot password from login', async ({ page }) => {
    await page.goto('/auth/login');
    await page.locator('a[href*="forgot"]').click();
    await expect(page).toHaveURL(/auth\/forgot-password/);
  });

  test('should display forgot password page', async ({ page }) => {
    await page.goto('/auth/forgot-password');
    await expect(page.locator('h2')).toContainText('Reset your password');
  });

  test('should redirect unauthenticated users to login', async ({ page }) => {
    await page.goto('/dashboard');
    await expect(page).toHaveURL(/auth\/login|^\/$/);
  });
});
