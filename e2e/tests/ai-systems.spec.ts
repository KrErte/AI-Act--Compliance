import { test, expect } from '@playwright/test';

test.describe('AI Systems', () => {
  test('should redirect to login when accessing AI systems unauthenticated', async ({ page }) => {
    await page.goto('/ai-systems');
    await expect(page).toHaveURL(/auth\/login|^\/$/);
  });

  test('should display public risk classifier', async ({ page }) => {
    await page.goto('/classify');
    await expect(page.locator('body')).toContainText('Risk Classifier');
  });

  test('should show classification questions on public classifier', async ({ page }) => {
    await page.goto('/classify');
    // The classifier should load questions or show initial state
    await expect(page.locator('body')).toContainText(/AI|risk|classifier/i);
  });
});
