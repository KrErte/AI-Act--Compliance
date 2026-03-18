import { test, expect } from '@playwright/test';

test.describe('Dashboard', () => {
  test('should display landing page for unauthenticated users', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('body')).toContainText('EU AI Act Compliance');
  });

  test('should display landing hero section', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('body')).toContainText('Get Started');
  });

  test('should display pricing section on landing page', async ({ page }) => {
    await page.goto('/');
    await expect(page.locator('body')).toContainText('Pricing');
  });

  test('should have navigation to login from landing', async ({ page }) => {
    await page.goto('/');
    const loginLink = page.locator('a[href*="login"], a:has-text("Log In")');
    await expect(loginLink.first()).toBeVisible();
  });
});
