import { test, expect } from '@playwright/test';

test.describe('Compliance', () => {
  test('should redirect to login when accessing compliance unauthenticated', async ({ page }) => {
    await page.goto('/compliance');
    await expect(page).toHaveURL(/auth\/login|^\/$/);
  });

  test('should redirect to login when accessing organization unauthenticated', async ({ page }) => {
    await page.goto('/organization');
    await expect(page).toHaveURL(/auth\/login|^\/$/);
  });

  test('should redirect to login when accessing team unauthenticated', async ({ page }) => {
    await page.goto('/team');
    await expect(page).toHaveURL(/auth\/login|^\/$/);
  });
});
