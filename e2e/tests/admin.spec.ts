import { test, expect } from '@playwright/test';

test.describe('Admin Panel', () => {
  test('should redirect to login when accessing admin unauthenticated', async ({ page }) => {
    await page.goto('/admin');
    await expect(page).toHaveURL(/auth\/login|^\/$/);
  });

  test('should redirect to login when accessing admin users unauthenticated', async ({ page }) => {
    await page.goto('/admin/users');
    await expect(page).toHaveURL(/auth\/login|^\/$/);
  });

  test('should redirect to login when accessing admin audit log unauthenticated', async ({ page }) => {
    await page.goto('/admin/audit-log');
    await expect(page).toHaveURL(/auth\/login|^\/$/);
  });
});
